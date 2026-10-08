package de.veloce.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.veloce.app.data.repository.SettingsStore
import de.veloce.app.domain.model.BleDevice
import de.veloce.app.domain.model.ConnectionState
import de.veloce.app.domain.model.ParsedBleLine
import de.veloce.app.domain.model.RideDataSource
import de.veloce.app.domain.repository.BleConnection
import de.veloce.app.domain.repository.BleScanner
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val devices: List<BleDevice> = emptyList(),
    val isScanning: Boolean = false,
    val onlyNamedDevices: Boolean = true,
    val scanError: String? = null,
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val lastDeviceAddress: String? = null,
    val lastDeviceName: String? = null,
    val dataSource: RideDataSource = RideDataSource.SIMULATOR,
    val terminalExpanded: Boolean = false,
    val terminalPaused: Boolean = false,
    val terminalLines: List<ParsedBleLine> = emptyList(),
    val validLineCount: Int = 0,
    val invalidLineCount: Int = 0,
    val latestTelemetry: de.veloce.app.domain.model.TelemetryPoint? = null,
    val writeError: String? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val scanner: BleScanner,
    private val connection: BleConnection,
    private val settingsStore: SettingsStore,
) : ViewModel() {
    private val mutableState = MutableStateFlow(SettingsUiState())
    val state = mutableState.asStateFlow()
    private var scanJob: Job? = null
    private var scanTimeoutJob: Job? = null
    private var pendingDevice: BleDevice? = null

    init {
        viewModelScope.launch {
            scanner.isScanning.collect { scanning ->
                mutableState.update { it.copy(isScanning = scanning) }
            }
        }
        viewModelScope.launch {
            connection.state.collect { connectionState ->
                mutableState.update { it.copy(connectionState = connectionState) }
                if (connectionState is ConnectionState.Connected) {
                    pendingDevice?.let { device ->
                        settingsStore.saveLastConnectedDevice(device.address, connectionState.deviceName)
                        mutableState.update {
                            it.copy(
                                lastDeviceAddress = device.address,
                                lastDeviceName = connectionState.deviceName,
                            )
                        }
                    }
                }
            }
        }
        viewModelScope.launch {
            connection.lines.collect { line ->
                mutableState.update { current ->
                    current.copy(
                        latestTelemetry = line.point ?: current.latestTelemetry,
                        validLineCount = current.validLineCount + if (line.isValid) 1 else 0,
                        invalidLineCount = current.invalidLineCount + if (line.isValid) 0 else 1,
                        terminalLines = if (current.terminalPaused) {
                            current.terminalLines
                        } else {
                            (current.terminalLines + line).takeLast(MAX_TERMINAL_LINES)
                        },
                    )
                }
            }
        }
        viewModelScope.launch {
            settingsStore.lastDeviceAddress.collect { address ->
                mutableState.update { it.copy(lastDeviceAddress = address) }
            }
        }
        viewModelScope.launch {
            settingsStore.lastDeviceName.collect { name ->
                mutableState.update { it.copy(lastDeviceName = name) }
            }
        }
        viewModelScope.launch {
            settingsStore.rideDataSource.collect { source ->
                mutableState.update { it.copy(dataSource = source) }
            }
        }
    }

    fun isBluetoothEnabled(): Boolean = try {
        scanner.isBluetoothEnabled()
    } catch (_: SecurityException) {
        false
    }

    fun startScan() {
        stopScan()
        mutableState.update { it.copy(devices = emptyList(), scanError = null) }
        scanJob = viewModelScope.launch {
            scanner.scan()
                .catch { exception ->
                    if (exception is CancellationException) throw exception
                    mutableState.update {
                        it.copy(scanError = exception.message ?: "Bluetooth-Suche fehlgeschlagen.")
                    }
                }
                .collect { device ->
                    mutableState.update { current ->
                        val updated = (current.devices.filterNot { it.address == device.address } + device)
                            .filter { !current.onlyNamedDevices || !it.name.isNullOrBlank() }
                            .sortedWith(compareByDescending<BleDevice> { it.isPreferred }.thenByDescending { it.rssi })
                        current.copy(devices = updated)
                    }
                }
        }
        scanTimeoutJob = viewModelScope.launch {
            delay(SCAN_DURATION_MS)
            stopScan()
        }
    }

    fun stopScan() {
        scanJob?.cancel()
        scanJob = null
        scanTimeoutJob?.cancel()
        scanTimeoutJob = null
        scanner.stopScan()
    }

    fun setOnlyNamedDevices(onlyNamed: Boolean) {
        mutableState.update { current ->
            current.copy(
                onlyNamedDevices = onlyNamed,
                devices = current.devices
                    .filter { !onlyNamed || !it.name.isNullOrBlank() }
                    .sortedWith(compareByDescending<BleDevice> { it.isPreferred }.thenByDescending { it.rssi }),
            )
        }
    }

    fun connect(device: BleDevice) {
        if (mutableState.value.connectionState is ConnectionState.Connecting) return
        stopScan()
        pendingDevice = device
        clearTerminal()
        mutableState.update { it.copy(connectionState = ConnectionState.Connecting, scanError = null) }
        viewModelScope.launch {
            try {
                connection.connect(device)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                mutableState.update {
                    it.copy(
                        connectionState = ConnectionState.Error(
                            exception.message ?: "Verbindung konnte nicht hergestellt werden.",
                        ),
                    )
                }
            }
        }
    }

    fun connectToLastDevice() {
        val address = mutableState.value.lastDeviceAddress ?: return
        connect(
            BleDevice(
                name = mutableState.value.lastDeviceName,
                address = address,
                rssi = 0,
            ),
        )
    }

    fun disconnect() {
        connection.disconnect()
        pendingDevice = null
    }

    fun setDataSource(source: RideDataSource) {
        viewModelScope.launch {
            settingsStore.setRideDataSource(source)
        }
    }

    fun toggleTerminalExpanded() {
        mutableState.update { it.copy(terminalExpanded = !it.terminalExpanded) }
    }

    fun toggleTerminalPaused() {
        mutableState.update { it.copy(terminalPaused = !it.terminalPaused) }
    }

    fun clearTerminal() {
        mutableState.update {
            it.copy(
                terminalLines = emptyList(),
                validLineCount = 0,
                invalidLineCount = 0,
                latestTelemetry = null,
            )
        }
    }

    fun sendText(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            mutableState.update { it.copy(writeError = null) }
            try {
                connection.write(text)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: IOException) {
                mutableState.update { it.copy(writeError = exception.message ?: "Senden fehlgeschlagen.") }
            } catch (exception: IllegalStateException) {
                mutableState.update { it.copy(writeError = exception.message ?: "Keine aktive Verbindung.") }
            }
        }
    }

    override fun onCleared() {
        stopScan()
        super.onCleared()
    }

    private companion object {
        const val MAX_TERMINAL_LINES = 200
        const val SCAN_DURATION_MS = 10_000L
    }
}

fun ParsedBleLine.displayTimestamp(): String {
    val local = java.time.LocalTime.ofInstant(receivedAt, java.time.ZoneId.systemDefault())
    return "%02d:%02d:%02d.%03d".format(
        local.hour,
        local.minute,
        local.second,
        local.nano / 1_000_000,
    )
}