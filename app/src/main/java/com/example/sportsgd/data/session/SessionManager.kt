package com.example.sportsgd.data.session

import android.content.Context
import com.example.sportsgd.domain.model.AuthSession
import com.example.sportsgd.domain.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE,
    )

    private val mutableSession = MutableStateFlow(readSession())
    val session: StateFlow<AuthSession?> = mutableSession.asStateFlow()
    val isLoggedIn: Boolean
        get() = session.value != null
    val isPersisted: Boolean
        get() = preferences.contains(KEY_EMAIL) && preferences.contains(KEY_ROLE)

    fun saveSession(session: AuthSession) {
        preferences.edit()
            .putString(KEY_EMAIL, session.email)
            .putString(KEY_DISPLAY_NAME, session.displayName)
            .putString(KEY_ROLE, session.role.name)
            .apply()
        mutableSession.value = session
    }

    fun clearSession() {
        preferences.edit().clear().apply()
        mutableSession.value = null
    }

    /** Keeps the current in-memory session but prevents it from surviving a process restart. */
    fun forgetPersistence() {
        preferences.edit().clear().apply()
    }

    private fun readSession(): AuthSession? {
        val email = preferences.getString(KEY_EMAIL, null)?.takeIf { it.isNotBlank() } ?: return null
        val displayName = preferences.getString(KEY_DISPLAY_NAME, null)?.takeIf { it.isNotBlank() }
            ?: email.substringBefore('@')
        val storedRole = preferences.getString(KEY_ROLE, null)
        val role = storedRole?.let { stored ->
            enumValues<UserRole>().firstOrNull { it.name == stored }
        }
        if (role == null) {
            // A partial or tampered session must never gain coach privileges by fallback.
            preferences.edit().clear().apply()
            return null
        }
        return AuthSession(email = email, displayName = displayName, role = role)
    }

    private companion object {
        const val PREFERENCES_NAME = "sports_gd_session"
        const val KEY_EMAIL = "email"
        const val KEY_DISPLAY_NAME = "display_name"
        const val KEY_ROLE = "role"
    }
}
