package de.veloce.app.presentation.rides

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.veloce.app.domain.model.MapPoint
import de.veloce.app.domain.model.RideHistoryItem
import de.veloce.app.presentation.components.RideMap
import de.veloce.app.presentation.components.StatCard
import de.veloce.app.presentation.components.VeloceCard
import de.veloce.app.presentation.theme.VeloceColors
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun RideHistoryCard(
    ride: RideHistoryItem,
    route: List<MapPoint>,
    isRouteLoading: Boolean,
    routeError: String?,
    showRoute: Boolean,
    onToggleRoute: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val routeDistance = route.distanceMeters()
    val distanceMeters = routeDistance.takeIf { it > 0.0 } ?: ride.distanceMeters

    VeloceCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = ride.vehicleType.toVehicleLabel(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = ride.startedAt.atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("dd.MM.yyyy · HH:mm", Locale.GERMANY)),
                    style = MaterialTheme.typography.bodySmall,
                    color = VeloceColors.Muted,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatCard(
                    label = "Dauer",
                    value = ride.durationSeconds.formatDuration(),
                    unit = "",
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    label = "Strecke",
                    value = (distanceMeters / 1000.0).formatOneDecimal(),
                    unit = "km",
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    label = "Ø Tempo",
                    value = ride.averageSpeedKmh.formatOneDecimal(),
                    unit = "km/h",
                    modifier = Modifier.weight(1f),
                )
            }

            if (onToggleRoute != null) {
                TextButton(
                    onClick = onToggleRoute,
                    modifier = Modifier.align(Alignment.Start),
                ) {
                    Text(if (showRoute) "Karte ausblenden" else "Route auf Karte")
                }
            }

            if (showRoute) {
                when {
                    isRouteLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }

                    routeError != null -> {
                        Text(
                            text = routeError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    route.isEmpty() -> {
                        Text(
                            text = "Für diese Fahrt wurden keine gültigen GPS-Punkte gespeichert.",
                            color = VeloceColors.Muted,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    else -> {
                        RideMap(
                            route = route,
                            currentPoint = null,
                            speedKmh = 0.0,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(18.dp)),
                        )
                    }
                }
            }
        }
    }
}

private fun List<MapPoint>.distanceMeters(): Double =
    zipWithNext().sumOf { (start, end) ->
        val lat1 = Math.toRadians(start.lat)
        val lat2 = Math.toRadians(end.lat)
        val deltaLat = lat2 - lat1
        val deltaLng = Math.toRadians(end.lng - start.lng)
        val haversine = kotlin.math.sin(deltaLat / 2).let { it * it } +
            kotlin.math.cos(lat1) * kotlin.math.cos(lat2) *
            kotlin.math.sin(deltaLng / 2).let { it * it }
        6_371_000.0 * 2 * kotlin.math.atan2(
            kotlin.math.sqrt(haversine),
            kotlin.math.sqrt(1 - haversine),
        )
    }

private fun Double.formatDuration(): String {
    val totalMinutes = (this.coerceAtLeast(0.0) / 60.0).toLong()
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours}h ${minutes}min" else "${minutes}min"
}

private fun Double.formatOneDecimal(): String =
    String.format(Locale.GERMANY, "%.1f", this)

private fun String.toVehicleLabel(): String = when (this) {
    "Car" -> "Auto"
    "Motorcycle" -> "Motorrad"
    else -> this
}
