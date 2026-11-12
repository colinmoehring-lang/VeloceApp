package de.veloce.app.domain.repository

import de.veloce.app.domain.model.AuthSession
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeSession(): Flow<AuthSession?>
    suspend fun logIn(email: String, password: String)
    suspend fun signUp(email: String, password: String)
    suspend fun verifyEmail(email: String, code: String)
    suspend fun logOut()
}
