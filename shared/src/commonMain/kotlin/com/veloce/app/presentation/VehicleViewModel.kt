package com.veloce.app.presentation

import com.veloce.app.domain.model.Vehicle
import com.veloce.app.domain.repository.VehicleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VehicleViewModel(private val vehicleRepository: VehicleRepository) {
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _state = MutableStateFlow(VehicleState())
    val state: StateFlow<VehicleState> = _state.asStateFlow()

    fun loadVehicles() {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            vehicleRepository.getVehicles()
                .onSuccess { list ->
                    _state.value = _state.value.copy(isLoading = false, vehicles = list)
                }
                .onFailure { ex ->
                    _state.value = _state.value.copy(isLoading = false, error = ex.message)
                }
        }
    }

    fun createVehicle(name: String, description: String, vehicleType: String) {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            vehicleRepository.createVehicle(name, description, vehicleType)
                .onSuccess {
                    loadVehicles()
                }
                .onFailure { ex ->
                    _state.value = _state.value.copy(isLoading = false, error = ex.message ?: "Fahrzeug konnte nicht erstellt werden")
                }
        }
    }
}

data class VehicleState(
    val vehicles: List<Vehicle> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
