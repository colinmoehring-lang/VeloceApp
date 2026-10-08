package de.veloce.app.domain.model

import java.time.Instant

data class ParsedBleLine(
    val rawLine: String,
    val receivedAt: Instant,
    val point: TelemetryPoint?,
) {
    val isValid: Boolean get() = point != null
}
