package de.veloce.app.data.mapper

import de.veloce.app.data.remote.dto.AuthResponseDto
import de.veloce.app.data.remote.dto.RideSummaryDto
import de.veloce.app.data.remote.dto.UserRecordsDto
import de.veloce.app.data.remote.dto.VehicleDto
import de.veloce.app.domain.model.AuthSession
import de.veloce.app.domain.model.MapPoint
import de.veloce.app.domain.model.RideHistoryItem
import de.veloce.app.domain.model.UserRecords
import de.veloce.app.domain.model.Vehicle
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException
import java.util.UUID

fun AuthResponseDto.toDomain() = AuthSession(
    accessToken = accessToken,
    expiresInSeconds = expiresIn,
    userId = UUID.fromString(userId),
    email = email,
)

fun VehicleDto.toDomain() = Vehicle(
    id = vehicleId,
    name = name.orEmpty(),
    description = description.orEmpty(),
    vehicleType = vehicleType,
    imageData = imageData,
)

fun UserRecordsDto.toDomain() = UserRecords(
    speedKmh = speedRecord ?: 0.0,
    leanLeftDegrees = leanRecordLeft ?: 0.0,
    leanRightDegrees = leanRecordRight ?: 0.0,
    gForce = gForceRecord ?: 0.0,
)

fun RideSummaryDto.toDomain() = RideHistoryItem(
    id = rideId,
    vehicleType = vehicleType,
    startedAt = startTime.toInstant(),
    durationSeconds = duration,
    distanceMeters = distance,
    averageSpeedKmh = averageSpeed,
    highestSpeedKmh = highestSpeed,
)

private fun String.toInstant(): Instant = try {
    Instant.parse(this)
} catch (_: DateTimeParseException) {
    OffsetDateTime.parse(this).toInstant()
}

internal fun String.toMapPointOrNull(): MapPoint? {
    val coordinates = split(',', limit = 2)
    if (coordinates.size != 2) return null

    val latitude = coordinates[0].trim().toDoubleOrNull() ?: return null
    val longitude = coordinates[1].trim().toDoubleOrNull() ?: return null
    if (
        !latitude.isFinite() ||
        !longitude.isFinite() ||
        latitude !in -90.0..90.0 ||
        longitude !in -180.0..180.0 ||
        (latitude == 0.0 && longitude == 0.0)
    ) {
        return null
    }

    return MapPoint(latitude, longitude)
}
