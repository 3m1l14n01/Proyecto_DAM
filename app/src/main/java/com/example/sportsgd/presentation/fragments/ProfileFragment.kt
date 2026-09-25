package com.example.sportsgd.presentation.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.sportsgd.R
import com.example.sportsgd.SportsGdApplication
import com.example.sportsgd.databinding.FragmentProfileBinding
import com.example.sportsgd.domain.model.UserRole
import com.example.sportsgd.presentation.viewmodels.AppViewModelFactory
import com.example.sportsgd.presentation.viewmodels.LoginViewModel

class ProfileFragment : BaseAppFragment(R.layout.fragment_profile) {
    private val viewModel: LoginViewModel by viewModels {
        AppViewModelFactory(requireActivity().application as SportsGdApplication)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAppChrome(view)
        val binding = FragmentProfileBinding.bind(view)
        val sessionManager = (requireActivity().application as SportsGdApplication).container.sessionManager
        val session = sessionManager.session.value
        val name = session?.displayName ?: "Coach SPORTSGD"
        binding.tvProfileInitials.text = name.split(Regex("\\s+")).take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("").ifBlank { "SG" }
        binding.tvProfileName.text = name
        binding.tvProfileEmail.text = session?.email ?: "coach@sportsgd.mx"
        binding.tvProfileRole.text = if (session?.role == UserRole.STUDENT) {
            getString(R.string.profile_role_student)
        } else {
            getString(R.string.profile_role_coach)
        }
        binding.tvProfileTeam.text = getString(R.string.profile_data_scope)
        binding.tvProfileSession.text = getString(
            if (sessionManager.isPersisted) {
                R.string.profile_session_persisted
            } else {
                R.string.profile_session_temporary
            },
        )
        binding.btnBackToMenu.setOnClickListener { findNavController().popBackStack() }
        binding.btnProfileLogout.setOnClickListener {
            viewModel.logout()
            findNavController().navigateClearingSession(R.id.loginFragment)
        }
    }
}
