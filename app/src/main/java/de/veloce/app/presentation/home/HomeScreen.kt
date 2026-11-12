package de.veloce.app.presentation.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.veloce.app.domain.model.Vehicle
import de.veloce.app.presentation.components.ListRow
import de.veloce.app.presentation.components.SectionHeader
import de.veloce.app.presentation.components.StatCard
import de.veloce.app.presentation.components.VeloceCard
import de.veloce.app.presentation.rides.RideHistoryCard
import de.veloce.app.presentation.theme.VeloceColors
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HomeScreen(
    onOpenVehicles: () -> Unit,
    onOpenRides: () -> Unit,
    onLogout: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val latestRide = state.latestRide
    val leftLean = abs(state.records.leanLeftDegrees).coerceIn(0.0, 45.0)
    val rightLean = abs(state.records.leanRightDegrees).coerceIn(0.0, 45.0)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(VeloceColors.Background),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 20.dp,
            top = 18.dp,
            end = 20.dp,
            bottom = 110.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text("VELOCE", style = MaterialTheme.typography.labelSmall, color = VeloceColors.Muted)
                    Text("Deine Fahrten", style = MaterialTheme.typography.headlineMedium)
                }
                IconButton(onClick = onLogout) {
                    Icon(Icons.Outlined.Logout, contentDescription = "Abmelden")
                }
            }
        }
        item {
            VeloceCard {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 17.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("HÖCHSTE SCHRÄGLAGE", style = MaterialTheme.typography.labelSmall, color = VeloceColors.Muted)
                    Spacer(Modifier.height(8.dp))
                    LeanGauge(leftLean, rightLean)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        StatCard(
                            "Höchsttempo",
                            state.records.speedKmh.displayValue(),
                            "km/h",
                            modifier = Modifier.weight(1f),
                        )
                        StatCard(
                            "G-Kraft",
                            state.records.gForce.displayValue(),
                            "g",
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (state.recordsError != null) {
                        Text(
                            state.recordsError.orEmpty(),
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        TextButton(onClick = viewModel::refresh) { Text("Erneut laden") }
                    }
                }
            }
        }
        item {
            SectionHeader("Fahrzeuge", onAction = onOpenVehicles)
        }
        if (state.isLoading && state.vehicles.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else if (state.vehicles.isEmpty()) {
            item {
                VeloceCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            state.vehiclesError ?: "Noch kein Fahrzeug angelegt.",
                            modifier = Modifier.weight(1f),
                            color = VeloceColors.Muted,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        IconButton(onClick = onOpenVehicles) {
                            Icon(Icons.Outlined.Add, contentDescription = "Fahrzeug hinzufügen")
                        }
                    }
                }
            }
        } else {
            item {
                VeloceCard {
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        state.vehicles.take(2).forEachIndexed { index, vehicle ->
                            VehiclePreview(vehicle)
                            if (index < state.vehicles.take(2).lastIndex) {
                                Spacer(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(VeloceColors.Line),
                                )
                            }
                        }
                    }
                }
            }
        }
        item {
            SectionHeader("Letzte Fahrt", onAction = onOpenRides)
        }
        item {
            when {
                state.isLoadingRides && latestRide == null -> {
                    VeloceCard {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                latestRide != null -> {
                    RideHistoryCard(
                        ride = latestRide,
                        route = state.latestRideRoute,
                        isRouteLoading = state.isLoadingLatestRideRoute,
                        routeError = state.rideRouteError,
                        showRoute = true,
                        onToggleRoute = null,
                    )
                }

                state.ridesError != null -> {
                    VeloceCard {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = state.ridesError.orEmpty(),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                            TextButton(onClick = viewModel::refresh) { Text("Fahrten erneut laden") }
                        }
                    }
                }

                else -> {
                    VeloceCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(7.dp),
                        ) {
                            Text("Noch keine Fahrten", fontWeight = FontWeight.Medium)
                            Text(
                                "Deine letzte Fahrt erscheint hier.",
                                color = VeloceColors.Muted,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VehiclePreview(vehicle: Vehicle) {
    ListRow(
        title = vehicle.name.ifBlank { "Fahrzeug" },
        subtitle = listOf(vehicle.vehicleType.vehicleTypeLabel(), vehicle.description)
            .filter { it.isNotBlank() }
            .joinToString(" · "),
        icon = Icons.Outlined.DirectionsCar,
    )
}

@Composable
private fun LeanGauge(leftLean: Double, rightLean: Double) {
    val signedLean = if (leftLean > rightLean) -leftLean else rightLean
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(136.dp),
        ) {
            val centerX = size.width / 2f
            val baseline = size.height - 10.dp.toPx()
            val radiusX = size.width * 0.48f
            val radiusY = minOf(size.width * 0.78f, baseline - 4.dp.toPx())
            drawArc(
                color = VeloceColors.Orange,
                startAngle = 210f,
                sweepAngle = 120f,
                useCenter = false,
                topLeft = Offset(centerX - radiusX, baseline - radiusY),
                size = Size(radiusX * 2, radiusY * 2),
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round),
            )
            for (tick in 0..9) {
                val angle = Math.toRadians(210.0 + 120.0 * tick / 9.0)
                val innerOffset = if (tick % 3 == 0) 11.dp.toPx() else 7.dp.toPx()
                drawLine(
                    color = VeloceColors.Orange.copy(alpha = 0.65f),
                    start = Offset(
                        centerX + cos(angle).toFloat() * (radiusX - innerOffset),
                        baseline + sin(angle).toFloat() * (radiusY - innerOffset),
                    ),
                    end = Offset(
                        centerX + cos(angle).toFloat() * (radiusX + 1.dp.toPx()),
                        baseline + sin(angle).toFloat() * (radiusY + 1.dp.toPx()),
                    ),
                    strokeWidth = 1.5.dp.toPx(),
                )
            }
            val needleAngle = Math.toRadians(270.0 + signedLean)
            drawLine(
                color = VeloceColors.Ink,
                start = Offset(centerX, baseline),
                end = Offset(
                    centerX + cos(needleAngle).toFloat() * (radiusX - 20.dp.toPx()),
                    baseline + sin(needleAngle).toFloat() * (radiusY - 15.dp.toPx()),
                ),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round,
            )
            drawCircle(color = VeloceColors.Ink, radius = 4.dp.toPx(), center = Offset(centerX, baseline))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .padding(top = 1.dp),
            verticalAlignment = Alignment.Top,
        ) {
            LeanRecordValue(
                label = "Links",
                value = leftLean,
                modifier = Modifier.weight(1f),
                alignment = TextAlign.Start,
            )
            LeanRecordValue(
                label = "Gerade",
                value = 0.0,
                modifier = Modifier.weight(1f),
                alignment = TextAlign.Center,
            )
            LeanRecordValue(
                label = "Rechts",
                value = rightLean,
                modifier = Modifier.weight(1f),
                alignment = TextAlign.End,
            )
        }
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun LeanRecordValue(
    label: String,
    value: Double,
    modifier: Modifier,
    alignment: TextAlign,
) {
    Column(modifier = modifier, horizontalAlignment = when (alignment) {
        TextAlign.Start -> Alignment.Start
        TextAlign.End -> Alignment.End
        else -> Alignment.CenterHorizontally
    }) {
        Text(
            "${value.displayValue()}°",
            style = MaterialTheme.typography.titleMedium,
            color = if (label == "Gerade") VeloceColors.Muted else VeloceColors.Orange,
            textAlign = alignment,
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = VeloceColors.Muted)
    }
}

private fun Double.displayValue(): String =
    if (this % 1.0 == 0.0) toLong().toString() else String.format(java.util.Locale.GERMANY, "%.1f", this)

private fun String.vehicleTypeLabel(): String = when (this) {
    "Car" -> "Auto"
    "Motorcycle" -> "Motorrad"
    else -> this
}
