package com.veloce.app.domain.repository

import com.veloce.app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<AuthTokens>
    suspend fun signUp(username: String, password: String): Result<String>
    fun getSavedToken(): String?
    fun saveToken(token: String)
    fun logout()
}

interface UserRepository {
    suspend fun getUserRecords(): Result<UserRecords>
}

interface VehicleRepository {
    suspend fun getVehicles(): Result<List<Vehicle>>
    suspend fun getVehicleById(id: String): Result<Vehicle>
    suspend fun createVehicle(name: String?, description: String?, vehicleType: String, imageData: String? = null): Result<Vehicle>
    suspend fun updateVehicle(id: String, name: String?, description: String?, vehicleType: String): Result<Vehicle>
    suspend fun deleteVehicle(id: String): Result<Boolean>
}

interface RideRepository {
    suspend fun getRides(): Result<List<Ride>>
    suspend fun getRideById(id: String): Result<Ride>
    suspend fun startRide(vehicleType: String, vehicleId: String?): Result<String> // returns RideId
    suspend fun addRidePoint(rideId: String, point: RidePoint): Result<Unit>
    suspend fun stopRide(rideId: String): Result<Ride>
    suspend fun getRideCoordinates(rideId: String): Result<List<String>>
    suspend fun getRidePointDetail(ridePointId: String): Result<RidePointDetail>
}

interface BluetoothRepository {
    fun getTelemetryStream(): Flow<TelemetryData>
    fun isConnected(): Flow<Boolean>
    fun setSimulationMode(enabled: Boolean)
    fun isSimulationMode(): Boolean
    suspend fun connect(deviceAddress: String): Result<Boolean>
    suspend fun disconnect()
}
