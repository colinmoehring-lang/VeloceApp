package de.veloce.app.domain.usecase

import de.veloce.app.domain.repository.AuthRepository

class VerifyEmailUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(email: String, code: String) {
        require(email.trim().isValidEmail()) { "Bitte gib eine gültige E-Mail-Adresse ein." }
        require(code.matches(Regex("[0-9]{6}"))) { "Bitte gib den 6-stelligen Verifizierungscode ein." }
        repository.verifyEmail(email.trim(), code)
    }
}
