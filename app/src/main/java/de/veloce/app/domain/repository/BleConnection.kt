package de.veloce.app.domain.repository

import de.veloce.app.domain.model.BleDevice
import de.veloce.app.domain.model.ConnectionState
import de.veloce.app.domain.model.ParsedBleLine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface BleConnection {
    val state: StateFlow<ConnectionState>
    val lines: Flow<ParsedBleLine>
    suspend fun connect(device: BleDevice)
    fun disconnect()
    suspend fun write(text: String)
}
