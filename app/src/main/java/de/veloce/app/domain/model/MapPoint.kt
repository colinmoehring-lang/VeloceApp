package de.veloce.app.domain.model

/**
 * Ein einzelner GPS-Punkt der Fahrtstrecke. Wird von der Karte gerendert und
 * im Aufzeichnungs-Verlauf gesammelt.
 */
data class MapPoint(val lat: Double, val lng: Double)
