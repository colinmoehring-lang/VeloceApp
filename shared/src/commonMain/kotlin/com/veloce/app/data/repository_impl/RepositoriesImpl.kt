package com.veloce.app.data.repository_impl

import com.veloce.app.data.remote.HttpClientProvider
import com.veloce.app.domain.model.*
import com.veloce.app.domain.repository.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
private data class LogInRequest(val userName: String, val password: String)

@Serializable
private data class SignUpRequest(val userName: String, val password: String)

@Serializable
private data class CreateVehicleRequest(
    val name: String?,
    val description: String?,
    val vehicleType: String,
    val imageData: String? = null
)

@Serializable
private data class StartRideRequest(
    val vehicleType: String,
    val vehicleId: String?
)

@Serializable
private data class StartRideResponse(
    val rideId: String
)

class AuthRepositoryImpl : AuthRepository {
    private var token: String? = null

    override suspend fun login(username: String, password: String): Result<AuthTokens> = runCatching {
        val response = HttpClientProvider.client.post(HttpClientProvider.buildUrl("Auth/login")) {
            setBody(LogInRequest(username, password))
        }
        if (response.status.isSuccess()) {
            val tokens = response.body<AuthTokens>()
            saveToken(tokens.accessToken)
            tokens
        } else {
            throw Exception("Login failed: ${response.status.value}")
        }
    }

    override suspend fun signUp(username: String, password: String): Result<String> = runCatching {
        val response = HttpClientProvider.client.post(HttpClientProvider.buildUrl("Auth/sign-up")) {
            setBody(SignUpRequest(username, password))
        }
        if (response.status.isSuccess()) {
            "Sign up successful"
        } else {
            throw Exception("Sign up failed: ${response.status.value}")
        }
    }

    override fun getSavedToken(): String? = token ?: HttpClientProvider.authToken

    override fun saveToken(token: String) {
        this.token = token
        HttpClientProvider.authToken = token
    }

    override fun logout() {
        this.token = null
        HttpClientProvider.authToken = null
    }
}

class UserRepositoryImpl : UserRepository {
    override suspend fun getUserRecords(): Result<UserRecords> = runCatching {
        val response = HttpClientProvider.client.get(HttpClientProvider.buildUrl("User/records"))
        if (response.status.isSuccess()) {
            response.body<UserRecords>()
        } else {
            throw Exception("Failed to fetch records: ${response.status.value}")
        }
    }
}

class VehicleRepositoryImpl : VehicleRepository {
    override suspend fun getVehicles(): Result<List<Vehicle>> = runCatching {
        val response = HttpClientProvider.client.get(HttpClientProvider.buildUrl("Vehicle"))
        if (response.status.isSuccess()) {
            response.body<List<Vehicle>>()
        } else {
            throw Exception("Failed to fetch vehicles: ${response.status.value}")
        }
    }

    override suspend fun getVehicleById(id: String): Result<Vehicle> = runCatching {
        val response = HttpClientProvider.client.get(HttpClientProvider.buildUrl("Vehicle/$id"))
        if (response.status.isSuccess()) {
            response.body<Vehicle>()
        } else {
            throw Exception("Vehicle not found")
        }
    }

    override suspend fun createVehicle(name: String?, description: String?, vehicleType: String, imageData: String?): Result<Vehicle> = runCatching {
        val response = HttpClientProvider.client.post(HttpClientProvider.buildUrl("Vehicle")) {
            setBody(CreateVehicleRequest(name, description, vehicleType, imageData))
        }
        if (response.status.isSuccess()) {
            response.body<Vehicle>()
        } else {
            throw Exception("Failed to create vehicle: ${response.status.value}")
        }
    }

    override suspend fun updateVehicle(id: String, name: String?, description: String?, vehicleType: String): Result<Vehicle> = runCatching {
        val response = HttpClientProvider.client.put(HttpClientProvider.buildUrl("Vehicle/$id")) {
            setBody(CreateVehicleRequest(name, description, vehicleType))
        }
        if (response.status.isSuccess()) {
            response.body<Vehicle>()
        } else {
            throw Exception("Failed to update vehicle")
        }
    }

    override suspend fun deleteVehicle(id: String): Result<Boolean> = runCatching {
        val response = HttpClientProvider.client.delete(HttpClientProvider.buildUrl("Vehicle/$id"))
        response.status.isSuccess()
    }
}

class RideRepositoryImpl : RideRepository {
    override suspend fun getRides(): Result<List<Ride>> = runCatching {
        val response = HttpClientProvider.client.get(HttpClientProvider.buildUrl("Ride"))
        if (response.status.isSuccess()) {
            response.body<List<Ride>>()
        } else {
            throw Exception("Failed to fetch rides: ${response.status.value}")
        }
    }

    override suspend fun getRideById(id: String): Result<Ride> = runCatching {
        val response = HttpClientProvider.client.get(HttpClientProvider.buildUrl("Ride/$id"))
        if (response.status.isSuccess()) {
            response.body<Ride>()
        } else {
            throw Exception("Ride not found")
        }
    }

    override suspend fun startRide(vehicleType: String, vehicleId: String?): Result<String> = runCatching {
        val response = HttpClientProvider.client.post(HttpClientProvider.buildUrl("Ride/start")) {
            setBody(StartRideRequest(vehicleType, vehicleId))
        }
        if (response.status.isSuccess()) {
            val res = response.body<StartRideResponse>()
            res.rideId
        } else {
            throw Exception("Failed to start ride: ${response.status.value}")
        }
    }

    override suspend fun addRidePoint(rideId: String, point: RidePoint): Result<Unit> = runCatching {
        val response = HttpClientProvider.client.post(HttpClientProvider.buildUrl("Ride/$rideId/point")) {
            setBody(point)
        }
        if (!response.status.isSuccess()) {
            throw Exception("Failed to add ride point")
        }
    }

    override suspend fun stopRide(rideId: String): Result<Ride> = runCatching {
        val response = HttpClientProvider.client.post(HttpClientProvider.buildUrl("Ride/$rideId/stop"))
        if (response.status.isSuccess()) {
            response.body<Ride>()
        } else {
            throw Exception("Failed to stop ride")
        }
    }

    override suspend fun getRideCoordinates(rideId: String): Result<List<String>> = runCatching {
        val response = HttpClientProvider.client.get(HttpClientProvider.buildUrl("RidePoint/all/$rideId"))
        if (response.status.isSuccess()) {
            response.body<List<String>>()
        } else {
            throw Exception("Failed to fetch ride coordinates")
        }
    }

    override suspend fun getRidePointDetail(ridePointId: String): Result<RidePointDetail> = runCatching {
        val response = HttpClientProvider.client.get(HttpClientProvider.buildUrl("RidePoint/$ridePointId"))
        if (response.status.isSuccess()) {
            response.body<RidePointDetail>()
        } else {
            throw Exception("Failed to fetch ride point detail")
        }
    }
}
