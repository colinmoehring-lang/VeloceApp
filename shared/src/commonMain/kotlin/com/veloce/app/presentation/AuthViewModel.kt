package com.veloce.app.presentation

import com.veloce.app.domain.model.*
import com.veloce.app.domain.repository.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) {
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    init {
        val savedToken = authRepository.getSavedToken()
        if (!savedToken.isNullOrEmpty()) {
            _state.value = _state.value.copy(isLoggedIn = true)
        }
    }

    fun login(username: String, password: String) {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            authRepository.login(username, password)
                .onSuccess { tokens ->
                    _state.value = _state.value.copy(isLoading = false, isLoggedIn = true, userName = tokens.userName)
                }
                .onFailure { ex ->
                    _state.value = _state.value.copy(isLoading = false, error = ex.message ?: "Login failed")
                }
        }
    }

    fun signUp(username: String, password: String) {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            authRepository.signUp(username, password)
                .onSuccess {
                    _state.value = _state.value.copy(isLoading = false, message = "Registrierung erfolgreich! Bitte anmelden.")
                }
                .onFailure { ex ->
                    _state.value = _state.value.copy(isLoading = false, error = ex.message ?: "Registrierung fehlgeschlagen")
                }
        }
    }

    fun logout() {
        authRepository.logout()
        _state.value = AuthState()
    }
}

data class AuthState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val userName: String = "",
    val error: String? = null,
    val message: String? = null
)
