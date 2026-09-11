package com.veloce.app.presentation

import com.veloce.app.domain.model.Ride
import com.veloce.app.domain.model.UserRecords
import com.veloce.app.domain.model.Vehicle
import com.veloce.app.domain.repository.RideRepository
import com.veloce.app.domain.repository.UserRepository
import com.veloce.app.domain.repository.VehicleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val userRepository: UserRepository,
    private val vehicleRepository: VehicleRepository,
    private val rideRepository: RideRepository
) {
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    fun loadData() {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            // Fetch User Records
            userRepository.getUserRecords()
                .onSuccess { records ->
                    _state.value = _state.value.copy(records = records)
                }
                .onFailure { ex ->
                    _state.value = _state.value.copy(error = ex.message)
                }

            // Fetch Vehicles
            vehicleRepository.getVehicles()
                .onSuccess { vehicles ->
                    _state.value = _state.value.copy(vehicles = vehicles)
                }

            // Fetch Rides
            rideRepository.getRides()
                .onSuccess { rides ->
                    _state.value = _state.value.copy(rides = rides)
                }

            _state.value = _state.value.copy(isLoading = false)
        }
    }
}

data class HomeState(
    val records: UserRecords? = UserRecords(130.0, 48.0, 45.0, 2.18), // Default mock matching user sketch
    val vehicles: List<Vehicle> = listOf(
        Vehicle("1", "u1", "BMW S1000RR", "199 PS Superbike", "Motorrad"),
        Vehicle("2", "u1", "Porsche GT3 RS", "525 PS Track Car", "Auto")
    ),
    val rides: List<Ride> = listOf(
        Ride(
            rideId = "r1",
            vehicleType = "Motorrad",
            duration = 1840.0,
            distance = 42.5,
            highestSpeed = 142.0,
            highestLeanAngleLeft = 48.0,
            highestLeanAngleRight = 44.0,
            highestGForce = 2.18
        )
    ),
    val isLoading: Boolean = false,
    val error: String? = null
)
