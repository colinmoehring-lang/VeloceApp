package com.veloce.app.data.bluetooth

import com.veloce.app.domain.model.TelemetryData
import com.veloce.app.domain.repository.BluetoothRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.math.sin

class BluetoothRepositoryImpl : BluetoothRepository {
    private val _isConnected = MutableStateFlow(false)
    private val _telemetryStream = MutableSharedFlow<TelemetryData>(replay = 1)
    private var simulationActive = true // Default simulation true for smooth desktop demo & testing

    private val jsonParser = Json { ignoreUnknownKeys = true; isLenient = true }
    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        startSimulationLoop()
    }

    override fun getTelemetryStream(): Flow<TelemetryData> = _telemetryStream.asSharedFlow()

    override fun isConnected(): Flow<Boolean> = _isConnected.asStateFlow()

    override fun setSimulationMode(enabled: Boolean) {
        simulationActive = enabled
        _isConnected.value = enabled
    }

    override fun isSimulationMode(): Boolean = simulationActive

    override suspend fun connect(deviceAddress: String): Result<Boolean> {
        _isConnected.value = true
        return Result.success(true)
    }

    override suspend fun disconnect() {
        _isConnected.value = false
    }

    fun parseLine(jsonLine: String) {
        try {
            val telemetry = jsonParser.decodeFromString<TelemetryData>(jsonLine)
            scope.launch { _telemetryStream.emit(telemetry) }
        } catch (e: Exception) {
            // Invalid line swallowed
        }
    }

    private fun startSimulationLoop() {
        scope.launch {
            var step = 0
            var baseLat = 51.1234
            var baseLng = 6.8421
            while (true) {
                if (simulationActive) {
                    _isConnected.value = true
                    step++
                    val speed = 40.0 + 25.0 * sin(step * 0.1)
                    val roll = 28.0 * sin(step * 0.15)
                    val g = 1.0 + 0.6 * kotlin.math.abs(sin(step * 0.1))
                    
                    baseLat += 0.00015 * sin(step * 0.05)
                    baseLng += 0.00020 * kotlin.math.cos(step * 0.05)

                    val data = TelemetryData(
                        lat = baseLat,
                        lng = baseLng,
                        spd = (speed * 10).toInt() / 10.0,
                        roll = (roll * 10).toInt() / 10.0,
                        g = (g * 100).toInt() / 100.0
                    )
                    _telemetryStream.emit(data)
                }
                delay(500) // Arduino 500ms interval
            }
        }
    }
}
