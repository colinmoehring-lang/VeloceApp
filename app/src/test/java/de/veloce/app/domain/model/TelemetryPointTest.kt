package de.veloce.app.domain.model

import java.time.Instant
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TelemetryPointTest {
    @Test
    fun `accepts finite coordinates within latitude and longitude ranges`() {
        assertTrue(point(90.0, 180.0).hasValidCoordinates)
        assertTrue(point(-90.0, -180.0).hasValidCoordinates)
        assertTrue(point(0.0, 12.0).hasValidCoordinates)
    }

    @Test
    fun `rejects no-fix sentinel and out-of-range or non-finite coordinates`() {
        assertFalse(point(0.0, 0.0).hasValidCoordinates)
        assertFalse(point(90.1, 10.0).hasValidCoordinates)
        assertFalse(point(10.0, -180.1).hasValidCoordinates)
        assertFalse(point(Double.NaN, 10.0).hasValidCoordinates)
        assertFalse(point(10.0, Double.POSITIVE_INFINITY).hasValidCoordinates)
    }

    private fun point(latitude: Double, longitude: Double) = TelemetryPoint(
        timestamp = Instant.EPOCH,
        latitude = latitude,
        longitude = longitude,
        speedKmh = 0.0,
        leanDegrees = 0.0,
        gForce = 0.0,
    )
}
