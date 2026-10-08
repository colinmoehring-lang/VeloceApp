package de.veloce.app.data.mapper

import de.veloce.app.data.remote.dto.AuthResponseDto
import de.veloce.app.data.remote.dto.UserRecordsDto
import de.veloce.app.data.remote.dto.VehicleDto
import de.veloce.app.domain.model.AuthSession
import de.veloce.app.domain.model.UserRecords
import de.veloce.app.domain.model.Vehicle
import java.util.UUID

fun AuthResponseDto.toDomain() = AuthSession(
    accessToken = accessToken,
    expiresInSeconds = expiresIn,
    userId = UUID.fromString(userId),
    userName = userName,
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
