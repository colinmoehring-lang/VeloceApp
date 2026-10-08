package de.veloce.app.domain.usecase

import de.veloce.app.domain.model.Vehicle
import de.veloce.app.domain.repository.VehicleRepository

class GetVehiclesUseCase(
    private val repository: VehicleRepository,
) {
    suspend operator fun invoke(): List<Vehicle> = repository.getVehicles()
}
