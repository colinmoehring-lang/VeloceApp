package de.veloce.app.data.repository

import de.veloce.app.data.mapper.toDomain
import de.veloce.app.data.remote.VeloceApi
import de.veloce.app.data.remote.toApiException
import de.veloce.app.data.remote.dto.LogInRequestDto
import de.veloce.app.data.remote.dto.SignUpRequestDto
import de.veloce.app.data.remote.dto.VerifyEmailRequestDto
import de.veloce.app.data.remote.ApiException
import de.veloce.app.domain.model.AuthSession
import de.veloce.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: VeloceApi,
    private val sessionStore: SessionStore,
) : AuthRepository {
    override fun observeSession(): Flow<AuthSession?> = sessionStore.observeSession()

    override suspend fun logIn(email: String, password: String) {
        try {
            sessionStore.save(api.logIn(LogInRequestDto(email, password)).toDomain())
        } catch (exception: HttpException) {
            throw exception.toApiException()
        }
    }

    override suspend fun signUp(email: String, password: String) {
        try {
            val response = api.signUp(SignUpRequestDto(email, password))
            if (!response.success) {
                throw ApiException(400, response.message.ifBlank { "Registrierung fehlgeschlagen." })
            }
        } catch (exception: HttpException) {
            throw exception.toApiException()
        }
    }

    override suspend fun verifyEmail(email: String, code: String) {
        try {
            val response = api.verifyEmail(VerifyEmailRequestDto(email, code))
            if (!response.success) {
                throw ApiException(400, response.message.ifBlank { "E-Mail-Verifizierung fehlgeschlagen." })
            }
        } catch (exception: HttpException) {
            throw exception.toApiException()
        }
    }

    override suspend fun logOut() {
        sessionStore.clear()
    }
}
