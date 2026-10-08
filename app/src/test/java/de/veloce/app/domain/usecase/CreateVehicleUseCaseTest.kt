package de.veloce.app.domain.usecase

import de.veloce.app.domain.model.Vehicle
import de.veloce.app.domain.repository.VehicleRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CreateVehicleUseCaseTest {
    @Test
    fun `trims fields and delegates vehicle creation`() = runBlocking {
        val repository = FakeVehicleRepository()

        CreateVehicleUseCase(repository)(
            name = "  Tourer  ",
            description = "  Pendlerfahrzeug  ",
            vehicleType = "Motorcycle",
            imageData = null,
        )

        assertEquals("Tourer", repository.createdName)
        assertEquals("Pendlerfahrzeug", repository.createdDescription)
        assertEquals("Motorcycle", repository.createdType)
    }

    @Test
    fun `rejects blank vehicle name`() = runBlocking {
        val repository = FakeVehicleRepository()

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                CreateVehicleUseCase(repository)("", "", "Car", null)
            }
        }
        assertEquals(null, repository.createdName)
    }

    private class FakeVehicleRepository : VehicleRepository {
        var createdName: String? = null
        var createdDescription: String? = null
        var createdType: String? = null

        override suspend fun getVehicles(): List<Vehicle> = emptyList()

        override suspend fun createVehicle(
            name: String,
            description: String,
            vehicleType: String,
            imageData: String?,
        ): Vehicle {
            createdName = name
            createdDescription = description
            createdType = vehicleType
            return Vehicle("vehicle-id", name, description, vehicleType, imageData)
        }

        override suspend fun updateVehicle(
            id: String,
            name: String,
            description: String,
            vehicleType: String,
            imageData: String?,
        ): Vehicle = error("Not used in this test")

        override suspend fun deleteVehicle(id: String) = error("Not used in this test")
    }
}
