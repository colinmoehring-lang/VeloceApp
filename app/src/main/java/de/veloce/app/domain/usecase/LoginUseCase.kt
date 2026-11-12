package de.veloce.app.domain.usecase

import de.veloce.app.domain.repository.AuthRepository

class LoginUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String) {
        require(email.trim().isValidEmail()) { "Bitte gib eine gültige E-Mail-Adresse ein." }
        require(password.isNotBlank()) { "Bitte gib ein Passwort ein." }
        repository.logIn(email.trim(), password)
    }
}

internal fun String.isValidEmail(): Boolean =
    matches(
        Regex(
            "^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@" +
                "[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?" +
                "(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)+$",
        ),
    )
