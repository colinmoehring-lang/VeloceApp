package com.veloce.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthTokens(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Int,
    val userId: String,
    val userName: String
)

@Serializable
data class UserRecords(
    val speedRecord: Double? = null,
    val leanRecordLeft: Double? = null,
    val leanRecordRight: Double? = null,
    val gForceRecord: Double? = null
)

@Serializable
data class Vehicle(
    val vehicleId: String = "",
    val userId: String = "",
    val name: String? = null,
    val description: String? = null,
    val vehicleType: String = "Motorrad",
    val imageData: String? = null // Base64 encoded or hex
)

@Serializable
data class Ride(
    val rideId: String = "",
    val userId: String = "",
    val vehicleId: String? = null,
    val vehicleType: String = "Motorrad",
    val startTime: String = "",
    val endTime: String = "",
    val duration: Double = 0.0,
    val distance: Double = 0.0,
    val highestSpeed: Double = 0.0,
    val coordinateHighestSpeed: String = "",
    val averageSpeed: Double = 0.0,
    val highestLeanAngleLeft: Double = 0.0,
    val coordinateHighestLeanAngleLeft: String = "",
    val highestLeanAngleRight: Double = 0.0,
    val coordinateHighestLeanAngleRight: String = "",
    val highestGForce: Double = 0.0
)

@Serializable
data class RidePoint(
    val timestamp: String,
    val coordinate: String, // "lat,lng"
    val speed: Double,
    val gForce: Double,
    val lean: Double = 0.0
)

@Serializable
data class RidePointDetail(
    val ridePointId: String = "",
    val speed: Double = 0.0,
    val gForce: Double = 0.0,
    val lean: Double = 0.0,
    val coordinate: String = "0.0,0.0"
)

@Serializable
data class TelemetryData(
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val spd: Double = 0.0,
    val roll: Double = 0.0,
    val g: Double = 0.0
)
