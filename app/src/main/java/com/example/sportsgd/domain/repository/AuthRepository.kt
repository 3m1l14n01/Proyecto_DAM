package com.example.sportsgd.domain.repository

import com.example.sportsgd.domain.model.AuthSession
import com.example.sportsgd.domain.model.LoginResult
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val session: StateFlow<AuthSession?>

    suspend fun login(email: String, password: String): LoginResult

    suspend fun requestPasswordRecovery(email: String): Boolean

    fun logout()
}
