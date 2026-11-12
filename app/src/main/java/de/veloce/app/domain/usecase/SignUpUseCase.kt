package de.veloce.app.domain.usecase

import de.veloce.app.domain.repository.AuthRepository

class SignUpUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String) {
        require(email.trim().isValidEmail()) { "Bitte gib eine gültige E-Mail-Adresse ein." }
        require(password.length in 8..128) { "Das Passwort muss zwischen 8 und 128 Zeichen lang sein." }
        require(password.any { it in 'A'..'Z' }) { "Das Passwort muss mindestens einen Großbuchstaben enthalten." }
        require(password.any { it in 'a'..'z' }) { "Das Passwort muss mindestens einen Kleinbuchstaben enthalten." }
        require(password.any { it in '0'..'9' }) { "Das Passwort muss mindestens eine Ziffer enthalten." }
        require(password.any { it in "!@#\$%^&*()_+-=[]{};':\",.<>?/\\|~" }) {
            "Das Passwort muss mindestens ein Sonderzeichen enthalten."
        }
        repository.signUp(email.trim(), password)
    }
}
