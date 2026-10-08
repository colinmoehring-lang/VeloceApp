package de.veloce.app.presentation.settings

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.SettingsBluetooth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import de.veloce.app.domain.model.BleDevice
import de.veloce.app.domain.model.ConnectionState
import de.veloce.app.domain.model.RideDataSource
import de.veloce.app.presentation.components.ListRow
import de.veloce.app.presentation.components.SectionHeader
import de.veloce.app.presentation.components.StatusChip
import de.veloce.app.presentation.components.VeloceCard
import de.veloce.app.presentation.theme.VeloceColors

@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var bluetoothEnabled by remember { mutableStateOf(viewModel.isBluetoothEnabled()) }
    var requestedPermissions by rememberSaveable { mutableStateOf(false) }
    var permissionGranted by remember { mutableStateOf(hasBlePermissions(context)) }
    var command by rememberSaveable { mutableStateOf("") }
    val linesState = rememberLazyListState()
    val packageUri = Uri.parse("package:${context.packageName}")

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
        requestedPermissions = true
        permissionGranted = results.values.all { it } || hasBlePermissions(context)
    }
    val bluetoothLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        bluetoothEnabled = viewModel.isBluetoothEnabled()
    }

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                bluetoothEnabled = viewModel.isBluetoothEnabled()
                permissionGranted = hasBlePermissions(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.stopScan()
        }
    }

    LaunchedEffect(state.terminalLines.size, state.terminalExpanded, state.terminalPaused) {
        if (state.terminalExpanded && !state.terminalPaused && state.terminalLines.isNotEmpty()) {
            linesState.animateScrollToItem(state.terminalLines.lastIndex)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(VeloceColors.Background),
        contentPadding = PaddingValues(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Einstellungen", style = MaterialTheme.typography.headlineMedium)
                Text("Bluetooth-Verbindung und Fahrtdaten", color = VeloceColors.Muted)
            }
        }

        item { SectionHeader("Datenquelle") }
        item {
            VeloceCard {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    DataSourceRow(
                        title = "Arduino (Bluetooth)",
                        description = "Aus: Simulator · An: Messwerte vom BT05 / HM-10",
                        selected = state.dataSource == RideDataSource.ARDUINO,
                        onSelect = {
                            viewModel.setDataSource(
                                if (state.dataSource == RideDataSource.ARDUINO) {
                                    RideDataSource.SIMULATOR
                                } else {
                                    RideDataSource.ARDUINO
                                },
                            )
                        },
                    )
                }
            }
        }

        item { SectionHeader("Bluetooth") }
        item {
            VeloceCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ConnectionStatus(state.connectionState, state.isScanning)
                    if (!permissionGranted) {
                        Text(
                            "Veloce benötigt die Berechtigung „Geräte in der Nähe“, um BLE-Geräte zu finden und eine Verbindung herzustellen. Standort wird nicht für die Bluetooth-Suche verwendet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = VeloceColors.Muted,
                        )
                        if (requestedPermissions && !shouldShowRationale()) {
                            Text(
                                "Die Berechtigung wurde dauerhaft abgelehnt. Du kannst sie in den App-Einstellungen wieder aktivieren.",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Button(
                                onClick = {
                                    context.startActivity(
                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri),
                                    )
                                },
                                shape = RoundedCornerShape(50),
                            ) { Text("App-Einstellungen öffnen") }
                        } else {
                            Button(
                                onClick = {
                                    permissionLauncher.launch(requiredBlePermissions())
                                },
                                shape = RoundedCornerShape(50),
                            ) { Text("Berechtigung erteilen") }
                        }
                    } else if (!bluetoothEnabled) {
                        Text(
                            "Bluetooth ist ausgeschaltet. Aktiviere Bluetooth, um nach deinem Arduino zu suchen.",
                            color = VeloceColors.Muted,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Button(
                            onClick = {
                                bluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                            },
                            shape = RoundedCornerShape(50),
                        ) { Text("Bluetooth einschalten") }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Nur Geräte mit Namen", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = state.onlyNamedDevices,
                                onCheckedChange = viewModel::setOnlyNamedDevices,
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Button(
                                onClick = viewModel::startScan,
                                enabled = !state.isScanning && state.connectionState !is ConnectionState.Connecting,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(containerColor = VeloceColors.Ink),
                            ) {
                                if (state.isScanning) {
                                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                                    Text(" Suchen …")
                                } else {
                                    Icon(Icons.Outlined.Bluetooth, contentDescription = null)
                                    Text(" Suchen")
                                }
                            }
                            if (state.connectionState is ConnectionState.Connected) {
                                TextButton(onClick = viewModel::disconnect) { Text("Trennen") }
                            }
                        }
                        val lastAddress = state.lastDeviceAddress
                        if (
                            lastAddress != null &&
                            state.connectionState !is ConnectionState.Connected &&
                            state.connectionState !is ConnectionState.Connecting
                        ) {
                            ListRow(
                                title = state.lastDeviceName ?: "Zuletzt verbunden",
                                subtitle = lastAddress,
                                icon = Icons.Outlined.SettingsBluetooth,
                                onClick = viewModel::connectToLastDevice,
                            )
                        }
                    }
                    state.scanError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        if (permissionGranted && bluetoothEnabled && state.devices.isNotEmpty()) {
            item { SectionHeader("Gefundene Geräte") }
            item {
                VeloceCard {
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        state.devices.forEachIndexed { index, device ->
                            DeviceRow(
                                device = device,
                                enabled = state.connectionState !is ConnectionState.Connecting,
                                onClick = { viewModel.connect(device) },
                            )
                            if (index < state.devices.lastIndex) {
                                HorizontalDivider(color = VeloceColors.Line)
                            }
                        }
                    }
                }
            }
        } else if (permissionGranted && bluetoothEnabled && state.isScanning && state.devices.isEmpty()) {
            item {
                Text("Suche nach BLE-Geräten in der Nähe …", color = VeloceColors.Muted)
            }
        } else if (
            permissionGranted &&
            bluetoothEnabled &&
            !state.isScanning &&
            state.devices.isEmpty() &&
            state.scanError == null
        ) {
            item {
                Text("Noch keine Geräte gefunden. Starte eine neue Suche.", color = VeloceColors.Muted)
            }
        }

        if (state.connectionState is ConnectionState.Connected) {
            item { SectionHeader("Terminal (Debug)", onAction = viewModel::toggleTerminalExpanded) }
            item {
                TerminalCard(
                    state = state,
                    listState = linesState,
                    command = command,
                    onCommandChange = { command = it },
                    onSend = {
                        viewModel.sendText(command)
                        command = ""
                    },
                    onPause = viewModel::toggleTerminalPaused,
                    onClear = viewModel::clearTerminal,
                    onToggleExpanded = viewModel::toggleTerminalExpanded,
                )
            }
        }

        item {
            TextButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
                Text("Abmelden", color = VeloceColors.Muted)
            }
        }
    }
}

