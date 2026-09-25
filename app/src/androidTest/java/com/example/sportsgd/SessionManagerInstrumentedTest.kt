package com.example.sportsgd

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.sportsgd.data.session.SessionManager
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SessionManagerInstrumentedTest {
    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val preferences
        get() = context.getSharedPreferences("sports_gd_session", Context.MODE_PRIVATE)

    @Before
    fun clearSession() {
        preferences.edit().clear().commit()
    }

    @After
    fun cleanUp() {
        preferences.edit().clear().commit()
    }

    @Test
    fun unknownRoleFailsClosedAndRemovesPersistedSession() {
        preferences.edit()
            .putString("email", "student@sportsgd.mx")
            .putString("display_name", "Estudiante SPORTSGD")
            .putString("role", "ADMIN")
            .commit()

        val manager = SessionManager(context)

        assertNull(manager.session.value)
        assertFalse(manager.isLoggedIn)
        assertFalse(manager.isPersisted)
    }
}
