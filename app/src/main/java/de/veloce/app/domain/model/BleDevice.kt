package de.veloce.app.domain.model

data class BleDevice(
    val name: String?,
    val address: String,
    val rssi: Int,
    val hasVeloceService: Boolean = false,
) {
    val isPreferred: Boolean
        get() = hasVeloceService || name.equals("BT05", ignoreCase = true) ||
            name.equals("HM-10", ignoreCase = true)
}
