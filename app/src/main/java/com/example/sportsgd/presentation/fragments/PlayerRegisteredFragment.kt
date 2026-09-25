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
import com.example.sportsgd.databinding.FragmentSuccessBinding
import com.example.sportsgd.presentation.viewmodels.AppViewModelFactory
import com.example.sportsgd.presentation.viewmodels.PlayerDetailViewModel
import kotlinx.coroutines.launch

class PlayerRegisteredFragment : BaseAppFragment(R.layout.fragment_success) {
    private val playerId: Long by lazy { arguments?.getLong("playerId", -1L) ?: -1L }
    private val viewModel: PlayerDetailViewModel by viewModels {
        AppViewModelFactory(requireActivity().application as SportsGdApplication, playerId = playerId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAppChrome(view)
        val binding = FragmentSuccessBinding.bind(view)
        binding.apply {
            ivSuccess.setImageResource(R.drawable.sportsgd_success)
            btnSuccessAction.text = "Ver jugadores"
            btnSuccessAction.setOnClickListener {
                if (!findNavController().popBackStack(R.id.playerListFragment, false)) {
                    findNavController().navigateSafely(R.id.playerListFragment)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (state.isLoading) return@collect
                    binding.ivSuccess.isVisible = state.player != null
                    binding.tvSuccessTitle.text = if (state.player != null) {
                        "¡Jugador registrado!"
                    } else {
                        "Jugador no encontrado"
                    }
                    binding.tvSuccessMessage.text = state.player?.let { player ->
                        "${player.fullName} quedó guardado. Revisa la lista para consultar su ficha."
                    } ?: "No se encontró el registro solicitado."
                }
            }
        }
    }
}
