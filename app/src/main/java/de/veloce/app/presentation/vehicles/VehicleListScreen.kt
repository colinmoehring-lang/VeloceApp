package de.veloce.app.presentation.vehicles

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.veloce.app.domain.model.Vehicle
import de.veloce.app.presentation.components.StatusChip
import de.veloce.app.presentation.components.VeloceCard
import de.veloce.app.presentation.theme.VeloceColors
import java.io.IOException

@Composable
fun VehicleListScreen(
    onBack: () -> Unit,
    onVehiclesChanged: () -> Unit,
    viewModel: VehicleListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var editingVehicle by remember { mutableStateOf<Vehicle?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var vehicleToDelete by remember { mutableStateOf<Vehicle?>(null) }
    var lastChangeCount by remember { mutableIntStateOf(state.changeCount) }

    LaunchedEffect(state.changeCount) {
        if (state.changeCount > lastChangeCount) {
            showEditor = false
            onVehiclesChanged()
        }
        lastChangeCount = state.changeCount
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VeloceColors.Background)
            .padding(horizontal = 20.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Zurück")
            }
            Text("Fahrzeuge", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
            IconButton(
                onClick = {
                    editingVehicle = null
                    showEditor = true
                },
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Fahrzeug hinzufügen")
            }
        }

        state.error?.let { ErrorText(it, onRetry = viewModel::refresh) }
        when {
            state.isLoading && state.vehicles.isEmpty() -> Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            !state.isLoading && state.error != null && state.vehicles.isEmpty() -> Unit

            state.vehicles.isEmpty() -> EmptyVehicles(onAdd = {
                editingVehicle = null
                showEditor = true
            })

            else -> LazyColumn(
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.vehicles, key = { it.id }) { vehicle ->
                    VehicleListRow(
                        vehicle = vehicle,
                        onEdit = {
                            editingVehicle = vehicle
                            showEditor = true
                        },
                        onDelete = { vehicleToDelete = vehicle },
                    )
                }
            }
        }
    }

    if (showEditor) {
        VehicleEditorDialog(
            vehicle = editingVehicle,
            isSaving = state.isSaving,
            saveError = state.saveError,
            onDismiss = { showEditor = false },
            onSave = { name, description, vehicleType, imageData ->
                viewModel.save(editingVehicle, name, description, vehicleType, imageData)
            },
        )
    }

    vehicleToDelete?.let { vehicle ->
        AlertDialog(
            onDismissRequest = { vehicleToDelete = null },
            title = { Text("Fahrzeug löschen?") },
            text = { Text("„${vehicle.name}“ wird aus deiner Fahrzeugliste entfernt.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.delete(vehicle)
                        vehicleToDelete = null
                    },
                ) { Text("Löschen", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { vehicleToDelete = null }) { Text("Abbrechen") }
            },
        )
    }
}

@Composable
private fun VehicleListRow(
    vehicle: Vehicle,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var showActions by remember { mutableStateOf(false) }
    VeloceCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            VehiclePicture(vehicle.imageData, Modifier.size(56.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, top = 14.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(vehicle.name.ifBlank { "Fahrzeug" }, fontWeight = FontWeight.Medium)
                Text(
                    vehicle.description.ifBlank { vehicle.vehicleType.toGermanType() },
                    color = VeloceColors.Muted,
                    style = MaterialTheme.typography.bodySmall,
                )
                StatusChip(vehicle.vehicleType.toGermanType())
            }
            Box {
                IconButton(onClick = { showActions = true }) {
                    Icon(Icons.Outlined.MoreVert, contentDescription = "Aktionen")
                }
                DropdownMenu(expanded = showActions, onDismissRequest = { showActions = false }) {
                    DropdownMenuItem(
                        text = { Text("Bearbeiten") },
                        leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                        onClick = { showActions = false; onEdit() },
                    )
                    DropdownMenuItem(
                        text = { Text("Löschen") },
                        leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null) },
                        onClick = { showActions = false; onDelete() },
                    )
                }
            }
        }
    }
}

@Composable
private fun VehicleEditorDialog(
    vehicle: Vehicle?,
    isSaving: Boolean,
    saveError: String?,
    onDismiss: () -> Unit,
    onSave: (name: String, description: String, type: String, imageData: String?) -> Unit,
) {
    val context = LocalContext.current
    var name by remember(vehicle) { mutableStateOf(vehicle?.name.orEmpty()) }
    var description by remember(vehicle) { mutableStateOf(vehicle?.description.orEmpty()) }
    var type by remember(vehicle) { mutableStateOf(vehicle?.vehicleType ?: "Car") }
    var imageData by remember(vehicle) { mutableStateOf(vehicle?.imageData) }
    var imageError by remember { mutableStateOf<String?>(null) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: throw IOException("Bilddatei konnte nicht geöffnet werden.")
                imageData = Base64.encodeToString(bytes, Base64.NO_WRAP)
                imageError = null
            } catch (_: IOException) {
                imageError = "Das Bild konnte nicht gelesen werden."
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (vehicle == null) "Fahrzeug hinzufügen" else "Fahrzeug bearbeiten") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Beschreibung") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                )
                Text("Fahrzeugtyp", style = MaterialTheme.typography.labelLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = type == "Car", onClick = { type = "Car" })
                    Text("Auto")
                    Spacer(Modifier.size(12.dp))
                    RadioButton(selected = type == "Motorcycle", onClick = { type = "Motorcycle" })
                    Text("Motorrad")
                }
                OutlinedButton(
                    onClick = { imagePicker.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Outlined.Image, contentDescription = null)
                    Text(
                        if (imageData == null) "Bild auswählen" else "Bild ändern",
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                if (imageData != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        VehiclePicture(imageData, Modifier.size(48.dp))
                        TextButton(onClick = { imageData = null }) { Text("Bild entfernen") }
                    }
                }
                (saveError ?: imageError)?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, description, type, imageData) },
                enabled = !isSaving && name.isNotBlank(),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = VeloceColors.Ink),
            ) {
                if (isSaving) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else Text("Speichern")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Abbrechen") } },
    )
}

@Composable
private fun VehiclePicture(imageData: String?, modifier: Modifier = Modifier) {
    val bitmap = remember(imageData) {
        imageData?.let { encoded ->
            try {
                val bytes = Base64.decode(encoded, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } catch (_: IllegalArgumentException) {
                null
            }
        }
    }
    if (bitmap != null) {
        Image(bitmap = bitmap, contentDescription = "Fahrzeugbild", modifier = modifier)
    } else {
        Box(
            modifier = modifier.background(VeloceColors.Background, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.DirectionsCar, contentDescription = null, tint = VeloceColors.Muted)
        }
    }
}

@Composable
private fun EmptyVehicles(onAdd: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Outlined.DirectionsCar,
            contentDescription = null,
            tint = VeloceColors.Muted,
            modifier = Modifier.size(42.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text("Noch keine Fahrzeuge", style = MaterialTheme.typography.titleMedium)
        Text("Lege dein erstes Fahrzeug an.", color = VeloceColors.Muted)
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onAdd,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = VeloceColors.Ink),
        ) { Text("Fahrzeug hinzufügen") }
    }
}

@Composable
private fun ErrorText(text: String, onRetry: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        Text(text, color = MaterialTheme.colorScheme.error)
        TextButton(onClick = onRetry) { Text("Erneut versuchen") }
    }
}

private fun String.toGermanType(): String = when (this) {
    "Car" -> "Auto"
    "Motorcycle" -> "Motorrad"
    else -> this
}
