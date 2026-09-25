package com.example.sportsgd.presentation.fragments

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.sportsgd.R

enum class MainSection(val destinationId: Int, val viewId: Int) {
    HOME(R.id.dashboardFragment, R.id.navHome),
    PLAYERS(R.id.playerListFragment, R.id.navPlayers),
    CALENDAR(R.id.calendarFragment, R.id.navCalendar),
    GOALS(R.id.goalsFragment, R.id.navGoals),
}

abstract class BaseAppFragment(@LayoutRes layoutId: Int) : Fragment(layoutId) {
    protected open val darkStatusBarIcons: Boolean = false

    override fun onResume() {
        super.onResume()
        val window = requireActivity().window
        val statusBarColor = if (darkStatusBarIcons) {
            R.color.sportsgd_canvas
        } else {
            R.color.sportsgd_primary
        }
        window.statusBarColor = ContextCompat.getColor(requireContext(), statusBarColor)
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = darkStatusBarIcons
    }

    protected fun setupAppChrome(view: View, activeSection: MainSection? = null) {
        view.findViewById<View?>(R.id.btnMenu)?.setOnClickListener {
            findNavController().navigateSafely(R.id.menuFragment)
        }
        view.findViewById<View?>(R.id.btnNotifications)?.setOnClickListener {
            findNavController().navigateSafely(R.id.notificationsFragment)
        }

        MainSection.entries.forEach { section ->
            view.findViewById<View?>(section.viewId)?.apply {
                isSelected = section == activeSection
                alpha = if (isSelected) 1f else 0.72f
                setOnClickListener { findNavController().navigateTopLevel(section.destinationId) }
            }
        }
    }

    protected fun NavController.navigateClearingSession(destinationId: Int) {
        navigate(
            destinationId,
            null,
            navOptions {
                popUpTo(graph.id) { inclusive = true }
                launchSingleTop = true
            },
        )
    }

    protected fun NavController.navigateSafely(destinationId: Int, args: Bundle? = null) {
        if (currentDestination?.id == destinationId) return
        navigate(destinationId, args)
    }

    private fun NavController.navigateTopLevel(destinationId: Int) {
        if (currentDestination?.id == destinationId) return
        navigate(
            destinationId,
            null,
            navOptions {
                popUpTo(R.id.dashboardFragment) { inclusive = false }
                launchSingleTop = true
                restoreState = true
            },
        )
    }
}
