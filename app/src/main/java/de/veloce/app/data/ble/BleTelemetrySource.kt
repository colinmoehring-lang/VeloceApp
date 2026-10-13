package de.veloce.app.data.ble

import de.veloce.app.data.repository.SettingsStore
import de.veloce.app.domain.model.TelemetryPoint
import de.veloce.app.domain.model.withLeanOffset
import de.veloce.app.domain.repository.BleConnection
import de.veloce.app.domain.repository.TelemetrySource
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

/**
 * Liefert die Telemetrie-Punkte vom Arduino und bereinigt die Schräglage um den
 * kalibrierten Montage-Offset (siehe [SettingsStore.leanOffsetDegrees]). Eine
 * Änderung des Offsets wirkt auch mitten in einer laufenden Fahrt.
 */
@Singleton
class BleTelemetrySource @Inject constructor(
    private val connection: BleConnection,
    private val settingsStore: SettingsStore,
) : TelemetrySource {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun points(): Flow<TelemetryPoint> =
        settingsStore.leanOffsetDegrees.flatMapLatest { offset ->
            connection.lines
                .filter { it.point != null }
                .map { requireNotNull(it.point).withLeanOffset(offset) }
        }
}