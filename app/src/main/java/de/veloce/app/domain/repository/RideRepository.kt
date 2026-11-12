package de.veloce.app.domain.repository

import de.veloce.app.domain.model.MapPoint
import de.veloce.app.domain.model.RideHistoryItem
import de.veloce.app.domain.model.TelemetryPoint

interface RideRepository {
    suspend fun startRide(vehicleType: String, vehicleId: String? = null): String
    suspend fun addPoint(rideId: String, point: TelemetryPoint)
    suspend fun stopRide(rideId: String)
    suspend fun getRides(): List<RideHistoryItem>
    suspend fun getRideRoute(rideId: String): List<MapPoint>
}