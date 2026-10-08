package de.veloce.app.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import de.veloce.app.domain.model.BleDevice
import de.veloce.app.domain.repository.BleScanner
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class AndroidBleScanner @Inject constructor(
    @ApplicationContext private val context: Context,
) : BleScanner {
    private val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
    private val adapter: BluetoothAdapter? get() = bluetoothManager?.adapter
    private val mutableIsScanning = MutableStateFlow(false)
    override val isScanning = mutableIsScanning.asStateFlow()
    @Volatile private var activeScanner: BluetoothLeScanner? = null
    @Volatile private var activeCallback: ScanCallback? = null
    @Volatile private var activeScope: ProducerScope<BleDevice>? = null

    @SuppressLint("MissingPermission")
    override fun isBluetoothEnabled(): Boolean = adapter?.isEnabled == true

    @SuppressLint("MissingPermission")
    override fun scan(): Flow<BleDevice> = callbackFlow {
        val leScanner: BluetoothLeScanner = try {
            adapter?.bluetoothLeScanner
        } catch (exception: SecurityException) {
            close(IllegalStateException("Für die Bluetooth-Suche fehlen Berechtigungen.", exception))
            return@callbackFlow
        }
            ?: run {
                close(IllegalStateException("Bluetooth ist auf diesem Gerät nicht verfügbar."))
                return@callbackFlow
            }
        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                try {
                    val device = result.device
                    val serviceFound = result.scanRecord?.serviceUuids
                        ?.any { it.uuid == SERVICE_UUID } == true
                    val name = result.scanRecord?.deviceName ?: device.name
                    trySend(
                        BleDevice(
                            name = name,
                            address = device.address,
                            rssi = result.rssi,
                            hasVeloceService = serviceFound,
                        ),
                    )
                } catch (_: SecurityException) {
                    close(IllegalStateException("Die Bluetooth-Berechtigung wurde während der Suche entzogen."))
                }
            }

            override fun onScanFailed(errorCode: Int) {
                close(IllegalStateException(scanErrorMessage(errorCode)))
            }
        }

        try {
            activeScanner = leScanner
            activeCallback = callback
            activeScope = this
            leScanner.startScan(
                null,
                ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build(),
                callback,
            )
            mutableIsScanning.value = true
        } catch (exception: SecurityException) {
            activeScanner = null
            activeCallback = null
            activeScope = null
            mutableIsScanning.value = false
            close(IllegalStateException("Für die Bluetooth-Suche fehlen Berechtigungen.", exception))
            return@callbackFlow
        } catch (exception: IllegalStateException) {
            activeScanner = null
            activeCallback = null
            activeScope = null
            mutableIsScanning.value = false
            close(IllegalStateException("Bluetooth-Suche konnte nicht gestartet werden.", exception))
            return@callbackFlow
        }

        awaitClose {
            try {
                leScanner.stopScan(callback)
            } catch (_: SecurityException) {
                // The scan has already been stopped by Android after permission revocation.
            } finally {
                if (activeCallback === callback) {
                    activeScanner = null
                    activeCallback = null
                    activeScope = null
                }
                mutableIsScanning.value = false
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun stopScan() {
        val scanner = activeScanner
        val callback = activeCallback
        try {
            if (scanner != null && callback != null) scanner.stopScan(callback)
        } catch (_: SecurityException) {
            activeScope?.close(IllegalStateException("Für die Bluetooth-Suche fehlt die Berechtigung."))
        } finally {
            activeScope?.close()
            mutableIsScanning.value = false
        }
    }

    private fun scanErrorMessage(code: Int): String = when (code) {
        ScanCallback.SCAN_FAILED_ALREADY_STARTED -> "Eine Bluetooth-Suche läuft bereits."
        ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> "Bluetooth-Suche konnte nicht registriert werden."
        ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED -> "BLE-Suche wird von diesem Gerät nicht unterstützt."
        ScanCallback.SCAN_FAILED_INTERNAL_ERROR -> "Interner Bluetooth-Fehler bei der Suche."
        else -> "Bluetooth-Suche fehlgeschlagen (Fehler $code)."
    }

    private companion object {
        val SERVICE_UUID: UUID = UUID.fromString("0000FFE0-0000-1000-8000-00805F9B34FB")
    }
}
