package com.veloce.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veloce.app.domain.model.Vehicle
import com.veloce.app.presentation.RideViewModel
import com.veloce.app.ui.components.*
import com.veloce.app.ui.theme.*

@Composable
fun RideScreen(
    rideViewModel: RideViewModel,
    userVehicles: List<Vehicle> = emptyList()
) {
    val state by rideViewModel.state.collectAsState()
    var showStartDialog by remember { mutableStateOf(false) }

    var selectedType by remember { mutableStateOf("Motorrad") }
    var selectedVehicleId by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (state.isRideActive) "FAHRT AKTIV" else "BEREIT ZUR FAHRT",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = if (state.isRideActive) ElectricLime else NeonCyan
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Map canvas in upper space as depicted in user sketch
            RouteMapCanvas(
                coordinates = listOf(
                    Pair(state.currentTelemetry.lat, state.currentTelemetry.lng)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Live Telemetry HUD Gauges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SpeedometerGauge(currentSpeed = state.currentTelemetry.spd)

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GForceGauge(gForce = state.currentTelemetry.g)

                    if (state.vehicleType == "Motorrad") {
                        LeanAngleGauge(rollAngle = state.currentTelemetry.roll)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Stats info bar if active
            if (state.isRideActive) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardBg)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatItem(label = "Zeit", value = "${state.elapsedSeconds}s")
                    StatItem(label = "Distanz", value = "${(state.distanceKm * 100).toInt() / 100.0} km")
                    StatItem(label = "Top-Speed", value = "${state.topSpeed.toInt()} km/h")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Big Action Button: GO! or STOP!
            if (!state.isRideActive) {
                Button(
                    onClick = { showStartDialog = true },
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 12.dp)
                ) {
                    Text(
                        text = "GO!",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                }
            } else {
                Button(
                    onClick = { rideViewModel.stopRide() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonOrange)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "FAHRT BEENDEN",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp)) // Bottom bar padding
        }

        // Start Ride Configuration Dialog
        if (showStartDialog) {
            AlertDialog(
                onDismissRequest = { showStartDialog = false },
                containerColor = CardBg,
                title = {
                    Text("Fahrt Konfigurieren", color = Color.White, fontWeight = FontWeight.Bold)
                },
                text = {
                    Column {
                        Text("1. Fahrzeugtyp wählen:", color = Color.Gray, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilterChip(
                                selected = selectedType == "Motorrad",
                                onClick = { selectedType = "Motorrad" },
                                label = { Text("Motorrad") },
                                leadingIcon = { Icon(Icons.Default.TwoWheeler, contentDescription = null) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonCyan,
                                    selectedLabelColor = Color.Black
                                )
                            )
                            FilterChip(
                                selected = selectedType == "Auto",
                                onClick = { selectedType = "Auto" },
                                label = { Text("Auto") },
                                leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonCyan,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("2. Fahrzeug auswählen (optional):", color = Color.Gray, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        if (userVehicles.isEmpty()) {
                            Text("Kein Fahrzeug registriert", color = Color.DarkGray, fontSize = 12.sp)
                        } else {
                            userVehicles.forEach { v ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedVehicleId = v.vehicleId }
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedVehicleId == v.vehicleId,
                                        onClick = { selectedVehicleId = v.vehicleId },
                                        colors = RadioButtonDefaults.colors(selectedColor = NeonCyan)
                                    )
                                    Text(v.name ?: "Fahrzeug", color = Color.White, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showStartDialog = false
                            rideViewModel.startRide(selectedType, selectedVehicleId)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Text("Fertig & Start", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showStartDialog = false }) {
                        Text("Abbrechen", color = Color.Gray)
                    }
                }
            )
        }
    }
}
