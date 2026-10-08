package de.veloce.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateVehicleRequestDto(
    val name: String?,
    val description: String?,
    val vehicleType: String,
    val imageData: String?,
)

@Serializable
data class UpdateVehicleRequestDto(
    val name: String?,
    val description: String?,
    val vehicleType: String,
    val imageData: String?,
)

@Serializable
data class VehicleDto(
    val vehicleId: String,
    val userId: String,
    val name: String? = null,
    val description: String? = null,
    val vehicleType: String,
    val imageData: String? = null,
)

@Serializable
data class UserRecordsDto(
    val speedRecord: Double? = null,
    val leanRecordLeft: Double? = null,
    val leanRecordRight: Double? = null,
    val gForceRecord: Double? = null,
)
