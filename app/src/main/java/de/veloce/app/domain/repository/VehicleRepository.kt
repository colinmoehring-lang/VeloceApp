package de.veloce.app.domain.repository

import de.veloce.app.domain.model.Vehicle

interface VehicleRepository {
    suspend fun getVehicles(): List<Vehicle>
    suspend fun createVehicle(
        name: String,
        description: String,
        vehicleType: String,
        imageData: String?,
    ): Vehicle

    suspend fun updateVehicle(
        id: String,
        name: String,
        description: String,
        vehicleType: String,
        imageData: String?,
    ): Vehicle

    suspend fun deleteVehicle(id: String)
}
