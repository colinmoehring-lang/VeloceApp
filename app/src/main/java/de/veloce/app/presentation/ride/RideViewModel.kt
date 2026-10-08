package de.veloce.app.presentation.ride

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.veloce.app.BuildConfig
import de.veloce.app.data.ble.BleTelemetrySource
import de.veloce.app.data.repository.SettingsStore
import de.veloce.app.data.simulator.SimulatedTelemetrySource
import de.veloce.app.data.remote.ApiException
import de.veloce.app.presentation.components.MapPoint
import de.veloce.app.domain.model.TelemetryPoint
import de.veloce.app.domain.model.ConnectionState
import de.veloce.app.domain.model.RideDataSource
import de.veloce.app.domain.repository.BleConnection
import de.veloce.app.domain.usecase.RecordRideUseCase
import de.veloce.app.domain.usecase.StartRideUseCase
import de.veloce.app.domain.usecase.StopRideUseCase
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException

data class RideUiState(
    val isRecording: Boolean = false,
    val isStarting: Boolean = false,
    val isStopping: Boolean = false,
    val stopPending: Boolean = false,
    val vehicleType: String = "Motorcycle",
    val dataSource: RideDataSource = RideDataSource.SIMULATOR,
    val isLoadingSettings: Boolean = true,
    val isArduinoConnected: Boolean = false,
    val currentPoint: TelemetryPoint? = null,
    val route: List<MapPoint> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class RideViewModel @Inject constructor(
    private val startRide: StartRideUseCase,
    private val recordRide: RecordRideUseCase,
    private val stopRide: StopRideUseCase,
    private val simulatorSource: SimulatedTelemetrySource,
    private val bleTelemetrySource: BleTelemetrySource,
    private val bleConnection: BleConnection,
    private val settingsStore: SettingsStore,
) : ViewModel() {
    private val mutableState = MutableStateFlow(RideUiState())
    val state = mutableState.asStateFlow()
    val mapboxToken: String = BuildConfig.MAPBOX_TOKEN

    private var rideId: String? = null
    private var telemetryJob: Job? = null

    init {
        viewModelScope.launch {
            settingsStore.rideDataSource.collect { dataSource ->
                mutableState.update { it.copy(dataSource = dataSource, isLoadingSettings = false) }
            }
        }
        viewModelScope.launch {
            bleConnection.state.collect { connectionState ->
                mutableState.update {
                    it.copy(isArduinoConnected = connectionState is ConnectionState.Connected)
                }
            }
        }
    }

    fun selectVehicleType(vehicleType: String) {
        if (vehicleType == "Car" || vehicleType == "Motorcycle") {
            mutableState.update { it.copy(vehicleType = vehicleType) }
        }
    }

    fun startRecording() {
        if (mutableState.value.isStarting || mutableState.value.isRecording) return
        val source = when (mutableState.value.dataSource) {
            RideDataSource.SIMULATOR -> simulatorSource
            RideDataSource.ARDUINO -> {
                if (!mutableState.value.isArduinoConnected) {
                    mutableState.update {
                        it.copy(error = "Verbinde zuerst dein Arduino-Gerät in den Einstellungen.")
                    }
                    return
                }
                bleTelemetrySource
            }
        }
        mutableState.update { it.copy(isStarting = true, error = null, stopPending = false) }
        viewModelScope.launch {
            try {
                val id = startRide(mutableState.value.vehicleType)
                rideId = id
                mutableState.update {
                    it.copy(
                        isStarting = false,
                        isRecording = true,
                        currentPoint = null,
                        route = emptyList(),
                        error = null,
                    )
                }
                telemetryJob = viewModelScope.launch {
                    source.points().collect { point ->
                        mutableState.update { previous ->
                            previous.copy(
                                currentPoint = point,
                                route = if (point.hasValidCoordinates) {
                                    previous.route + MapPoint(point.latitude, point.longitude)
                                } else {
                                    previous.route
                                },
                            )
                        }
                        if (point.hasValidCoordinates) {
                            try {
                                recordRide(id, point)
                            } catch (exception: CancellationException) {
                                throw exception
                            } catch (exception: ApiException) {
                                mutableState.update { it.copy(error = exception.toDisplayMessage()) }
                            } catch (_: SerializationException) {
                                mutableState.update {
                                    it.copy(error = "Ein Messpunkt konnte nicht an den Server übertragen werden.")
                                }
                            } catch (_: IOException) {
                                mutableState.update {
                                    it.copy(error = "Keine Verbindung – ein Messpunkt wurde nicht übertragen.")
                                }
                            }
                        }
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: ApiException) {
                mutableState.update {
                    it.copy(isStarting = false, error = exception.toDisplayMessage())
                }
            } catch (_: IOException) {
                mutableState.update {
                    it.copy(isStarting = false, error = "Fahrt konnte nicht gestartet werden. Server nicht erreichbar.")
                }
            }
        }
    }

    fun stopRecording() {
        if (mutableState.value.isStopping) return
        val id = rideId ?: return
        telemetryJob?.cancel()
        telemetryJob = null
        mutableState.update { it.copy(isRecording = false, isStopping = true, error = null) }
        finishRide(id)
    }

    fun retryStop() {
        if (mutableState.value.isStopping) return
        val id = rideId ?: return
        mutableState.update { it.copy(isStopping = true, error = null) }
        finishRide(id)
    }

    private fun finishRide(id: String) {
        viewModelScope.launch {
            try {
                stopRide(id)
                rideId = null
                mutableState.update {
                    it.copy(isStopping = false, stopPending = false, isRecording = false, error = null)
                }
            } catch (exception: ApiException) {
                mutableState.update {
                    it.copy(
                        isStopping = false,
                        stopPending = true,
                        error = "Fahrt konnte nicht abgeschlossen werden: ${exception.message}",
                    )
                }
            } catch (_: IOException) {
                mutableState.update {
                    it.copy(
                        isStopping = false,
                        stopPending = true,
                        error = "Fahrt konnte nicht abgeschlossen werden. Prüfe die Verbindung und versuche es erneut.",
                    )
                }
            }
        }
    }

    private fun ApiException.toDisplayMessage(): String =
        if (message.isBlank()) "Serverfehler ($statusCode)." else "Serverfehler ($statusCode): $message"

    override fun onCleared() {
        telemetryJob?.cancel()
        super.onCleared()
    }
}