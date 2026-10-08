package de.veloce.app.domain.usecase

import de.veloce.app.domain.repository.RideRepository

class StopRideUseCase(
    private val repository: RideRepository,
) {
    suspend operator fun invoke(rideId: String) = repository.stopRide(rideId)
}
