package de.veloce.app.presentation.ride

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsBike
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.veloce.app.presentation.components.MapMode
import de.veloce.app.presentation.components.RideMap
import de.veloce.app.presentation.components.StatCard
import de.veloce.app.presentation.theme.VeloceColors
import java.util.Locale

@Composable
fun RideScreen(
    onOpenSettings: () -> Unit,
    viewModel: RideViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val point = state.currentPoint

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VeloceColors.Background)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column {
            Text("Fahrt", style = MaterialTheme.typography.headlineMedium)
            Text(
                when {
                    state.isRecording -> "Messwerte werden live aufgezeichnet"
                    state.dataSource == de.veloce.app.domain.model.RideDataSource.ARDUINO ->
                        "Datenquelle: Arduino (Bluetooth)"
                    else -> "Datenquelle: Simulator"
                },
                style = MaterialTheme.typography.bodySmall,
                color = VeloceColors.Muted,
            )
        }

        if (
            state.dataSource == de.veloce.app.domain.model.RideDataSource.ARDUINO &&
            !state.isArduinoConnected &&
            !state.isRecording
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = VeloceColors.PaleOrange,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Kein Arduino verbunden. Verbinde das Gerät in den Einstellungen, bevor du startest.",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                        color = VeloceColors.Ink,
                    )
                    TextButton(onClick = onOpenSettings) { Text("Einstellungen") }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            FilterChip(
                selected = state.vehicleType == "Car",
                onClick = { viewModel.selectVehicleType("Car") },
                enabled = !state.isRecording && !state.isStarting && !state.stopPending,
                label = { Text("Auto") },
                leadingIcon = { Icon(Icons.Outlined.DirectionsCar, contentDescription = null) },
            )
            FilterChip(
                selected = state.vehicleType == "Motorcycle",
                onClick = { viewModel.selectVehicleType("Motorcycle") },
                enabled = !state.isRecording && !state.isStarting && !state.stopPending,
                label = { Text("Motorrad") },
                leadingIcon = { Icon(Icons.Outlined.DirectionsBike, contentDescription = null) },
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = VeloceColors.Surface,
            shadowElevation = 4.dp,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    StatCard(
                        label = "Geschwindigkeit",
                        value = point?.speedKmh?.formatOneDecimal() ?: "0,0",
                        unit = "km/h",
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        label = "Schräglage",
                        value = point?.leanDegrees?.formatOneDecimal() ?: "0,0",
                        unit = "°",
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        label = "G-Kraft",
                        value = point?.gForce?.formatTwoDecimals() ?: "0,00",
                        unit = "g",
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(ColorMapBackground),
        ) {
            if (viewModel.mapboxToken.isNotBlank()) {
                RideMap(
                    route = state.route,
                    speedKmh = point?.speedKmh ?: 0.0,
                    mode = MapMode.LIVE,
                    mapboxToken = viewModel.mapboxToken,
                    modifier = Modifier.fillMaxSize(),
                )
                if (point == null || !point.hasValidCoordinates) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(12.dp),
                        color = VeloceColors.Surface.copy(alpha = 0.94f),
                        shape = RoundedCornerShape(50),
                    ) {
                        Text(
                            if (state.isRecording) "Warte auf GPS" else "Karte für die Fahrt",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text("Karte nicht konfiguriert", fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Füge MAPBOX_TOKEN in local.properties ein, um die Karte anzuzeigen.",
                        color = VeloceColors.Muted,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        state.error?.let {
            Text(
                text = it,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Button(
            onClick = when {
                state.isRecording -> viewModel::stopRecording
                state.stopPending -> viewModel::retryStop
                else -> viewModel::startRecording
            },
            enabled = !state.isStarting &&
                !state.isStopping &&
                !state.isLoadingSettings &&
                (
                    state.isRecording ||
                        state.stopPending ||
                        state.dataSource != de.veloce.app.domain.model.RideDataSource.ARDUINO ||
                        state.isArduinoConnected
                    ),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (state.isRecording) VeloceColors.Error else VeloceColors.Ink,
            ),
        ) {
            when {
                state.isStarting || state.isStopping -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = VeloceColors.Surface,
                        strokeWidth = 2.dp,
                    )
                }

                state.isRecording -> Text("Fahrt stoppen")
                state.stopPending -> Text("Abschluss erneut senden")
                else -> Text("Fahrt starten")
            }
        }
    }
}

private val ColorMapBackground get() = androidx.compose.ui.graphics.Color(0xFFE7E6E1)

private fun Double.formatOneDecimal(): String =
    String.format(Locale.GERMANY, "%.1f", this)

private fun Double.formatTwoDecimals(): String =
    String.format(Locale.GERMANY, "%.2f", this)
