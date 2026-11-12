package de.veloce.app.domain.usecase

import de.veloce.app.domain.model.AuthSession
import de.veloce.app.domain.repository.AuthRepository
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class LoginUseCaseTest {
    @Test
    fun `trims email and delegates credentials`() = runBlocking {
        val repository = FakeAuthRepository()

        LoginUseCase(repository)("  veloce@example.com  ", "secret")

        assertEquals("veloce@example.com", repository.email)
        assertEquals("secret", repository.password)
    }

    private class FakeAuthRepository : AuthRepository {
        var email: String? = null
        var password: String? = null

        override fun observeSession(): Flow<AuthSession?> = flowOf(null)
        override suspend fun logIn(email: String, password: String) {
            this.email = email
            this.password = password
        }
        override suspend fun signUp(email: String, password: String) = Unit
        override suspend fun verifyEmail(email: String, code: String) = Unit
        override suspend fun logOut() = Unit
    }
}
