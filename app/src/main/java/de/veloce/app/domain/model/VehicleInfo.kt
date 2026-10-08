package de.veloce.app.domain.model

data class Vehicle(
    val id: String,
    val name: String,
    val description: String,
    val vehicleType: String,
    val imageData: String?,
)

data class UserRecords(
    val speedKmh: Double,
    val leanLeftDegrees: Double,
    val leanRightDegrees: Double,
    val gForce: Double,
)
