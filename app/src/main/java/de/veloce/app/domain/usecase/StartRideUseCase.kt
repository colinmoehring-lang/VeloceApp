package de.veloce.app.domain.usecase

import de.veloce.app.domain.repository.RideRepository

class StartRideUseCase(
    private val repository: RideRepository,
) {
    suspend operator fun invoke(vehicleType: String, vehicleId: String? = null): String =
        repository.startRide(vehicleType, vehicleId)
}