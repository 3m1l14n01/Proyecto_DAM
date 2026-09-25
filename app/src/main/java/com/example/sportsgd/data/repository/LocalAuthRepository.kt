package com.example.sportsgd.data.repository

import com.example.sportsgd.data.session.SessionManager
import com.example.sportsgd.domain.model.AuthSession
import com.example.sportsgd.domain.model.LoginResult
import com.example.sportsgd.domain.model.UserRole
import com.example.sportsgd.domain.repository.AuthRepository
import kotlinx.coroutines.flow.StateFlow

object DemoCredentials {
    const val EMAIL = "coach@sportsgd.mx"
    const val STUDENT_EMAIL = "student@sportsgd.mx"
    const val PASSWORD = "Sport2026!"
}

class LocalAuthRepository(
    private val sessionManager: SessionManager,
) : AuthRepository {
    override val session: StateFlow<AuthSession?> = sessionManager.session

    override suspend fun login(email: String, password: String): LoginResult {
        val normalizedEmail = email.trim().lowercase()
        if (normalizedEmail.isBlank() || password.isBlank()) {
            return LoginResult.MissingFields
        }
        if (password != DemoCredentials.PASSWORD ||
            normalizedEmail !in setOf(DemoCredentials.EMAIL, DemoCredentials.STUDENT_EMAIL)
        ) {
            return LoginResult.InvalidCredentials
        }

        val session = if (normalizedEmail == DemoCredentials.EMAIL) {
            AuthSession(DemoCredentials.EMAIL, "Coach SPORTSGD", UserRole.COACH)
        } else {
            AuthSession(DemoCredentials.STUDENT_EMAIL, "Estudiante SPORTSGD", UserRole.STUDENT)
        }
        sessionManager.saveSession(session)
        return LoginResult.Success(session)
    }

    override suspend fun requestPasswordRecovery(email: String): Boolean =
        email.trim().lowercase() in setOf(DemoCredentials.EMAIL, DemoCredentials.STUDENT_EMAIL)

    override fun logout() {
        sessionManager.clearSession()
    }
}
