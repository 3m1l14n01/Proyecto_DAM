package com.example.sportsgd.presentation.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.sportsgd.R
import com.example.sportsgd.SportsGdApplication
import com.example.sportsgd.databinding.FragmentRecoverAccessBinding
import com.example.sportsgd.presentation.viewmodels.AppViewModelFactory
import com.example.sportsgd.presentation.viewmodels.LoginViewModel
import com.example.sportsgd.presentation.viewmodels.RecoveryResult
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class RecoverAccessFragment : BaseAppFragment(R.layout.fragment_recover_access) {
    override val darkStatusBarIcons: Boolean = true

    private var binding: FragmentRecoverAccessBinding? = null
    private val viewModel: LoginViewModel by viewModels {
        AppViewModelFactory(requireActivity().application as SportsGdApplication)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentBinding = FragmentRecoverAccessBinding.bind(view)
        binding = currentBinding
        currentBinding.btnContinueRecovery.setOnClickListener {
            viewModel.requestPasswordRecovery(currentBinding.etRecoveryEmail.text?.toString().orEmpty())
        }
        currentBinding.btnBackToLogin.setOnClickListener { findNavController().popBackStack() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val safeBinding = binding ?: return@collect
                    safeBinding.btnContinueRecovery.isEnabled = !state.isLoading
                    safeBinding.etRecoveryEmail.error = state.emailError
                    when (state.recoveryResult) {
                        RecoveryResult.Sent -> {
                            viewModel.clearFeedback()
                            findNavController().navigateSafely(R.id.checkEmailFragment)
                        }
                        RecoveryResult.UnknownEmail -> {
                            Snackbar.make(view, "El correo no pertenece al usuario de demostración.", Snackbar.LENGTH_LONG).show()
                            viewModel.clearFeedback()
                        }
                        is RecoveryResult.Failure -> {
                            Snackbar.make(view, state.recoveryResult.message, Snackbar.LENGTH_LONG).show()
                            viewModel.clearFeedback()
                        }
                        RecoveryResult.InvalidEmail, null -> Unit
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
