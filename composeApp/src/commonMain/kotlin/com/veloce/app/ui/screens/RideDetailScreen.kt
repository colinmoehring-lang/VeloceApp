package com.veloce.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veloce.app.presentation.RideDetailViewModel
import com.veloce.app.ui.components.RouteMapCanvas
import com.veloce.app.ui.components.StatItem
import com.veloce.app.ui.theme.*

@Composable
fun RideDetailScreen(
    rideId: String,
    rideDetailViewModel: RideDetailViewModel,
    onBack: () -> Unit
) {
    val state by rideDetailViewModel.state.collectAsState()

    LaunchedEffect(rideId) {
        rideDetailViewModel.loadRide(rideId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Back Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Zurück", tint = NeonCyan)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Fahrt-Details & Telemetrie",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        val ride = state.ride
        // General Stats Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Zusammenfassung (${ride?.vehicleType ?: "Motorrad"})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(label = "Dauer", value = "${(ride?.duration ?: 1840.0).toInt() / 60} min")
                    StatItem(label = "Distanz", value = "${ride?.distance ?: 42.5} km")
                    StatItem(label = "Ø Speed", value = "${(ride?.averageSpeed ?: 82.5).toInt()} km/h")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(label = "Top-Speed", value = "${(ride?.highestSpeed ?: 142.0).toInt()} km/h")
                    StatItem(label = "Max Neigung", value = "${(ride?.highestLeanAngleLeft ?: 48.0).toInt()}°")
                    StatItem(label = "Max G-Force", value = "${ride?.highestGForce ?: 2.18}G")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Interactive Map
        Text(
            text = "Streckenanalyse (Interactive Map)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        RouteMapCanvas(
            coordinates = state.coordinates,
            selectedIndex = state.selectedIndex,
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Scrubbing Controller Slider
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Strecke abfahren (Scrubbing)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricLime
                )
                Text(
                    text = "Bewege den Regler, um die Live-Telemetrie an jedem Punkt der Fahrt einzusehen.",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(12.dp))

                val maxIndex = (state.coordinates.size - 1).coerceAtLeast(0)
                Slider(
                    value = state.selectedIndex.toFloat(),
                    onValueChange = { rideDetailViewModel.scrubToPoint(it.toInt()) },
                    valueRange = 0f..maxIndex.toFloat(),
                    steps = maxIndex.coerceAtLeast(1),
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonCyan,
                        inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Telemetry values at scrubbed point
                val detail = state.scrubDetail
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatItem(label = "Speed an Punkt", value = "${detail?.speed ?: 0.0} km/h")
                    StatItem(label = "Schräglage", value = "${detail?.lean ?: 0.0}°")
                    StatItem(label = "G-Force", value = "${detail?.gForce ?: 1.0}G")
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
