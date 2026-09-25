package com.example.sportsgd

import android.content.Intent
import androidx.navigation.fragment.NavHostFragment
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StartupInstrumentedTest {
    @Test
    fun activityStartsAndNavigationGraphInflates() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val intent = Intent(instrumentation.targetContext, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val activity = instrumentation.startActivitySync(intent) as MainActivity
        try {
            val navHost = activity.supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val graph = navHost.navController.graph
            assertNotNull(graph.findNode(R.id.loginFragment))
            assertNotNull(graph.findNode(R.id.dashboardFragment))
            assertNotNull(graph.findNode(R.id.playerDetailFragment))
            assertNotNull(graph.findNode(R.id.routineActiveFragment))
        } finally {
            instrumentation.runOnMainSync { activity.finish() }
        }
    }
}
