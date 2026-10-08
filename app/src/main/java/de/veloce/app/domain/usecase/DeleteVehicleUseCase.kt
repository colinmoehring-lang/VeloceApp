package de.veloce.app.domain.usecase

import de.veloce.app.domain.repository.VehicleRepository

class DeleteVehicleUseCase(
    private val repository: VehicleRepository,
) {
    suspend operator fun invoke(id: String) = repository.deleteVehicle(id)
}
