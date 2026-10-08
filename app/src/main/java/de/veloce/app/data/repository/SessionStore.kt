package de.veloce.app.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.Preferences
import de.veloce.app.domain.model.AuthSession
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.authDataStore by preferencesDataStore(name = "velo_auth")

@Singleton
class SessionStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val dataStore = context.authDataStore

    fun observeSession(): Flow<AuthSession?> = dataStore.data.map { preferences ->
        preferences.toSession()
    }

    suspend fun save(session: AuthSession) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = session.accessToken
            preferences[USER_NAME] = session.userName
            preferences[USER_ID] = session.userId.toString()
            preferences[EXPIRES_IN] = session.expiresInSeconds
        }
    }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    private fun Preferences.toSession(): AuthSession? {
        val token = this[ACCESS_TOKEN] ?: return null
        val userName = this[USER_NAME] ?: return null
        val id = this[USER_ID] ?: return null
        val userId = try {
            UUID.fromString(id)
        } catch (_: IllegalArgumentException) {
            return null
        }
        return AuthSession(token, this[EXPIRES_IN] ?: 0, userId, userName)
    }

    private companion object {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_ID = stringPreferencesKey("user_id")
        val EXPIRES_IN = intPreferencesKey("expires_in_seconds")
    }
}
