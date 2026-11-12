package de.veloce.app.presentation.rides

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.veloce.app.data.remote.ApiException
import de.veloce.app.domain.model.MapPoint
import de.veloce.app.domain.model.RideHistoryItem
import de.veloce.app.domain.usecase.GetRideRouteUseCase
import de.veloce.app.domain.usecase.GetRidesUseCase
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException

data class RideListUiState(
    val isLoading: Boolean = true,
    val rides: List<RideHistoryItem> = emptyList(),
    val error: String? = null,
    val expandedRideId: String? = null,
    val expandedRoute: List<MapPoint> = emptyList(),
    val isRouteLoading: Boolean = false,
    val routeError: String? = null,
)

@HiltViewModel
class RideListViewModel @Inject constructor(
    private val getRides: GetRidesUseCase,
    private val getRideRoute: GetRideRouteUseCase,
) : ViewModel() {
    private val mutableState = MutableStateFlow(RideListUiState())
    val state = mutableState.asStateFlow()
    private var routeJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            mutableState.update { it.copy(isLoading = true, error = null) }
            try {
                val rides = getRides()
                mutableState.update { it.copy(isLoading = false, rides = rides) }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: ApiException) {
                mutableState.update { it.copy(isLoading = false, error = exception.message) }
            } catch (_: SerializationException) {
                mutableState.update {
                    it.copy(isLoading = false, error = "Die Fahrtdaten konnten nicht verarbeitet werden.")
                }
            } catch (_: IOException) {
                mutableState.update {
                    it.copy(isLoading = false, error = "Fahrten konnten nicht geladen werden.")
                }
            }
        }
    }

    fun toggleRoute(rideId: String) {
        routeJob?.cancel()
        if (mutableState.value.expandedRideId == rideId) {
            mutableState.update {
                it.copy(
                    expandedRideId = null,
                    expandedRoute = emptyList(),
                    isRouteLoading = false,
                    routeError = null,
                )
            }
            return
        }

        mutableState.update {
            it.copy(
                expandedRideId = rideId,
                expandedRoute = emptyList(),
                isRouteLoading = true,
                routeError = null,
            )
        }
        routeJob = viewModelScope.launch {
            try {
                val route = getRideRoute(rideId)
                mutableState.update {
                    if (it.expandedRideId == rideId) {
                        it.copy(isRouteLoading = false, expandedRoute = route)
                    } else {
                        it
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: ApiException) {
                mutableState.update {
                    if (it.expandedRideId == rideId) {
                        it.copy(isRouteLoading = false, routeError = exception.message)
                    } else {
                        it
                    }
                }
            } catch (_: SerializationException) {
                mutableState.update {
                    if (it.expandedRideId == rideId) {
                        it.copy(
                            isRouteLoading = false,
                            routeError = "Die GPS-Route konnte nicht verarbeitet werden.",
                        )
                    } else {
                        it
                    }
                }
            } catch (_: IOException) {
                mutableState.update {
                    if (it.expandedRideId == rideId) {
                        it.copy(isRouteLoading = false, routeError = "Die GPS-Route konnte nicht geladen werden.")
                    } else {
                        it
                    }
                }
            }
        }
    }
}
