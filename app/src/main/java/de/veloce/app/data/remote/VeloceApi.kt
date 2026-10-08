package de.veloce.app.data.remote

import de.veloce.app.data.remote.dto.AuthResponseDto
import de.veloce.app.data.remote.dto.AddRidePointRequestDto
import de.veloce.app.data.remote.dto.CreateVehicleRequestDto
import de.veloce.app.data.remote.dto.GenericMessageResponseDto
import de.veloce.app.data.remote.dto.LogInRequestDto
import de.veloce.app.data.remote.dto.SignUpRequestDto
import de.veloce.app.data.remote.dto.StartRideRequestDto
import de.veloce.app.data.remote.dto.StartRideResponseDto
import de.veloce.app.data.remote.dto.UpdateVehicleRequestDto
import de.veloce.app.data.remote.dto.UserRecordsDto
import de.veloce.app.data.remote.dto.VehicleDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface VeloceApi {
    @POST("api/integration/v1/Auth/sign-up")
    suspend fun signUp(@Body body: SignUpRequestDto): GenericMessageResponseDto

    @POST("api/integration/v1/Auth/login")
    suspend fun logIn(@Body body: LogInRequestDto): AuthResponseDto

    @GET("api/integration/v1/Vehicle")
    suspend fun getVehicles(): List<VehicleDto>

    @POST("api/integration/v1/Vehicle")
    suspend fun createVehicle(@Body body: CreateVehicleRequestDto): VehicleDto

    @PUT("api/integration/v1/Vehicle/{id}")
    suspend fun updateVehicle(
        @Path("id") id: String,
        @Body body: UpdateVehicleRequestDto,
    ): VehicleDto

    @DELETE("api/integration/v1/Vehicle/{id}")
    suspend fun deleteVehicle(@Path("id") id: String): Response<Unit>

    @GET("api/integration/v1/User/records")
    suspend fun getUserRecords(): UserRecordsDto

    @POST("api/integration/v1/Ride/start")
    suspend fun startRide(@Body body: StartRideRequestDto): StartRideResponseDto

    @POST("api/integration/v1/Ride/{rideId}/point")
    suspend fun addRidePoint(
        @Path("rideId") rideId: String,
        @Body body: AddRidePointRequestDto,
    ): Response<Unit>

    @POST("api/integration/v1/Ride/{rideId}/stop")
    suspend fun stopRide(@Path("rideId") rideId: String): Response<Unit>
}
