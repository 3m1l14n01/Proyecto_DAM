package com.example.sportsgd.presentation.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.core.view.isVisible
import com.example.sportsgd.R
import com.example.sportsgd.SportsGdApplication
import com.example.sportsgd.databinding.FragmentPlayerListBinding
import com.example.sportsgd.domain.model.UserRole
import com.example.sportsgd.presentation.adapters.PlayerAdapter
import com.example.sportsgd.presentation.viewmodels.AppViewModelFactory
import com.example.sportsgd.presentation.viewmodels.PlayersViewModel
import kotlinx.coroutines.launch

class PlayerListFragment : BaseAppFragment(R.layout.fragment_player_list) {
    private var binding: FragmentPlayerListBinding? = null
    private val viewModel: PlayersViewModel by viewModels {
        AppViewModelFactory(requireActivity().application as SportsGdApplication)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentBinding = FragmentPlayerListBinding.bind(view)
        binding = currentBinding
        setupAppChrome(view, MainSection.PLAYERS)
        val adapter = PlayerAdapter { player ->
            findNavController().navigateSafely(
                R.id.playerDetailFragment,
                Bundle().apply { putLong("playerId", player.id) },
            )
        }
        currentBinding.rvPlayers.layoutManager = LinearLayoutManager(requireContext())
        currentBinding.rvPlayers.adapter = adapter
        currentBinding.btnAddPlayer.isVisible =
            (requireActivity().application as SportsGdApplication).container.sessionManager
                .session.value?.role == UserRole.COACH
        currentBinding.btnAddPlayer.setOnClickListener {
            findNavController().navigateSafely(R.id.playerFormFragment)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.players.collect { players ->
                    adapter.submitList(players)
                    binding?.tvPlayerCount?.text = "${players.size} atletas registrados"
                }
            }
        }
    }

    override fun onDestroyView() {
        binding?.rvPlayers?.adapter = null
        binding = null
        super.onDestroyView()
    }
}
