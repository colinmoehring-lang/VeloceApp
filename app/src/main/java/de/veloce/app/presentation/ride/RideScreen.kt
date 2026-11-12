package de.veloce.app.presentation.ride

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
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

    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* Aufzeichnung läuft unabhängig von der Notification-Berechtigung. */ }

    val startAction: () -> Unit = {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        viewModel.startRecording()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "recordingDot")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "alpha",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VeloceColors.Background)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Fahrt", style = MaterialTheme.typography.headlineMedium)
                    if (state.isRecording) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(VeloceColors.Error.copy(alpha = dotAlpha), CircleShape),
                        )
                    }
                }
                Text(
                    when {
                        state.isRecording -> "Messwerte werden live aufgezeichnet"
                        state.dataSource == de.veloce.app.domain.model.RideDataSource.ARDUINO ->
                            "Datenquelle: Arduino (Bluetooth)"
                        else -> "Datenquelle: Simulator"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (state.isRecording) VeloceColors.Error else VeloceColors.Muted,
                    fontWeight = if (state.isRecording) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
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
                        valueColor = if (state.isRecording && (point?.leanDegrees ?: 0.0) > 20.0) VeloceColors.Orange else VeloceColors.Ink,
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

        RideMap(
            route = state.route,
            currentPoint = point,
            speedKmh = point?.speedKmh ?: 0.0,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(22.dp)),
        )

        state.error?.let {
            Text(
                text = it,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        val buttonColor by animateColorAsState(
            targetValue = if (state.isRecording) VeloceColors.Error else VeloceColors.Ink,
            label = "recordButtonColor",
        )

        Button(
            onClick = when {
                state.isRecording -> viewModel::stopRecording
                state.stopPending -> viewModel::retryStop
                else -> startAction
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
            colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
        ) {
            when {
                state.isStarting || state.isStopping -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = VeloceColors.Surface,
                        strokeWidth = 2.dp,
                    )
                }

                state.isRecording -> Text("Fahrt stoppen", fontWeight = FontWeight.SemiBold)
                state.stopPending -> Text("Abschluss erneut senden", fontWeight = FontWeight.SemiBold)
                else -> Text("Fahrt starten", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

private fun Double.formatOneDecimal(): String =
    String.format(Locale.GERMANY, "%.1f", this)

private fun Double.formatTwoDecimals(): String =
    String.format(Locale.GERMANY, "%.2f", this)
