package de.veloce.app.domain.usecase

import de.veloce.app.domain.model.TelemetryPoint
import de.veloce.app.domain.repository.RideRepository
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class RecordRideUseCaseTest {
    @Test
    fun `does not send point without GPS coordinates`() = runBlocking {
        val repository = FakeRideRepository()
        val point = TelemetryPoint(Instant.EPOCH, 0.0, 0.0, 40.0, 2.0, 0.2)

        RecordRideUseCase(repository)("ride-id", point)

        assertEquals(0, repository.recordedPoints)
    }

    @Test
    fun `sends point with valid GPS coordinates`() = runBlocking {
        val repository = FakeRideRepository()
        val point = TelemetryPoint(Instant.EPOCH, 48.5, 9.0, 40.0, 2.0, 0.2)

        RecordRideUseCase(repository)("ride-id", point)

        assertEquals(1, repository.recordedPoints)
    }

    private class FakeRideRepository : RideRepository {
        var recordedPoints = 0

        override suspend fun startRide(vehicleType: String, vehicleId: String?): String = "ride-id"

        override suspend fun addPoint(rideId: String, point: TelemetryPoint) {
            recordedPoints++
        }

        override suspend fun stopRide(rideId: String) = Unit
    }
}
