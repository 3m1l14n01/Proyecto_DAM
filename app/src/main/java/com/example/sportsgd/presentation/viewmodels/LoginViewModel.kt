package com.example.sportsgd.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsgd.domain.model.AuthSession
import com.example.sportsgd.domain.model.LoginResult
import com.example.sportsgd.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val session: AuthSession? = null,
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val loginResult: LoginResult? = null,
    val recoveryResult: RecoveryResult? = null,
) {
    val isLoggedIn: Boolean
        get() = session != null
}

sealed interface RecoveryResult {
    data object Sent : RecoveryResult
    data object UnknownEmail : RecoveryResult
    data object InvalidEmail : RecoveryResult
    data class Failure(val message: String) : RecoveryResult
}

private data class LoginActionState(
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val loginResult: LoginResult? = null,
    val recoveryResult: RecoveryResult? = null,
)

class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val actionState = MutableStateFlow(LoginActionState())

    val uiState: StateFlow<LoginUiState> = combine(
        authRepository.session,
        actionState,
    ) { session, action ->
        LoginUiState(
            session = session,
            isLoading = action.isLoading,
            emailError = action.emailError,
            passwordError = action.passwordError,
            loginResult = action.loginResult,
            recoveryResult = action.recoveryResult,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LoginUiState(session = authRepository.session.value),
    )

    fun login(email: String, password: String) {
        val normalizedEmail = email.trim()
        val emailError = validateEmail(normalizedEmail)
        val passwordError = validatePassword(password)
        if (emailError != null || passwordError != null) {
            actionState.value = LoginActionState(
                emailError = emailError,
                passwordError = passwordError,
                loginResult = if (normalizedEmail.isBlank() || password.isBlank()) {
                    LoginResult.MissingFields
                } else {
                    null
                },
            )
            return
        }

        viewModelScope.launch {
            actionState.update {
                it.copy(
                    isLoading = true,
                    emailError = null,
                    passwordError = null,
                    loginResult = null,
                    recoveryResult = null,
                )
            }
            val result = runCatching {
                authRepository.login(normalizedEmail, password)
            }.getOrElse {
                LoginResult.InvalidCredentials
            }
            actionState.update { it.copy(isLoading = false, loginResult = result) }
        }
    }

    fun requestPasswordRecovery(email: String) {
        val normalizedEmail = email.trim()
        val emailError = validateEmail(normalizedEmail)
        if (emailError != null) {
            actionState.value = LoginActionState(
                emailError = emailError,
                recoveryResult = RecoveryResult.InvalidEmail,
            )
            return
        }

        viewModelScope.launch {
            actionState.update {
                it.copy(
                    isLoading = true,
                    emailError = null,
                    passwordError = null,
                    loginResult = null,
                    recoveryResult = null,
                )
            }
            val result = runCatching {
                if (authRepository.requestPasswordRecovery(normalizedEmail)) {
                    RecoveryResult.Sent
                } else {
                    RecoveryResult.UnknownEmail
                }
            }.getOrElse {
                RecoveryResult.Failure("No fue posible procesar la recuperación.")
            }
            actionState.update { it.copy(isLoading = false, recoveryResult = result) }
        }
    }

    fun clearFeedback() {
        actionState.update {
            it.copy(
                emailError = null,
                passwordError = null,
                loginResult = null,
                recoveryResult = null,
            )
        }
    }

    fun logout() {
        authRepository.logout()
        clearFeedback()
    }

    private fun validateEmail(email: String): String? = when {
        email.isBlank() -> "Ingresa tu correo."
        !EMAIL_REGEX.matches(email) -> "Ingresa un correo válido."
        else -> null
    }

    private fun validatePassword(password: String): String? = when {
        password.isBlank() -> "Ingresa tu contraseña."
        password.length < MIN_PASSWORD_LENGTH -> "La contraseña debe tener al menos 6 caracteres."
        else -> null
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 6
        val EMAIL_REGEX = Regex(
            pattern = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            option = RegexOption.IGNORE_CASE,
        )
    }
}
