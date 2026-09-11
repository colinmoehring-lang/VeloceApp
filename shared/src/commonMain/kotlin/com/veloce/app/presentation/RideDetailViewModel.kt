package com.veloce.app.presentation

import com.veloce.app.domain.model.Ride
import com.veloce.app.domain.model.RidePointDetail
import com.veloce.app.domain.repository.RideRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RideDetailViewModel(private val rideRepository: RideRepository) {
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _state = MutableStateFlow(RideDetailState())
    val state: StateFlow<RideDetailState> = _state.asStateFlow()

    fun loadRide(rideId: String) {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            rideRepository.getRideById(rideId)
                .onSuccess { ride ->
                    _state.value = _state.value.copy(ride = ride)
                }

            rideRepository.getRideCoordinates(rideId)
                .onSuccess { coords ->
                    val parsedCoords = coords.mapNotNull { str ->
                        val parts = str.split(",")
                        if (parts.size == 2) {
                            val lat = parts[0].toDoubleOrNull()
                            val lng = parts[1].toDoubleOrNull()
                            if (lat != null && lng != null) Pair(lat, lng) else null
                        } else null
                    }
                    _state.value = _state.value.copy(coordinates = parsedCoords)
                }

            _state.value = _state.value.copy(isLoading = false)
        }
    }

    fun scrubToPoint(index: Int) {
        val coords = _state.value.coordinates
        if (index in coords.indices) {
            _state.value = _state.value.copy(selectedIndex = index)
            val coord = coords[index]
            // Calculate interpolated stats for demo/playback scrubbing
            val total = coords.size.coerceAtLeast(1)
            val progress = index.toDouble() / total
            val ride = _state.value.ride
            
            val speed = ((ride?.highestSpeed ?: 120.0) * (0.4 + 0.6 * kotlin.math.sin(progress * Math.PI * 3))).coerceAtLeast(0.0)
            val lean = ((ride?.highestLeanAngleLeft ?: 45.0) * kotlin.math.sin(progress * Math.PI * 4))
            val g = (1.0 + 1.2 * kotlin.math.abs(kotlin.math.sin(progress * Math.PI * 2)))

            _state.value = _state.value.copy(
                scrubDetail = RidePointDetail(
                    ridePointId = "p_$index",
                    speed = (speed * 10).toInt() / 10.0,
                    gForce = (g * 100).toInt() / 100.0,
                    lean = (lean * 10).toInt() / 10.0,
                    coordinate = "${coord.first},${coord.second}"
                )
            )
        }
    }
}

data class RideDetailState(
    val ride: Ride? = null,
    val coordinates: List<Pair<Double, Double>> = listOf(
        Pair(51.1234, 6.8421),
        Pair(51.1245, 6.8435),
        Pair(51.1260, 6.8450),
        Pair(51.1275, 6.8480),
        Pair(51.1290, 6.8510)
    ),
    val selectedIndex: Int = 0,
    val scrubDetail: RidePointDetail? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