@Composable
private fun ConnectionStatus(state: ConnectionState, isScanning: Boolean) {
    val (text, background, foreground) = when (state) {
        ConnectionState.Disconnected -> Triple(
            if (isScanning) "Suche läuft" else "Nicht verbunden",
            VeloceColors.Background,
            VeloceColors.Muted,
        )
        ConnectionState.Scanning -> Triple("Suche läuft", VeloceColors.PaleOrange, VeloceColors.Orange)
        ConnectionState.Connecting -> Triple("Verbinde …", VeloceColors.PaleOrange, VeloceColors.Orange)
        is ConnectionState.Connected -> Triple(
            "Verbunden · ${state.deviceName}",
            VeloceColors.PaleGreen,
            VeloceColors.Green,
        )
        is ConnectionState.Error -> Triple("Verbindungsfehler", VeloceColors.PaleOrange, VeloceColors.Error)
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        StatusChip(text, background = background, foreground = foreground)
        if (state is ConnectionState.Error) {
            Text(state.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun DataSourceRow(
    title: String,
    description: String,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, fontWeight = FontWeight.Medium)
            Text(description, color = VeloceColors.Muted, style = MaterialTheme.typography.bodySmall)
        }
        Switch(checked = selected, onCheckedChange = { onSelect() })
    }
}

@Composable
private fun DeviceRow(device: BleDevice, enabled: Boolean, onClick: () -> Unit) {
    ListRow(
        title = device.name?.takeIf(String::isNotBlank) ?: "Unbekannt",
        subtitle = "${device.address} · ${device.rssi} dBm",
        icon = Icons.Outlined.Bluetooth,
        onClick = if (enabled) onClick else null,
        trailing = {
            StatusChip(
                text = when {
                    device.hasVeloceService -> "FFE0"
                    device.isPreferred -> "BT05"
                    else -> "Verbinden"
                },
                background = if (device.isPreferred) VeloceColors.PaleOrange else VeloceColors.Background,
                foreground = if (device.isPreferred) VeloceColors.Orange else VeloceColors.Muted,
            )
        },
    )
}

@Composable
private fun TerminalCard(
    state: SettingsUiState,
    listState: androidx.compose.foundation.lazy.LazyListState,
    command: String,
    onCommandChange: (String) -> Unit,
    onSend: () -> Unit,
    onPause: () -> Unit,
    onClear: () -> Unit,
    onToggleExpanded: () -> Unit,
) {
    val latest = state.latestTelemetry
    VeloceCard {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("BLE-Datenstrom", fontWeight = FontWeight.Medium)
                IconButton(onClick = onToggleExpanded) {
                    Icon(
                        if (state.terminalExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        contentDescription = if (state.terminalExpanded) "Einklappen" else "Ausklappen",
                    )
                }
            }
            Text(
                "Gültig: ${state.validLineCount} · Ungültig: ${state.invalidLineCount}",
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                latest?.let {
                    "Speed ${it.speedKmh.display()} km/h · Tilt ${it.leanDegrees.display()}° · " +
                        "G ${it.gForce.display()} · Lat ${it.latitude.display()} · Lng ${it.longitude.display()}"
                } ?: "Noch keine gültigen Messwerte.",
                style = MaterialTheme.typography.bodySmall,
                color = VeloceColors.Muted,
            )

            if (state.terminalExpanded) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onPause) {
                        Text(if (state.terminalPaused) "Fortsetzen" else "Pause")
                    }
                    TextButton(onClick = onClear) {
                        Icon(Icons.Outlined.DeleteOutline, contentDescription = null)
                        Text(" Leeren")
                    }
                }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = androidx.compose.ui.graphics.Color(0xFF17191D),
                ) {
                    if (state.terminalLines.isEmpty()) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("Warte auf Daten …", color = androidx.compose.ui.graphics.Color.LightGray)
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            items(state.terminalLines) { line ->
                                Text(
                                    "${line.displayTimestamp()}  ${line.rawLine}",
                                    color = if (line.isValid) {
                                        androidx.compose.ui.graphics.Color(0xFFE2E4E8)
                                    } else {
                                        androidx.compose.ui.graphics.Color(0xFFFF7777)
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }
                Text(
                    "Der Arduino empfängt derzeit keine Befehle. Das Feld ist nur zum Testen des Bluetooth-Moduls gedacht.",
                    color = VeloceColors.Muted,
                    style = MaterialTheme.typography.bodySmall,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = command,
                        onValueChange = onCommandChange,
                        modifier = Modifier.weight(1f),
                        label = { Text("Text senden") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { onSend() }),
                    )
                    IconButton(onClick = onSend, enabled = command.isNotBlank()) {
                        Icon(Icons.Outlined.Send, contentDescription = "Senden")
                    }
                }
                state.writeError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun shouldShowRationale(): Boolean {
    val context = LocalContext.current
    val activity = context.findActivity() ?: return false
    return requiredBlePermissions().any {
        androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
    }
}

private fun hasBlePermissions(context: Context): Boolean = requiredBlePermissions().all { permission ->
    ContextCompat.checkSelfPermission(
        context,
        permission,
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
}

private fun requiredBlePermissions(): Array<String> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
} else {
    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
}

private tailrec fun android.content.Context.findActivity(): android.app.Activity? = when (this) {
    is android.app.Activity -> this
    is android.content.ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun Double.display(): String = String.format(java.util.Locale.ROOT, "%.2f", this)
