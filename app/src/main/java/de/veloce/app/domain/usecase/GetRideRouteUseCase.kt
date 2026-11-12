package de.veloce.app.domain.usecase

import de.veloce.app.domain.model.MapPoint
import de.veloce.app.domain.repository.RideRepository

class GetRideRouteUseCase(
    private val repository: RideRepository,
) {
    suspend operator fun invoke(rideId: String): List<MapPoint> =
        repository.getRideRoute(rideId)
}
