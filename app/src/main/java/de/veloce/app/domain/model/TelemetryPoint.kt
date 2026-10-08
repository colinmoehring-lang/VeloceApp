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
        get() = latitude != 0.0 || longitude != 0.0
}
