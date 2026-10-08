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
    fun `trims username and delegates credentials`() = runBlocking {
        val repository = FakeAuthRepository()

        LoginUseCase(repository)("  veloce-user  ", "secret")

        assertEquals("veloce-user", repository.userName)
        assertEquals("secret", repository.password)
    }

    private class FakeAuthRepository : AuthRepository {
        var userName: String? = null
        var password: String? = null

        override fun observeSession(): Flow<AuthSession?> = flowOf(null)
        override suspend fun logIn(userName: String, password: String) {
            this.userName = userName
            this.password = password
        }
        override suspend fun signUp(userName: String, password: String) = Unit
        override suspend fun logOut() = Unit
    }
}
