package de.veloce.app.data.ble

import de.veloce.app.domain.model.TelemetryPoint
import de.veloce.app.domain.model.ParsedBleLine
import java.nio.charset.StandardCharsets
import java.time.Instant

class LineParser(
    private val clock: () -> Instant = Instant::now,
    private val maxLineBytes: Int = 512,
) {
    private val lineBytes = ArrayList<Byte>()
    private var discardUntilNewline = false
    private var oversizedLine = false

    init {
        require(maxLineBytes > 0)
    }

    @Synchronized
    fun reset(discardUntilFirstNewline: Boolean = false) {
        lineBytes.clear()
        discardUntilNewline = discardUntilFirstNewline
        oversizedLine = false
    }

    @Synchronized
    fun finish(): ParsedBleLine? {
        if (lineBytes.isEmpty() && !oversizedLine) {
            discardUntilNewline = false
            return null
        }
        val raw = if (oversizedLine) {
            "Unvollständige Zeile verworfen: länger als $maxLineBytes Bytes"
        } else {
            String(lineBytes.toByteArray(), StandardCharsets.UTF_8).removeSuffix("\r")
        }
        reset()
        return ParsedBleLine(raw, clock(), null)
    }

    @Synchronized
    fun feed(packet: ByteArray): List<ParsedBleLine> {
        val completed = mutableListOf<ParsedBleLine>()
        packet.forEach { byte ->
            if (byte == '\n'.code.toByte()) {
                if (discardUntilNewline) {
                    discardUntilNewline = false
                    if (lineBytes.isNotEmpty() || oversizedLine) {
                        val receivedAt = clock()
                        val raw = if (oversizedLine) {
                            "Unvollständige Zeile beim Verbindungsaufbau verworfen"
                        } else {
                            String(lineBytes.toByteArray(), StandardCharsets.UTF_8)
                                .removeSuffix("\r")
                                .ifBlank { "Unvollständige Zeile beim Verbindungsaufbau verworfen" }
                        }
                        completed += ParsedBleLine(raw, receivedAt, null)
                    }
                    lineBytes.clear()
                    oversizedLine = false
                    return@forEach
                }
                val receivedAt = clock()
                val raw = if (oversizedLine) {
                    "Zeile verworfen: länger als $maxLineBytes Bytes"
                } else {
                    String(lineBytes.toByteArray(), StandardCharsets.UTF_8).removeSuffix("\r")
                }
                completed += parseLine(raw, receivedAt)
                lineBytes.clear()
                oversizedLine = false
            } else {
                if (lineBytes.size < maxLineBytes) lineBytes += byte else oversizedLine = true
            }
        }
        return completed
    }

    @Synchronized
    fun parseLine(rawLine: String, receivedAt: Instant = clock()): ParsedBleLine {
        val normalized = rawLine.removeSuffix("\r")
        val fields = normalized.split(',')
        val point = if (fields.size == 5) {
            val speed = fields[0].trim().toDoubleOrNull()
            val tilt = fields[1].trim().toDoubleOrNull()
            val gForce = fields[2].trim().toDoubleOrNull()
            val latitude = fields[3].trim().toDoubleOrNull()
            val longitude = fields[4].trim().toDoubleOrNull()
            if (listOf(speed, tilt, gForce, latitude, longitude).all { it != null && it.isFinite() }) {
                TelemetryPoint(
                    timestamp = receivedAt,
                    latitude = latitude!!,
                    longitude = longitude!!,
                    speedKmh = speed!!,
                    leanDegrees = tilt!!,
                    gForce = gForce!!,
                )
            } else {
                null
            }
        } else {
            null
        }
        return ParsedBleLine(normalized, receivedAt, point)
    }
}
