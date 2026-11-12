package de.veloce.app.domain.usecase

import de.veloce.app.domain.model.AuthSession
import de.veloce.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class AuthEmailUseCasesTest {
    @Test
    fun `sign up trims email and delegates credentials`() = runBlocking {
        val repository = FakeAuthRepository()

        SignUpUseCase(repository)("  veloce@example.com  ", "StrongPass1!")

        assertEquals("veloce@example.com", repository.email)
        assertEquals("StrongPass1!", repository.password)
    }

    @Test
    fun `sign up rejects password that does not meet server rules`() = runBlocking {
        val repository = FakeAuthRepository()

        val exception = assertThrows(IllegalArgumentException::class.java) {
            runBlocking { SignUpUseCase(repository)("veloce@example.com", "weakpass") }
        }

        assertEquals("Das Passwort muss mindestens einen Großbuchstaben enthalten.", exception.message)
        assertEquals(null, repository.email)
    }

    @Test
    fun `verify email trims email and delegates code`() = runBlocking {
        val repository = FakeAuthRepository()

        VerifyEmailUseCase(repository)(" veloce@example.com ", "123456")

        assertEquals("veloce@example.com", repository.email)
        assertEquals("123456", repository.code)
    }

    private class FakeAuthRepository : AuthRepository {
        var email: String? = null
        var password: String? = null
        var code: String? = null

        override fun observeSession(): Flow<AuthSession?> = flowOf(null)
        override suspend fun logIn(email: String, password: String) = Unit
        override suspend fun signUp(email: String, password: String) {
            this.email = email
            this.password = password
        }
        override suspend fun verifyEmail(email: String, code: String) {
            this.email = email
            this.code = code
        }
        override suspend fun logOut() = Unit
    }
}
