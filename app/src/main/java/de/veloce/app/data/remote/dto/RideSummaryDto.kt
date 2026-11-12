package de.veloce.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RideSummaryDto(
    val rideId: String,
    val vehicleType: String,
    val startTime: String,
    val duration: Double,
    val distance: Double,
    val averageSpeed: Double,
    val highestSpeed: Double,
)
