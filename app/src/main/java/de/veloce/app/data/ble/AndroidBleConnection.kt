package de.veloce.app.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import dagger.hilt.android.qualifiers.ApplicationContext
import de.veloce.app.domain.model.BleDevice
import de.veloce.app.domain.model.ConnectionState
import de.veloce.app.domain.model.ParsedBleLine
import de.veloce.app.domain.repository.BleConnection
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

@Singleton
class AndroidBleConnection @Inject constructor(
    @ApplicationContext private val context: Context,
) : BleConnection {
    private val adapter: BluetoothAdapter? =
        context.getSystemService(BluetoothManager::class.java)?.adapter
    private val mutableState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    override val state = mutableState.asStateFlow()
    private val mutableLines = MutableSharedFlow<ParsedBleLine>(extraBufferCapacity = 256)
    override val lines = mutableLines.asSharedFlow()

    private val operations = Mutex()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val lineParser = LineParser()
    private var activeGatt: BluetoothGatt? = null
    private var writeCharacteristic: BluetoothGattCharacteristic? = null
    private var pendingWrite: CompletableDeferred<Int>? = null
    private var retryCount = 0
    private var connectingDevice: BluetoothDevice? = null
    private var connectingName: String = "Bluetooth-Gerät"
    private var intentionalDisconnect = false

    private val callback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (gatt !== activeGatt) {
                gatt.close()
                return
            }
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                if (!gatt.discoverServices()) {
                    failConnection("Bluetooth-Dienste konnten nicht durchsucht werden.")
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                if (retryForGatt133(gatt, status)) return
                if (!intentionalDisconnect) {
                    failConnection(
                        if (status == GATT_ERROR_133) {
                            "Verbindung fehlgeschlagen (GATT-Fehler 133). Bitte erneut verbinden."
                        } else {
                            "Die Bluetooth-Verbindung wurde unterbrochen (Status $status)."
                        },
                    )
                }
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                if (!retryForGatt133(gatt, status)) failConnection(gattStatusMessage(status))
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (gatt !== activeGatt) return
            if (status != BluetoothGatt.GATT_SUCCESS) {
                if (retryForGatt133(gatt, status)) return
                failConnection(gattStatusMessage(status))
                return
            }
            val service = gatt.getService(SERVICE_UUID)
            val characteristic = service?.getCharacteristic(CHARACTERISTIC_UUID)
            if (characteristic == null) {
                failConnection("Das Gerät stellt den erwarteten Dienst FFE0/FFE1 nicht bereit.")
                return
            }
            if ((characteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0) {
                failConnection("Die Daten-Characteristic FFE1 unterstützt keine Benachrichtigungen.")
                return
            }
            writeCharacteristic = characteristic
            if (!gatt.setCharacteristicNotification(characteristic, true)) {
                failConnection("Benachrichtigungen für FFE1 konnten nicht aktiviert werden.")
                return
            }
            val descriptor = characteristic.getDescriptor(CCCD_UUID)
            if (descriptor == null) {
                failConnection("Der Benachrichtigungs-Descriptor 0x2902 fehlt.")
                return
            }
            val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
            } else {
                @Suppress("DEPRECATION")
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                @Suppress("DEPRECATION")
                if (gatt.writeDescriptor(descriptor)) BluetoothGatt.GATT_SUCCESS else BluetoothGatt.GATT_FAILURE
            }
            if (result != BluetoothGatt.GATT_SUCCESS) {
                failConnection("Der Benachrichtigungs-Descriptor konnte nicht geschrieben werden.")
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int,
        ) {
            if (gatt !== activeGatt || descriptor.uuid != CCCD_UUID) return
            if (status == BluetoothGatt.GATT_SUCCESS) {
                retryCount = 0
                lineParser.reset(discardUntilFirstNewline = true)
                mutableState.value = ConnectionState.Connected(connectingName)
            } else {
                if (!retryForGatt133(gatt, status)) failConnection(gattStatusMessage(status))
            }
        }

        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
        ) {
            if (gatt === activeGatt && characteristic.uuid == CHARACTERISTIC_UUID) {
                processNotification(characteristic.value ?: return)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
        ) {
            if (gatt === activeGatt && characteristic.uuid == CHARACTERISTIC_UUID) {
                processNotification(value)
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int,
        ) {
            if (gatt === activeGatt && characteristic.uuid == CHARACTERISTIC_UUID) {
                pendingWrite?.complete(status)
                pendingWrite = null
            }
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun connect(device: BleDevice) {
        withContext(Dispatchers.Main.immediate) {
            disconnect()
            val remote = try {
                adapter?.getRemoteDevice(device.address)
            } catch (_: IllegalArgumentException) {
                null
            } ?: throw IllegalArgumentException("Die Bluetooth-Geräteadresse ist ungültig.")

            connectingDevice = remote
            connectingName = device.name?.takeIf(String::isNotBlank) ?: device.address
            retryCount = 0
            intentionalDisconnect = false
            mutableState.value = ConnectionState.Connecting
            openGatt()
        }

        try {
            withTimeout(CONNECTION_TIMEOUT_MS) {
                state.firstConnectedOrError()
            }
        } catch (_: TimeoutCancellationException) {
            failConnection("Zeitüberschreitung beim Verbinden. Bitte versuche es erneut.")
        }
    }

    @SuppressLint("MissingPermission")
    private fun openGatt() {
        val device = connectingDevice ?: return
        try {
            activeGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                device.connectGatt(context, false, callback, BluetoothDevice.TRANSPORT_LE)
            } else {
                device.connectGatt(context, false, callback)
            }
            if (activeGatt == null) failConnection("Android konnte keine GATT-Verbindung öffnen.")
        } catch (_: SecurityException) {
            failConnection("Für die Bluetooth-Verbindung fehlt die Berechtigung „Geräte in der Nähe“.")
        }
    }

    @SuppressLint("MissingPermission")
    override fun disconnect() {
        emitPendingLine()
        intentionalDisconnect = true
        val gatt = activeGatt
        activeGatt = null
        connectingDevice = null
        writeCharacteristic = null
        pendingWrite?.completeExceptionally(IOException("Die Bluetooth-Verbindung wurde getrennt."))
        pendingWrite = null
        mutableState.value = ConnectionState.Disconnected
        if (gatt != null) {
            try {
                gatt.disconnect()
            } catch (_: SecurityException) {
                // The app may have lost permission after the connection was established.
            } finally {
                gatt.close()
            }
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun write(text: String) {
        val bytes = text.toByteArray(Charsets.UTF_8)
        if (bytes.isEmpty()) return
        try {
            operations.withLock {
                val gatt = activeGatt ?: error("Es besteht keine Bluetooth-Verbindung.")
                val characteristic = writeCharacteristic
                    ?: error("Die Daten-Characteristic FFE1 ist nicht verfügbar.")
                if (mutableState.value !is ConnectionState.Connected) {
                    error("Es besteht keine aktive Bluetooth-Verbindung.")
                }
                for (chunk in bytes.toList().chunked(MAX_WRITE_BYTES).map { it.toByteArray() }) {
                    val completion = CompletableDeferred<Int>()
                    pendingWrite = completion
                    val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        gatt.writeCharacteristic(
                            characteristic,
                            chunk,
                            BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE,
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                        @Suppress("DEPRECATION")
                        characteristic.value = chunk
                        @Suppress("DEPRECATION")
                        if (gatt.writeCharacteristic(characteristic)) BluetoothGatt.GATT_SUCCESS else BluetoothGatt.GATT_FAILURE
                    }
                    if (result != BluetoothGatt.GATT_SUCCESS) {
                        pendingWrite = null
                        error("Schreiben auf FFE1 wurde von Android abgelehnt (Status $result).")
                    }
                    val status = try {
                        withTimeout(WRITE_TIMEOUT_MS) { completion.await() }
                    } catch (exception: TimeoutCancellationException) {
                        pendingWrite = null
                        throw IOException("Zeitüberschreitung beim Schreiben auf FFE1.", exception)
                    }
                    if (status != BluetoothGatt.GATT_SUCCESS) {
                        error("Schreiben auf FFE1 fehlgeschlagen (Status $status).")
                    }
                }
            }
        } catch (exception: SecurityException) {
            throw IOException("Die Bluetooth-Berechtigung wurde vor dem Senden entzogen.", exception)
        }
    }

    private fun processNotification(value: ByteArray) {
        lineParser.feed(value).forEach { mutableLines.tryEmit(it) }
    }

    private fun failConnection(message: String) {
        emitPendingLine()
        val gatt = activeGatt
        activeGatt = null
        writeCharacteristic = null
        pendingWrite?.complete(BluetoothGatt.GATT_FAILURE)
        pendingWrite = null
        mutableState.value = ConnectionState.Error(message)
        try {
            gatt?.close()
        } catch (_: SecurityException) {
            // Closing a stale GATT handle is best effort after permission revocation.
        }
    }

    @SuppressLint("MissingPermission")
    private fun retryForGatt133(gatt: BluetoothGatt, status: Int): Boolean {
        if (status != GATT_ERROR_133 || retryCount != 0 || intentionalDisconnect) return false
        retryCount = 1
        emitPendingLine()
        lineParser.reset(discardUntilFirstNewline = true)
        activeGatt = null
        writeCharacteristic = null
        mutableState.value = ConnectionState.Connecting
        gatt.close()
        mainHandler.postDelayed({ openGatt() }, 400L)
        return true
    }

    private fun emitPendingLine() {
        lineParser.finish()?.let(mutableLines::tryEmit)
    }

    private suspend fun kotlinx.coroutines.flow.StateFlow<ConnectionState>.firstConnectedOrError() {
        val result = first {
            it is ConnectionState.Connected || it is ConnectionState.Error || it == ConnectionState.Disconnected
        }
        when (result) {
            is ConnectionState.Error -> error(result.message)
            ConnectionState.Disconnected -> error("Die Bluetooth-Verbindung wurde abgebrochen.")
            else -> Unit
        }
    }

    private fun gattStatusMessage(status: Int): String = when (status) {
        GATT_ERROR_133 -> "Verbindung fehlgeschlagen (GATT-Fehler 133)."
        else -> "Bluetooth-Kommunikation fehlgeschlagen (Status $status)."
    }

    private companion object {
        const val MAX_WRITE_BYTES = 20
        const val CONNECTION_TIMEOUT_MS = 15_000L
        const val WRITE_TIMEOUT_MS = 3_000L
        const val GATT_ERROR_133 = 133
        val SERVICE_UUID: UUID = UUID.fromString("0000FFE0-0000-1000-8000-00805F9B34FB")
        val CHARACTERISTIC_UUID: UUID = UUID.fromString("0000FFE1-0000-1000-8000-00805F9B34FB")
        val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")
    }
}
