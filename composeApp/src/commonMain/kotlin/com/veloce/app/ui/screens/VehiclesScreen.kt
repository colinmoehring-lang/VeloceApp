package com.veloce.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veloce.app.domain.model.Vehicle
import com.veloce.app.presentation.VehicleViewModel
import com.veloce.app.ui.theme.*

@Composable
fun VehiclesScreen(
    vehicleViewModel: VehicleViewModel,
    onBack: () -> Unit
) {
    val state by vehicleViewModel.state.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var vehicleType by remember { mutableStateOf("Motorrad") }

    LaunchedEffect(Unit) {
        vehicleViewModel.loadVehicles()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück", tint = NeonCyan)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Fahrzeug-Garage",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = NeonCyan,
                    contentColor = Color.Black,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Fahrzeug hinzufügen")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state.vehicles.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Keine Fahrzeuge vorhanden.\nKlicke oben auf '+' um eins zu erstellen!",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.vehicles) { vehicle ->
                        VehicleGridCard(vehicle = vehicle)
                    }
                }
            }
        }

        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                containerColor = CardBg,
                title = { Text("Neues Fahrzeug Erstellen", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name (z.B. BMW S1000RR)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Beschreibung (z.B. 199 PS)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = vehicleType == "Motorrad",
                                onClick = { vehicleType = "Motorrad" },
                                label = { Text("Motorrad") }
                            )
                            FilterChip(
                                selected = vehicleType == "Auto",
                                onClick = { vehicleType = "Auto" },
                                label = { Text("Auto") }
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showCreateDialog = false
                            vehicleViewModel.createVehicle(name, description, vehicleType)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Text("Erstellen", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) {
                        Text("Abbrechen", color = Color.Gray)
                    }
                }
            )
        }
    }
}

@Composable
fun VehicleGridCard(vehicle: Vehicle) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(NeonCyan.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (vehicle.vehicleType == "Auto") Icons.Default.DirectionsCar else Icons.Default.TwoWheeler,
                    contentDescription = vehicle.vehicleType,
                    tint = NeonCyan
                )
            }

            Column {
                Text(
                    text = vehicle.name ?: "Fahrzeug",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = vehicle.description ?: vehicle.vehicleType,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
