package com.veloce.app.presentation

import com.veloce.app.domain.model.Ride
import com.veloce.app.domain.model.RidePoint
import com.veloce.app.domain.model.TelemetryData
import com.veloce.app.domain.repository.BluetoothRepository
import com.veloce.app.domain.repository.RideRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RideViewModel(
    private val rideRepository: RideRepository,
    private val bluetoothRepository: BluetoothRepository
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private var telemetryJob: Job? = null
    private var timerJob: Job? = null

    private val _state = MutableStateFlow(RideState())
    val state: StateFlow<RideState> = _state.asStateFlow()

    init {
        // Collect live telemetry for HUD preview
        scope.launch {
            bluetoothRepository.getTelemetryStream().collect { data ->
                _state.value = _state.value.copy(currentTelemetry = data)
                if (_state.value.isRideActive) {
                    val newTopSpeed = maxOf(_state.value.topSpeed, data.spd)
                    val newMaxG = maxOf(_state.value.maxGForce, data.g)
                    val newMaxLean = maxOf(_state.value.maxLean, kotlin.math.abs(data.roll))
                    _state.value = _state.value.copy(
                        topSpeed = newTopSpeed,
                        maxGForce = newMaxG,
                        maxLean = newMaxLean
                    )
                }
            }
        }
    }

    fun startRide(vehicleType: String, vehicleId: String?) {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            rideRepository.startRide(vehicleType, vehicleId)
                .onSuccess { rideId ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isRideActive = true,
                        activeRideId = rideId,
                        vehicleType = vehicleType,
                        selectedVehicleId = vehicleId,
                        elapsedSeconds = 0,
                        distanceKm = 0.0,
                        topSpeed = 0.0,
                        maxGForce = 0.0,
                        maxLean = 0.0
                    )
                    startTimerAndStreaming(rideId)
                }
                .onFailure { ex ->
                    _state.value = _state.value.copy(isLoading = false, error = ex.message ?: "Fahrt konnte nicht gestartet werden")
                }
        }
    }

    private fun startTimerAndStreaming(rideId: String) {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (_state.value.isRideActive) {
                delay(1000)
                _state.value = _state.value.copy(
                    elapsedSeconds = _state.value.elapsedSeconds + 1,
                    distanceKm = _state.value.distanceKm + (_state.value.currentTelemetry.spd / 3600.0)
                )
            }
        }

        telemetryJob?.cancel()
        telemetryJob = scope.launch {
            while (_state.value.isRideActive) {
                delay(500) // 500ms interval requirement
                val cur = _state.value.currentTelemetry
                val point = RidePoint(
                    timestamp = "2026-09-11T20:20:00Z",
                    coordinate = "${cur.lat},${cur.lng}",
                    speed = cur.spd,
                    gForce = cur.g,
                    lean = if (_state.value.vehicleType == "Auto") 0.0 else cur.roll
                )
                rideRepository.addRidePoint(rideId, point)
            }
        }
    }

    fun stopRide() {
        val rideId = _state.value.activeRideId ?: return
        scope.launch {
            _state.value = _state.value.copy(isLoading = true)
            timerJob?.cancel()
            telemetryJob?.cancel()

            rideRepository.stopRide(rideId)
                .onSuccess { finishedRide ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isRideActive = false,
                        activeRideId = null,
                        lastFinishedRide = finishedRide
                    )
                }
                .onFailure { ex ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isRideActive = false,
                        activeRideId = null,
                        error = ex.message
                    )
                }
        }
    }
}

data class RideState(
    val isRideActive: Boolean = false,
    val activeRideId: String? = null,
    val vehicleType: String = "Motorrad",
    val selectedVehicleId: String? = null,
    val currentTelemetry: TelemetryData = TelemetryData(),
    val elapsedSeconds: Int = 0,
    val distanceKm: Double = 0.0,
    val topSpeed: Double = 0.0,
    val maxGForce: Double = 0.0,
    val maxLean: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastFinishedRide: Ride? = null
)
