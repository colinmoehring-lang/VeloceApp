package de.veloce.app.domain.model

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class LeanCalibrationTest {
    private val timestamp = Instant.parse("2026-10-08T09:00:00Z")

    private fun point(leanDegrees: Double) = TelemetryPoint(
        timestamp = timestamp,
        latitude = 48.5216,
        longitude = 9.0576,
        speedKmh = 42.0,
        leanDegrees = leanDegrees,
        gForce = 0.15,
    )

    @Test
    fun `offset of minus 20 maps raw 70 to 90 degrees`() {
        assertEquals(90.0, point(70.0).withLeanOffset(-20.0).leanDegrees, 0.0)
    }

    @Test
    fun `offset of minus 20 maps raw -110 to -90 degrees`() {
        assertEquals(-90.0, point(-110.0).withLeanOffset(-20.0).leanDegrees, 0.0)
    }

    @Test
    fun `offset of minus 20 maps raw -20 to zero`() {
        assertEquals(0.0, point(-20.0).withLeanOffset(-20.0).leanDegrees, 0.0)
    }

    @Test
    fun `positive offset shifts lean into the negative direction`() {
        assertEquals(25.0, point(45.0).withLeanOffset(20.0).leanDegrees, 0.0)
    }

    @Test
    fun `zero offset keeps the point untouched`() {
        val original = point(-18.5)
        assertSame(original, original.withLeanOffset(0.0))
    }

    @Test
    fun `offset only changes lean degrees`() {
        val calibrated = point(12.5).withLeanOffset(-20.0)
        assertEquals(32.5, calibrated.leanDegrees, 0.0)
        assertEquals(timestamp, calibrated.timestamp)
        assertEquals(48.5216, calibrated.latitude, 0.0)
        assertEquals(9.0576, calibrated.longitude, 0.0)
        assertEquals(42.0, calibrated.speedKmh, 0.0)
        assertEquals(0.15, calibrated.gForce, 0.0)
    }
}