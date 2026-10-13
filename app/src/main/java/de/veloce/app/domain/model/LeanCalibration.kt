package de.veloce.app.domain.model

/**
 * Montage-Offset der Schräglage (Zero-Offset-Kalibrierung).
 *
 * Der Arduino sitzt am Motorrad selten exakt waagerecht. [offset] ist der rohe
 * Tilt-Wert, den der Sensor anzeigt, wenn das Motorrad gerade steht. Alle
 * zukünftigen Werte werden um diesen Betrag bereinigt: korrigiert = Rohwert − offset.
 *
 * Beispiel: offset = -20° → roh 70° ergibt 90°, roh -110° ergibt -90°, roh -20° ergibt 0°.
 */
fun TelemetryPoint.withLeanOffset(offset: Double): TelemetryPoint =
    if (offset == 0.0) this else copy(leanDegrees = leanDegrees - offset)
