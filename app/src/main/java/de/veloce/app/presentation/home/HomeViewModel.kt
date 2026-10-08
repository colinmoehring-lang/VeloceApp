package de.veloce.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.veloce.app.data.remote.ApiException
import de.veloce.app.domain.model.UserRecords
import de.veloce.app.domain.model.Vehicle
import de.veloce.app.domain.usecase.GetPersonalRecordsUseCase
import de.veloce.app.domain.usecase.GetVehiclesUseCase
import java.io.IOException
import kotlinx.serialization.SerializationException
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val records: UserRecords = UserRecords(0.0, 0.0, 0.0, 0.0),
    val vehicles: List<Vehicle> = emptyList(),
    val recordsError: String? = null,
    val vehiclesError: String? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getRecords: GetPersonalRecordsUseCase,
    private val getVehicles: GetVehiclesUseCase,
) : ViewModel() {
    private val mutableState = MutableStateFlow(HomeUiState())
    val state = mutableState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            mutableState.update { it.copy(isLoading = true, recordsError = null, vehiclesError = null) }
            try {
                val records = getRecords()
                mutableState.update { it.copy(records = records) }
            } catch (exception: ApiException) {
                mutableState.update { it.copy(recordsError = exception.toHomeMessage()) }
            } catch (_: SerializationException) {
                mutableState.update { it.copy(recordsError = "Die Rekorddaten konnten nicht verarbeitet werden.") }
            } catch (_: IOException) {
                mutableState.update { it.copy(recordsError = "Rekorde konnten nicht geladen werden.") }
            }
            try {
                val vehicles = getVehicles()
                mutableState.update { it.copy(vehicles = vehicles) }
            } catch (exception: ApiException) {
                mutableState.update { it.copy(vehiclesError = exception.toHomeMessage()) }
            } catch (_: SerializationException) {
                mutableState.update { it.copy(vehiclesError = "Die Fahrzeugdaten konnten nicht verarbeitet werden.") }
            } catch (_: IOException) {
                mutableState.update { it.copy(vehiclesError = "Fahrzeuge konnten nicht geladen werden.") }
            }
            mutableState.update { it.copy(isLoading = false) }
        }
    }

    private fun ApiException.toHomeMessage(): String =
        if (message.isBlank()) "Serverfehler ($statusCode)." else message
}
