package de.veloce.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class StartRideRequestDto(
    val vehicleType: String,
    val vehicleId: String? = null,
)

@Serializable
data class StartRideResponseDto(
    val rideId: String,
)

@Serializable
data class AddRidePointRequestDto(
    val timestamp: String,
    val coordinate: String,
    val speed: Double,
    val gForce: Double,
    val lean: Double,
)
