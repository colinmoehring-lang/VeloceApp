package de.veloce.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.veloce.app.data.remote.ApiException
import de.veloce.app.domain.model.MapPoint
import de.veloce.app.domain.model.RideHistoryItem
import de.veloce.app.domain.model.UserRecords
import de.veloce.app.domain.model.Vehicle
import de.veloce.app.domain.usecase.GetPersonalRecordsUseCase
import de.veloce.app.domain.usecase.GetRideRouteUseCase
import de.veloce.app.domain.usecase.GetRidesUseCase
import de.veloce.app.domain.usecase.GetVehiclesUseCase
import java.io.IOException
import java.time.format.DateTimeParseException
import kotlinx.serialization.SerializationException
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val records: UserRecords = UserRecords(0.0, 0.0, 0.0, 0.0),
    val vehicles: List<Vehicle> = emptyList(),
    val latestRide: RideHistoryItem? = null,
    val latestRideRoute: List<MapPoint> = emptyList(),
    val isLoadingRides: Boolean = true,
    val isLoadingLatestRideRoute: Boolean = false,
    val recordsError: String? = null,
    val vehiclesError: String? = null,
    val ridesError: String? = null,
    val rideRouteError: String? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getRecords: GetPersonalRecordsUseCase,
    private val getVehicles: GetVehiclesUseCase,
    private val getRides: GetRidesUseCase,
    private val getRideRoute: GetRideRouteUseCase,
) : ViewModel() {
    private val mutableState = MutableStateFlow(HomeUiState())
    val state = mutableState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            mutableState.update {
                it.copy(
                    isLoading = true,
                    isLoadingRides = true,
                    recordsError = null,
                    vehiclesError = null,
                    ridesError = null,
                    rideRouteError = null,
                )
            }
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
            loadLatestRide()
            mutableState.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun loadLatestRide() {
        try {
            val latestRide = getRides().maxByOrNull { it.startedAt }
            mutableState.update {
                it.copy(
                    latestRide = latestRide,
                    latestRideRoute = emptyList(),
                    isLoadingLatestRideRoute = latestRide != null,
                    ridesError = null,
                    rideRouteError = null,
                )
            }
            if (latestRide != null) {
                try {
                    val route = getRideRoute(latestRide.id)
                    mutableState.update { it.copy(latestRideRoute = route) }
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: ApiException) {
                    mutableState.update { it.copy(rideRouteError = exception.toHomeMessage()) }
                } catch (_: SerializationException) {
                    mutableState.update {
                        it.copy(rideRouteError = "Die GPS-Route konnte nicht verarbeitet werden.")
                    }
                } catch (_: IOException) {
                    mutableState.update { it.copy(rideRouteError = "Die GPS-Route konnte nicht geladen werden.") }
                }
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: ApiException) {
            mutableState.update { it.copy(ridesError = exception.toHomeMessage()) }
        } catch (_: SerializationException) {
            mutableState.update { it.copy(ridesError = "Die Fahrtdaten konnten nicht verarbeitet werden.") }
        } catch (_: DateTimeParseException) {
            mutableState.update { it.copy(ridesError = "Das Fahrtdatum konnte nicht verarbeitet werden.") }
        } catch (_: IOException) {
            mutableState.update { it.copy(ridesError = "Fahrten konnten nicht geladen werden.") }
        } finally {
            mutableState.update {
                it.copy(isLoadingRides = false, isLoadingLatestRideRoute = false)
            }
        }
    }

    private fun ApiException.toHomeMessage(): String =
        if (message.isBlank()) "Serverfehler ($statusCode)." else message
}
