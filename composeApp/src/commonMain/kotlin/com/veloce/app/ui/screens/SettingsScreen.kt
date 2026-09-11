package com.veloce.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veloce.app.presentation.SettingsViewModel
import com.veloce.app.ui.theme.*

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    onLogout: () -> Unit
) {
    val state by settingsViewModel.state.collectAsState()
    var urlText by remember { mutableStateOf(state.baseUrl) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "Einstellungen",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Bluetooth, Backend & App Konfiguration",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Bluetooth Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Bluetooth, contentDescription = null, tint = NeonCyan)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Bluetooth Hardware", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(state.selectedDeviceName, fontSize = 12.sp, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Status", color = Color.White, fontSize = 14.sp)
                    Text(
                        text = if (state.isConnected) "VERBUNDEN" else "GETRENNT",
                        fontWeight = FontWeight.Bold,
                        color = if (state.isConnected) ElectricLime else NeonOrange,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Telemetrie-Simulator", color = Color.White, fontSize = 14.sp)
                        Text("Simuliert 500ms Daten ohne echten Arduino", color = Color.Gray, fontSize = 10.sp)
                    }
                    Switch(
                        checked = state.isSimulationMode,
                        onCheckedChange = { settingsViewModel.toggleSimulation(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Backend URL Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Dns, contentDescription = null, tint = NeonCyan)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Backend Base URL", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = urlText,
                    onValueChange = {
                        urlText = it
                        settingsViewModel.setBaseUrl(it)
                    },
                    label = { Text("Base Host URL") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Logout Button
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeonOrange.copy(alpha = 0.2f), contentColor = NeonOrange)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = "Abmelden")
            Spacer(modifier = Modifier.width(8.dp))
            Text("ABMELDEN", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
