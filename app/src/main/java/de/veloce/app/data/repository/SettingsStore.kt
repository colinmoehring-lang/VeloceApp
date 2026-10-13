package de.veloce.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import de.veloce.app.domain.model.RideDataSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "velo_settings")

@Singleton
class SettingsStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val dataStore = context.settingsDataStore

    val rideDataSource: Flow<RideDataSource> = dataStore.data.map { preferences ->
        when (preferences[DATA_SOURCE]) {
            RideDataSource.ARDUINO.name -> RideDataSource.ARDUINO
            else -> RideDataSource.SIMULATOR
        }
    }

    /**
     * Montage-Offset der Schräglage: der rohe Tilt-Wert bei geradem Motorrad.
     * 0.0 bedeutet, es wurde noch nicht kalibriert.
     */
    val leanOffsetDegrees: Flow<Double> = dataStore.data.map { preferences ->
        preferences[LEAN_OFFSET_DEGREES] ?: 0.0
    }

    val lastDeviceAddress: Flow<String?> = dataStore.data.map { it[LAST_DEVICE_ADDRESS] }
    val lastDeviceName: Flow<String?> = dataStore.data.map { it[LAST_DEVICE_NAME] }

    suspend fun setRideDataSource(source: RideDataSource) {
        dataStore.edit { it[DATA_SOURCE] = source.name }
    }

    suspend fun setLeanOffset(offset: Double) {
        dataStore.edit { it[LEAN_OFFSET_DEGREES] = offset }
    }

    suspend fun clearLeanOffset() {
        dataStore.edit { it.remove(LEAN_OFFSET_DEGREES) }
    }

    suspend fun saveLastConnectedDevice(address: String, name: String) {
        dataStore.edit {
            it[LAST_DEVICE_ADDRESS] = address
            it[LAST_DEVICE_NAME] = name
        }
    }

    private companion object {
        val DATA_SOURCE = stringPreferencesKey("ride_data_source")
        val LEAN_OFFSET_DEGREES = doublePreferencesKey("lean_offset_degrees")
        val LAST_DEVICE_ADDRESS = stringPreferencesKey("last_ble_device_address")
        val LAST_DEVICE_NAME = stringPreferencesKey("last_ble_device_name")
    }
}
