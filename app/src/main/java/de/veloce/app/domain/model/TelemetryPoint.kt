package de.veloce.app.domain.model

import java.time.Instant

data class TelemetryPoint(
    val timestamp: Instant,
    val latitude: Double,
    val longitude: Double,
    val speedKmh: Double,
    val leanDegrees: Double,
    val gForce: Double,
) {
    val hasValidCoordinates: Boolean
        get() = latitude.isFinite() &&
            longitude.isFinite() &&
            latitude in -90.0..90.0 &&
            longitude in -180.0..180.0 &&
            (latitude != 0.0 || longitude != 0.0)
}
