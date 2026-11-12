package de.veloce.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.veloce.app.data.remote.ApiException
import kotlinx.serialization.SerializationException
import de.veloce.app.domain.model.AuthSession
import de.veloce.app.domain.repository.AuthRepository
import de.veloce.app.domain.usecase.LoginUseCase
import de.veloce.app.domain.usecase.SignUpUseCase
import de.veloce.app.domain.usecase.VerifyEmailUseCase
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isCheckingSession: Boolean = true,
    val session: AuthSession? = null,
    val isBusy: Boolean = false,
    val isSignUp: Boolean = false,
    val isVerificationStep: Boolean = false,
    val email: String = "",
    val error: String? = null,
    val message: String? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val login: LoginUseCase,
    private val signUp: SignUpUseCase,
    private val verifyEmail: VerifyEmailUseCase,
    private val repository: AuthRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(AuthUiState())
    val state = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeSession().collect { session ->
                mutableState.update {
                    it.copy(isCheckingSession = false, session = session)
                }
            }
        }
    }

    fun updateEmail(email: String) {
        mutableState.update { it.copy(email = email.trim(), error = null) }
    }

    fun submit(password: String) {
        mutableState.update { it.copy(isBusy = true, error = null, message = null) }
        viewModelScope.launch {
            try {
                if (mutableState.value.isSignUp) {
                    signUp(mutableState.value.email, password)
                    mutableState.update {
                        it.copy(
                            isBusy = false,
                            isSignUp = false,
                            isVerificationStep = true,
                            message = "Wir haben einen 6-stelligen Code an deine E-Mail-Adresse gesendet.",
                        )
                    }
                } else {
                    login(mutableState.value.email, password)
                    mutableState.update { it.copy(isBusy = false) }
                }
            } catch (_: SerializationException) {
                mutableState.update {
                    it.copy(isBusy = false, error = "Die Serverantwort konnte nicht verarbeitet werden.")
                }
            } catch (exception: IllegalArgumentException) {
                mutableState.update { it.copy(isBusy = false, error = exception.message) }
            } catch (exception: ApiException) {
                if (
                    exception.statusCode == 401 &&
                    (exception.message.contains("not verified", ignoreCase = true) ||
                        exception.message.contains("verify your email", ignoreCase = true) ||
                        exception.message.contains("nicht verifiziert", ignoreCase = true))
                ) {
                    mutableState.update {
                        it.copy(
                            isBusy = false,
                            isVerificationStep = true,
                            error = null,
                            message = "Deine E-Mail-Adresse ist noch nicht verifiziert. Gib den Code aus der E-Mail ein.",
                        )
                    }
                    return@launch
                }
                mutableState.update {
                    it.copy(isBusy = false, error = exception.toDisplayMessage())
                }
            } catch (exception: IOException) {
                mutableState.update {
                    it.copy(isBusy = false, error = "Keine Verbindung zum Server. Bitte versuche es erneut.")
                }
            }
        }
    }

    fun verifyCode(code: String) {
        mutableState.update { it.copy(isBusy = true, error = null, message = null) }
        viewModelScope.launch {
            try {
                verifyEmail(mutableState.value.email, code.trim())
                mutableState.update {
                    it.copy(
                        isBusy = false,
                        isVerificationStep = false,
                        isSignUp = false,
                        message = "E-Mail bestätigt. Du kannst dich jetzt anmelden.",
                    )
                }
            } catch (_: SerializationException) {
                mutableState.update {
                    it.copy(isBusy = false, error = "Die Serverantwort konnte nicht verarbeitet werden.")
                }
            } catch (exception: IllegalArgumentException) {
                mutableState.update { it.copy(isBusy = false, error = exception.message) }
            } catch (exception: ApiException) {
                mutableState.update { it.copy(isBusy = false, error = exception.toDisplayMessage()) }
            } catch (exception: IOException) {
                mutableState.update {
                    it.copy(isBusy = false, error = "Keine Verbindung zum Server. Bitte versuche es erneut.")
                }
            }
        }
    }

    fun resendVerificationCode(password: String) {
        mutableState.update { it.copy(isBusy = true, error = null, message = null) }
        viewModelScope.launch {
            try {
                signUp(mutableState.value.email, password)
                mutableState.update {
                    it.copy(
                        isBusy = false,
                        message = "Ein neuer Verifizierungscode wurde an deine E-Mail-Adresse gesendet.",
                    )
                }
            } catch (_: SerializationException) {
                mutableState.update {
                    it.copy(isBusy = false, error = "Die Serverantwort konnte nicht verarbeitet werden.")
                }
            } catch (exception: IllegalArgumentException) {
                mutableState.update { it.copy(isBusy = false, error = exception.message) }
            } catch (exception: ApiException) {
                mutableState.update { it.copy(isBusy = false, error = exception.toDisplayMessage()) }
            } catch (exception: IOException) {
                mutableState.update {
                    it.copy(isBusy = false, error = "Keine Verbindung zum Server. Bitte versuche es erneut.")
                }
            }
        }
    }

    fun toggleMode() {
        mutableState.update {
            it.copy(
                isSignUp = !it.isSignUp,
                isVerificationStep = false,
                error = null,
                message = null,
            )
        }
    }

    fun logOut() {
        viewModelScope.launch {
            repository.logOut()
        }
    }
}

internal fun ApiException.toDisplayMessage(): String {
    val label = when (statusCode) {
        400 -> "Eingabe ungültig"
        401 -> "Anmeldung fehlgeschlagen"
        429 -> "Zu viele Versuche"
        else -> "Serverfehler ($statusCode)"
    }
    return if (message.isBlank()) "$label ($statusCode)." else "$label ($statusCode): $message"
}
