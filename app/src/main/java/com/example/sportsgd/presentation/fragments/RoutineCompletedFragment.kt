package com.example.sportsgd.presentation.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.sportsgd.R
import com.example.sportsgd.SportsGdApplication
import com.example.sportsgd.databinding.FragmentSuccessBinding
import com.example.sportsgd.presentation.viewmodels.AppViewModelFactory
import com.example.sportsgd.presentation.viewmodels.GoalsRoutinesViewModel
import kotlinx.coroutines.launch

class RoutineCompletedFragment : BaseAppFragment(R.layout.fragment_success) {
    private val routineId: Long by lazy { arguments?.getLong("routineId", -1L) ?: -1L }
    private val viewModel: GoalsRoutinesViewModel by activityViewModels {
        AppViewModelFactory(requireActivity().application as SportsGdApplication)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAppChrome(view)
        val binding = FragmentSuccessBinding.bind(view)
        binding.apply {
            ivSuccess.setImageResource(R.drawable.sportsgd_success)
            btnSuccessAction.text = "Volver a metas"
            btnSuccessAction.setOnClickListener {
                findNavController().navigate(
                    R.id.goalsFragment,
                    null,
                    navOptions {
                        popUpTo(R.id.goalsFragment) { inclusive = true }
                        launchSingleTop = true
                    },
                )
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (state.isLoading) return@collect
                    val routine = state.routines.firstOrNull { it.id == routineId }
                    binding.ivSuccess.isVisible = routine?.isCompleted == true
                    binding.tvSuccessTitle.text = if (routine?.isCompleted == true) {
                        "¡Rutina completada!"
                    } else {
                        "Rutina no disponible"
                    }
                    binding.tvSuccessMessage.text = if (routine?.isCompleted == true) {
                        "${routine.title} quedó registrada. ¡Buen trabajo!"
                    } else {
                        "No hay una rutina completada con este identificador."
                    }
                }
            }
        }
    }
}
