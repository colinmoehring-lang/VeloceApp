package de.veloce.app.data.repository

import de.veloce.app.data.mapper.toDomain
import de.veloce.app.data.remote.VeloceApi
import de.veloce.app.data.remote.toApiException
import de.veloce.app.data.remote.dto.CreateVehicleRequestDto
import de.veloce.app.data.remote.dto.UpdateVehicleRequestDto
import de.veloce.app.domain.model.Vehicle
import de.veloce.app.domain.repository.VehicleRepository
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VehicleRepositoryImpl @Inject constructor(
    private val api: VeloceApi,
) : VehicleRepository {
    override suspend fun getVehicles(): List<Vehicle> = callApi {
        api.getVehicles().map { it.toDomain() }
    }

    override suspend fun createVehicle(
        name: String,
        description: String,
        vehicleType: String,
        imageData: String?,
    ): Vehicle = callApi {
        api.createVehicle(CreateVehicleRequestDto(name, description, vehicleType, imageData)).toDomain()
    }

    override suspend fun updateVehicle(
        id: String,
        name: String,
        description: String,
        vehicleType: String,
        imageData: String?,
    ): Vehicle = callApi {
        api.updateVehicle(id, UpdateVehicleRequestDto(name, description, vehicleType, imageData)).toDomain()
    }

    override suspend fun deleteVehicle(id: String) {
        callApi {
            val response = api.deleteVehicle(id)
            if (!response.isSuccessful) throw HttpException(response)
        }
    }

    private suspend fun <T> callApi(block: suspend () -> T): T = try {
        block()
    } catch (exception: HttpException) {
        throw exception.toApiException()
    }
}
