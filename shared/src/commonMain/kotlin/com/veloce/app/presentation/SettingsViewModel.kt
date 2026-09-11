package com.veloce.app.presentation

import com.veloce.app.data.remote.HttpClientProvider
import com.veloce.app.domain.repository.BluetoothRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(private val bluetoothRepository: BluetoothRepository) {
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _state = MutableStateFlow(
        SettingsState(
            baseUrl = HttpClientProvider.baseUrl,
            isSimulationMode = bluetoothRepository.isSimulationMode()
        )
    )
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        scope.launch {
            bluetoothRepository.isConnected().collect { connected ->
                _state.value = _state.value.copy(isConnected = connected)
            }
        }
    }

    fun setBaseUrl(url: String) {
        HttpClientProvider.baseUrl = url
        _state.value = _state.value.copy(baseUrl = url)
    }

    fun toggleSimulation(enabled: Boolean) {
        bluetoothRepository.setSimulationMode(enabled)
        _state.value = _state.value.copy(isSimulationMode = enabled)
    }
}

data class SettingsState(
    val baseUrl: String = "http://localhost:5000",
    val isSimulationMode: Boolean = true,
    val isConnected: Boolean = false,
    val selectedDeviceName: String = "HC-05 (Arduino Veloce)",
    val speedUnit: String = "km/h",
    val themeMode: String = "Dark Glassmorphism"
)
