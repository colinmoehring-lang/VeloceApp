package de.veloce.app.presentation.ride

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.veloce.app.data.ride.RideRecordingController
import de.veloce.app.data.ride.RideRecordingState
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

/**
 * Dünne UI-Fassade über den [RideRecordingController].
 *
 * Die eigentliche Aufzeichnung lebt außerhalb der ViewModel (eigener Scope +
 * Foreground-Service), damit sie Bildschirm-Aus und Hintergrund überlebt.
 * Diese Klasse leitet nur Aktionen weiter und reicht den Zustand durch.
 */
@HiltViewModel
class RideViewModel @Inject constructor(
    private val controller: RideRecordingController,
) : ViewModel() {
    val state: StateFlow<RideRecordingState> = controller.state

    fun selectVehicleType(vehicleType: String) = controller.selectVehicleType(vehicleType)

    fun startRecording() = controller.startRecording()

    fun stopRecording() = controller.stopRecording()

    fun retryStop() = controller.retryStop()
}
