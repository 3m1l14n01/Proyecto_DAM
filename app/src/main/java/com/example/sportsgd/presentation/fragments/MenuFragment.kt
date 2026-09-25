package com.example.sportsgd.presentation.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.sportsgd.R
import com.example.sportsgd.SportsGdApplication
import com.example.sportsgd.databinding.FragmentMenuBinding
import com.example.sportsgd.presentation.viewmodels.AppViewModelFactory
import com.example.sportsgd.presentation.viewmodels.LoginViewModel

class MenuFragment : BaseAppFragment(R.layout.fragment_menu) {
    private val viewModel: LoginViewModel by viewModels {
        AppViewModelFactory(requireActivity().application as SportsGdApplication)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentMenuBinding.bind(view)
        val session = (requireActivity().application as SportsGdApplication).container.sessionManager.session.value
        binding.tvMenuUserName.text = session?.displayName ?: "Coach SPORTSGD"
        binding.tvMenuSession.text = session?.email ?: "Sesión de demostración"
        binding.btnCloseMenu.setOnClickListener { findNavController().popBackStack() }
        binding.btnMenuHome.setOnClickListener { openModule(R.id.dashboardFragment) }
        binding.btnMenuPlayers.setOnClickListener { openModule(R.id.playerListFragment) }
        binding.btnMenuCalendar.setOnClickListener { openModule(R.id.calendarFragment) }
        binding.btnMenuGoals.setOnClickListener { openModule(R.id.goalsFragment) }
        binding.btnMenuNotifications.setOnClickListener { openModule(R.id.notificationsFragment) }
        binding.btnMenuProfile.setOnClickListener { findNavController().navigateSafely(R.id.profileFragment) }
        binding.btnMenuLogout.setOnClickListener {
            viewModel.logout()
            findNavController().navigateClearingSession(R.id.loginFragment)
        }
    }

    private fun openModule(destination: Int) {
        findNavController().navigate(
            destination,
            null,
            navOptions {
                popUpTo(R.id.dashboardFragment) { inclusive = destination == R.id.dashboardFragment }
                launchSingleTop = true
            },
        )
    }
}
