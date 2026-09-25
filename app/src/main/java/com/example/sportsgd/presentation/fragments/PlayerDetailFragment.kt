package com.example.sportsgd.presentation.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.sportsgd.R
import com.example.sportsgd.SportsGdApplication
import com.example.sportsgd.databinding.FragmentPlayerDetailBinding
import com.example.sportsgd.domain.model.PlayerStatus
import com.example.sportsgd.presentation.displayName
import com.example.sportsgd.presentation.initials
import com.example.sportsgd.presentation.summary
import com.example.sportsgd.presentation.viewmodels.AppViewModelFactory
import com.example.sportsgd.presentation.viewmodels.PlayerDetailViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class PlayerDetailFragment : BaseAppFragment(R.layout.fragment_player_detail) {
    private var binding: FragmentPlayerDetailBinding? = null
    private val playerId: Long by lazy { arguments?.getLong("playerId", -1L) ?: -1L }
    private val viewModel: PlayerDetailViewModel by viewModels {
        AppViewModelFactory(
            application = requireActivity().application as SportsGdApplication,
            playerId = playerId,
        )
    }
    private var missingPlayerHandled = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentBinding = FragmentPlayerDetailBinding.bind(view)
        binding = currentBinding
        setupAppChrome(view)
        currentBinding.btnBackToPlayers.setOnClickListener { findNavController().popBackStack() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val safeBinding = binding ?: return@collect
                    state.player?.let { player ->
                        safeBinding.tvPlayerInitials.text = player.initials()
                        safeBinding.tvPlayerName.text = player.fullName
                        safeBinding.tvPlayerSummary.text = player.summary()
                        safeBinding.tvDetailEmail.text = "Correo: ${player.email}"
                        safeBinding.tvDetailAge.text = "Edad / nacimiento: ${player.birthDate}"
                        safeBinding.tvDetailSport.text = "Deporte: ${player.sport}"
                        safeBinding.tvDetailPosition.text = "Posición: ${player.position}"
                        safeBinding.tvDetailTeam.text = "Equipo: ${player.team}"
                        safeBinding.tvDetailSemester.text = "Semestre: ${player.semester}.º"
                        safeBinding.tvDetailAverage.text = "Promedio académico: ${"%.1f".format(player.average)}"
                        safeBinding.tvDetailStatus.text = "Estado: ${player.status.displayName()}"
                        val statusColor = when (player.status) {
                            PlayerStatus.ACTIVE -> R.color.sportsgd_success
                            PlayerStatus.INJURED -> R.color.sportsgd_error
                            PlayerStatus.RECOVERING -> R.color.sportsgd_warning
                        }
                        safeBinding.viewPlayerStatus.backgroundTintList = ColorStateList.valueOf(
                            ContextCompat.getColor(safeBinding.root.context, statusColor),
                        )
                        safeBinding.tvNextActivity.isVisible = state.nextActivity != null
                        safeBinding.tvNextActivity.text = state.nextActivity?.let { activity ->
                            getString(R.string.player_next_activity, activity.title)
                        }
                    }
                    if (state.notFound && !missingPlayerHandled) {
                        missingPlayerHandled = true
                        Snackbar.make(view, "No se encontró el jugador solicitado.", Snackbar.LENGTH_LONG).show()
                        findNavController().popBackStack()
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
