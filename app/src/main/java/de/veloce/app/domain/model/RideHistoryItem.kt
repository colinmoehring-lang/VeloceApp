package de.veloce.app.domain.model

import java.time.Instant

data class RideHistoryItem(
    val id: String,
    val vehicleType: String,
    val startedAt: Instant,
    val durationSeconds: Double,
    val distanceMeters: Double,
    val averageSpeedKmh: Double,
    val highestSpeedKmh: Double,
)
