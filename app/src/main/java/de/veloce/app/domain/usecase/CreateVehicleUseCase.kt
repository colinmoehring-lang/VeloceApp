package de.veloce.app.domain.usecase

import de.veloce.app.domain.model.Vehicle
import de.veloce.app.domain.repository.VehicleRepository

class CreateVehicleUseCase(
    private val repository: VehicleRepository,
) {
    suspend operator fun invoke(
        name: String,
        description: String,
        vehicleType: String,
        imageData: String?,
    ): Vehicle {
        require(name.isNotBlank()) { "Bitte gib einen Namen für das Fahrzeug ein." }
        return repository.createVehicle(name.trim(), description.trim(), vehicleType, imageData)
    }
}
