package de.veloce.app.domain.usecase

import de.veloce.app.domain.repository.AuthRepository

class SignUpUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(userName: String, password: String) {
        require(userName.isNotBlank()) { "Bitte gib einen Benutzernamen ein." }
        require(password.isNotBlank()) { "Bitte gib ein Passwort ein." }
        repository.signUp(userName.trim(), password)
    }
}
