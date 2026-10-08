package de.veloce.app.data.ble

import de.veloce.app.domain.model.TelemetryPoint
import de.veloce.app.domain.repository.BleConnection
import de.veloce.app.domain.repository.TelemetrySource
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

@Singleton
class BleTelemetrySource @Inject constructor(
    private val connection: BleConnection,
) : TelemetrySource {
    override fun points(): Flow<TelemetryPoint> = connection.lines
        .filter { it.point != null }
        .map { requireNotNull(it.point) }
}