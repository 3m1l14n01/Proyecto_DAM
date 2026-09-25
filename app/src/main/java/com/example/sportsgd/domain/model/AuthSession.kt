package com.example.sportsgd.domain.model

enum class UserRole {
    COACH,
    STUDENT,
}

data class AuthSession(
    val email: String,
    val displayName: String,
    val role: UserRole,
)

sealed interface LoginResult {
    data class Success(val session: AuthSession) : LoginResult

    data object MissingFields : LoginResult

    data object InvalidCredentials : LoginResult
}
