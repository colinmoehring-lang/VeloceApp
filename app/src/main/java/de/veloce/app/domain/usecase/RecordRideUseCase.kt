package de.veloce.app.domain.usecase

import de.veloce.app.domain.model.TelemetryPoint
import de.veloce.app.domain.repository.RideRepository

class RecordRideUseCase(
    private val repository: RideRepository,
) {
    suspend operator fun invoke(rideId: String, point: TelemetryPoint) {
        if (point.hasValidCoordinates) repository.addPoint(rideId, point)
    }
}