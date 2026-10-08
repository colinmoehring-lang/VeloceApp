package de.veloce.app.domain.repository

import de.veloce.app.domain.model.TelemetryPoint
import kotlinx.coroutines.flow.Flow

interface TelemetrySource {
    fun points(): Flow<TelemetryPoint>
}