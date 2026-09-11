package com.veloce.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veloce.app.domain.model.Ride
import com.veloce.app.domain.model.Vehicle
import com.veloce.app.presentation.HomeViewModel
import com.veloce.app.ui.theme.*

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    onNavigateToVehicles: () -> Unit,
    onNavigateToRides: () -> Unit,
    onSelectRideDetail: (String) -> Unit
) {
    val state by homeViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        homeViewModel.loadData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // Welcome Header
        Text(
            text = "Willkommen zurück!",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Deine Veloce Performance Rekorde",
            fontSize = 13.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // 3 Record Badges Row (from UserController.GetUserRecordsAsync)
        val records = state.records
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RecordBadge(
                title = "Top-Spd",
                value = "${records?.speedRecord?.toInt() ?: 130}",
                unit = "km/h",
                color = NeonCyan,
                modifier = Modifier.weight(1f)
            )
            RecordBadge(
                title = "Neigung",
                value = "${records?.leanRecordLeft?.toInt() ?: 48}°",
                unit = "Max",
                color = ElectricLime,
                modifier = Modifier.weight(1f)
            )
            RecordBadge(
                title = "G-Force",
                value = "${records?.gForceRecord ?: 2.18}",
                unit = "G",
                color = NeonOrange,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Fahrzeuge Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Fahrzeuge",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            IconButton(onClick = onNavigateToVehicles) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Alle Fahrzeuge",
                    tint = NeonCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Vehicle Preview Tile
        val firstVehicle = state.vehicles.firstOrNull() ?: Vehicle(name = "Kein Fahrzeug", vehicleType = "Motorrad")
        VehicleTile(vehicle = firstVehicle, onClick = onNavigateToVehicles)

        Spacer(modifier = Modifier.height(28.dp))

        // Fahrten Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Fahrten",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            IconButton(onClick = onNavigateToRides) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Alle Fahrten",
                    tint = NeonCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Ride Preview Tile
        val firstRide = state.rides.firstOrNull() ?: Ride(
            rideId = "r1",
            vehicleType = "Motorrad",
            duration = 1840.0,
            distance = 42.5,
            highestSpeed = 142.0,
            highestLeanAngleLeft = 48.0,
            highestGForce = 2.18
        )
        RideTile(ride = firstRide, onClick = { onSelectRideDetail(firstRide.rideId) })

        Spacer(modifier = Modifier.height(80.dp)) // Padding for bottom bar
    }
}

@Composable
fun RecordBadge(
    title: String,
    value: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(95.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Black, color = color)
                Spacer(modifier = Modifier.width(3.dp))
                Text(text = unit, fontSize = 10.sp, color = color.copy(alpha = 0.8f), modifier = Modifier.padding(bottom = 2.dp))
            }
        }
    }
}

@Composable
fun VehicleTile(
    vehicle: Vehicle,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(NeonCyan.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (vehicle.vehicleType == "Auto") Icons.Default.DirectionsCar else Icons.Default.TwoWheeler,
                    contentDescription = vehicle.vehicleType,
                    tint = NeonCyan,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = vehicle.name ?: "Unbekanntes Fahrzeug",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = vehicle.description ?: "${vehicle.vehicleType} • Veloce Specs",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Details",
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun RideTile(
    ride: Ride,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Fahrt - ${ride.vehicleType}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
                Text(
                    text = "${ride.distance} km",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(label = "Top-Speed", value = "${ride.highestSpeed.toInt()} km/h")
                StatItem(label = "Schräglage", value = "${ride.highestLeanAngleLeft.toInt()}°")
                StatItem(label = "G-Force", value = "${ride.highestGForce}G")
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 10.sp, color = Color.Gray)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}
