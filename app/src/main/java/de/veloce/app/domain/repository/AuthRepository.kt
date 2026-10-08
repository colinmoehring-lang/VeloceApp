package de.veloce.app.domain.repository

import de.veloce.app.domain.model.AuthSession
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeSession(): Flow<AuthSession?>
    suspend fun logIn(userName: String, password: String)
    suspend fun signUp(userName: String, password: String)
    suspend fun logOut()
}
