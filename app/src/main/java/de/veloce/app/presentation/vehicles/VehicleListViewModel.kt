package de.veloce.app.presentation.vehicles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.veloce.app.data.remote.ApiException
import de.veloce.app.domain.model.Vehicle
import de.veloce.app.domain.usecase.CreateVehicleUseCase
import de.veloce.app.domain.usecase.DeleteVehicleUseCase
import de.veloce.app.domain.usecase.GetVehiclesUseCase
import de.veloce.app.domain.usecase.UpdateVehicleUseCase
import java.io.IOException
import kotlinx.serialization.SerializationException
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VehicleListUiState(
    val isLoading: Boolean = true,
    val vehicles: List<Vehicle> = emptyList(),
    val error: String? = null,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val changeCount: Int = 0,
)

@HiltViewModel
class VehicleListViewModel @Inject constructor(
    private val getVehicles: GetVehiclesUseCase,
    private val createVehicle: CreateVehicleUseCase,
    private val updateVehicle: UpdateVehicleUseCase,
    private val deleteVehicle: DeleteVehicleUseCase,
) : ViewModel() {
    private val mutableState = MutableStateFlow(VehicleListUiState())
    val state = mutableState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            mutableState.update { it.copy(isLoading = true, error = null) }
            try {
                val vehicles = getVehicles()
                mutableState.update { it.copy(vehicles = vehicles, isLoading = false) }
            } catch (exception: ApiException) {
                mutableState.update { it.copy(isLoading = false, error = exception.toDisplayMessage()) }
            } catch (_: SerializationException) {
                mutableState.update {
                    it.copy(isLoading = false, error = "Die Fahrzeugdaten konnten nicht verarbeitet werden.")
                }
            } catch (_: IOException) {
                mutableState.update {
                    it.copy(isLoading = false, error = "Fahrzeuge konnten nicht geladen werden. Bitte erneut versuchen.")
                }
            }
        }
    }

    fun save(
        vehicle: Vehicle?,
        name: String,
        description: String,
        vehicleType: String,
        imageData: String?,
    ) {
        mutableState.update { it.copy(isSaving = true, saveError = null) }
        viewModelScope.launch {
            try {
                if (vehicle == null) {
                    createVehicle(name, description, vehicleType, imageData)
                } else {
                    updateVehicle(vehicle.id, name, description, vehicleType, imageData)
                }
                mutableState.update {
                    it.copy(
                        isSaving = false,
                        changeCount = it.changeCount + 1,
                    )
                }
                refresh()
            } catch (_: SerializationException) {
                mutableState.update {
                    it.copy(isSaving = false, saveError = "Die Serverantwort konnte nicht verarbeitet werden.")
                }
            } catch (exception: IllegalArgumentException) {
                mutableState.update { it.copy(isSaving = false, saveError = exception.message) }
            } catch (exception: ApiException) {
                mutableState.update { it.copy(isSaving = false, saveError = exception.toDisplayMessage()) }
            } catch (_: IOException) {
                mutableState.update {
                    it.copy(isSaving = false, saveError = "Änderungen konnten nicht gespeichert werden.")
                }
            }
        }
    }

    fun delete(vehicle: Vehicle) {
        mutableState.update { it.copy(error = null) }
        viewModelScope.launch {
            try {
                deleteVehicle(vehicle.id)
                mutableState.update {
                    it.copy(
                        changeCount = it.changeCount + 1,
                    )
                }
                refresh()
            } catch (exception: ApiException) {
                mutableState.update { it.copy(error = exception.toDisplayMessage()) }
            } catch (_: SerializationException) {
                mutableState.update { it.copy(error = "Die Serverantwort konnte nicht verarbeitet werden.") }
            } catch (_: IOException) {
                mutableState.update { it.copy(error = "Fahrzeug konnte nicht gelöscht werden.") }
            }
        }
    }

    private fun ApiException.toDisplayMessage(): String =
        if (message.isBlank()) "Serverfehler ($statusCode)." else "Serverfehler ($statusCode): $message"
}
