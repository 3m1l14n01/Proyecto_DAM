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
import com.example.sportsgd.databinding.FragmentLoginBinding
import com.example.sportsgd.domain.model.LoginResult
import com.example.sportsgd.presentation.viewmodels.AppViewModelFactory
import com.example.sportsgd.presentation.viewmodels.LoginViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class LoginFragment : BaseAppFragment(R.layout.fragment_login) {
    override val darkStatusBarIcons: Boolean = true

    private var binding: FragmentLoginBinding? = null
    private val viewModel: LoginViewModel by viewModels {
        AppViewModelFactory(requireActivity().application as SportsGdApplication)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentBinding = FragmentLoginBinding.bind(view)
        binding = currentBinding

        currentBinding.btnLogin.setOnClickListener {
            viewModel.login(
                email = currentBinding.etEmail.text?.toString().orEmpty(),
                password = currentBinding.etPassword.text?.toString().orEmpty(),
            )
        }
        currentBinding.tvRecoverAccess.setOnClickListener {
            findNavController().navigateSafely(R.id.recoverAccessFragment)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val safeBinding = binding ?: return@collect
                    safeBinding.btnLogin.isEnabled = !state.isLoading
                    safeBinding.etEmail.error = state.emailError
                    safeBinding.etPassword.error = state.passwordError
                    when (state.loginResult) {
                        is LoginResult.Success -> {
                            if (!safeBinding.cbRememberSession.isChecked) {
                                (requireActivity().application as SportsGdApplication)
                                    .container.sessionManager.forgetPersistence()
                            }
                            viewModel.clearFeedback()
                            findNavController().navigateClearingSession(R.id.dashboardFragment)
                        }
                        LoginResult.InvalidCredentials -> {
                            Snackbar.make(view, "Credenciales incorrectas", Snackbar.LENGTH_LONG).show()
                            viewModel.clearFeedback()
                        }
                        LoginResult.MissingFields, null -> Unit
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
