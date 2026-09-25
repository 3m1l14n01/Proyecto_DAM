package com.example.sportsgd.presentation.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.sportsgd.R
import com.example.sportsgd.SportsGdApplication
import com.example.sportsgd.databinding.FragmentDashboardBinding
import com.example.sportsgd.presentation.scheduleText
import com.example.sportsgd.presentation.viewmodels.AppViewModelFactory
import com.example.sportsgd.presentation.viewmodels.DashboardViewModel
import kotlinx.coroutines.launch

class DashboardFragment : BaseAppFragment(R.layout.fragment_dashboard) {
    private var binding: FragmentDashboardBinding? = null
    private val viewModel: DashboardViewModel by viewModels {
        AppViewModelFactory(requireActivity().application as SportsGdApplication)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentBinding = FragmentDashboardBinding.bind(view)
        binding = currentBinding
        setupAppChrome(view, MainSection.HOME)
        val container = (requireActivity().application as SportsGdApplication).container
        val displayName = container.sessionManager.session.value?.displayName?.substringBefore(' ') ?: "Coach"
        currentBinding.tvGreeting.text = "¡Hola, $displayName!"
        currentBinding.cardNextActivity.setOnClickListener {
            viewModel.uiState.value.nextActivity?.let { activity ->
                findNavController().navigateSafely(
                    R.id.activityDetailFragment,
                    Bundle().apply { putLong("activityId", activity.id) },
                )
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val safeBinding = binding ?: return@collect
                    safeBinding.tvPendingCount.text = state.upcomingActivityCount.toString().padStart(2, '0')
                    safeBinding.tvActiveGoals.text = state.goals.count { !it.isCompleted }.toString().padStart(2, '0')
                    safeBinding.tvTeamSummary.text =
                        "${state.registeredPlayers} jugadores · ${state.activePlayers} activos · ${state.injuredPlayers} lesionados · ${state.todayTrainingSessions} sesiones hoy"
                    val next = state.nextActivity
                    safeBinding.cardNextActivity.isVisible = next != null
                    safeBinding.tvNextActivity.text = next?.title.orEmpty()
                    safeBinding.tvNextActivityMeta.text = next?.scheduleText().orEmpty()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshTime()
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
