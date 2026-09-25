package com.example.sportsgd

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import com.example.sportsgd.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHost.navController
        val destinationId = navController.currentDestination?.id ?: return
        val isAuthenticated = (application as SportsGdApplication).container.sessionManager.isLoggedIn
        val onAccessScreen = destinationId == R.id.loginFragment ||
            destinationId == R.id.recoverAccessFragment ||
            destinationId == R.id.checkEmailFragment
        val destination = when {
            isAuthenticated && onAccessScreen -> R.id.dashboardFragment
            !isAuthenticated && !onAccessScreen -> R.id.loginFragment
            else -> return
        }
        navController.navigate(
            destination,
            null,
            navOptions {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            },
        )
    }
}
