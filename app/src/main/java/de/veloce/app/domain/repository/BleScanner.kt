package de.veloce.app.domain.repository

import de.veloce.app.domain.model.BleDevice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface BleScanner {
    val isScanning: StateFlow<Boolean>
    fun scan(): Flow<BleDevice>
    fun stopScan()
    fun isBluetoothEnabled(): Boolean
}
