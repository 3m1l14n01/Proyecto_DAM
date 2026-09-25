package com.example.sportsgd.presentation.fragments

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.sportsgd.R
import com.example.sportsgd.SportsGdApplication
import com.example.sportsgd.databinding.FragmentPlayerFormBinding
import com.example.sportsgd.domain.model.PlayerStatus
import com.example.sportsgd.domain.model.UserRole
import com.example.sportsgd.presentation.viewmodels.AppViewModelFactory
import com.example.sportsgd.presentation.viewmodels.PlayerFormInput
import com.example.sportsgd.presentation.viewmodels.PlayerSaveResult
import com.example.sportsgd.presentation.viewmodels.PlayersViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class PlayerFormFragment : BaseAppFragment(R.layout.fragment_player_form) {
    private var binding: FragmentPlayerFormBinding? = null
    private val viewModel: PlayersViewModel by viewModels {
        AppViewModelFactory(requireActivity().application as SportsGdApplication)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentBinding = FragmentPlayerFormBinding.bind(view)
        binding = currentBinding
        setupAppChrome(view, MainSection.PLAYERS)
        if ((requireActivity().application as SportsGdApplication).container.sessionManager
                .session.value?.role != UserRole.COACH
        ) {
            findNavController().navigateSafely(R.id.playerListFragment)
            return
        }
        val statuses = listOf("Activo", "Lesionado", "En recuperación")
        currentBinding.actvPlayerStatus.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, statuses),
        )
        currentBinding.actvPlayerStatus.setText(statuses.first(), false)
        currentBinding.btnSavePlayer.setOnClickListener { submit(currentBinding) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.saveResult.collect { result ->
                    val safeBinding = binding ?: return@collect
                    safeBinding.btnSavePlayer.isEnabled = result !is PlayerSaveResult.Saving
                    when (result) {
                        is PlayerSaveResult.Invalid -> showErrors(safeBinding, result)
                        is PlayerSaveResult.Success -> {
                            viewModel.clearSaveResult()
                            findNavController().navigateSafely(
                                R.id.playerRegisteredFragment,
                                Bundle().apply { putLong("playerId", result.playerId) },
                            )
                        }
                        is PlayerSaveResult.Failure -> {
                            Snackbar.make(view, result.message, Snackbar.LENGTH_LONG).show()
                            viewModel.clearSaveResult()
                        }
                        PlayerSaveResult.Saving, null -> Unit
                    }
                }
            }
        }
    }

    private fun submit(binding: FragmentPlayerFormBinding) {
        clearErrors(binding)
        val nameParts = binding.etPlayerName.text?.toString().orEmpty().trim()
            .split(Regex("\\s+")).filter(String::isNotBlank)
        val status = when (binding.actvPlayerStatus.text?.toString()) {
            "Lesionado" -> PlayerStatus.INJURED
            "En recuperación" -> PlayerStatus.RECOVERING
            else -> PlayerStatus.ACTIVE
        }
        viewModel.addPlayer(
            PlayerFormInput(
                firstName = nameParts.firstOrNull().orEmpty(),
                lastName = nameParts.drop(1).joinToString(" "),
                email = binding.etPlayerEmail.text?.toString().orEmpty(),
                ageOrBirthDate = binding.etPlayerAge.text?.toString().orEmpty(),
                sport = binding.etPlayerSport.text?.toString().orEmpty(),
                position = binding.etPlayerPosition.text?.toString().orEmpty(),
                team = binding.etPlayerTeam.text?.toString().orEmpty(),
                semester = binding.etPlayerSemester.text?.toString().orEmpty(),
                average = binding.etPlayerAverage.text?.toString().orEmpty(),
                status = status,
            ),
        )
    }

    private fun showErrors(binding: FragmentPlayerFormBinding, result: PlayerSaveResult.Invalid) {
        val errors = result.errors
        binding.tilPlayerName.error = errors.firstName ?: errors.lastName
        binding.tilPlayerEmail.error = errors.email
        binding.tilPlayerAge.error = errors.ageOrBirthDate
        binding.tilPlayerSport.error = errors.sport
        binding.tilPlayerPosition.error = errors.position
        binding.tilPlayerTeam.error = errors.team
        binding.tilPlayerSemester.error = errors.semester
        binding.tilPlayerAverage.error = errors.average
    }

    private fun clearErrors(binding: FragmentPlayerFormBinding) {
        listOf(
            binding.tilPlayerName,
            binding.tilPlayerEmail,
            binding.tilPlayerAge,
            binding.tilPlayerSport,
            binding.tilPlayerPosition,
            binding.tilPlayerTeam,
            binding.tilPlayerSemester,
            binding.tilPlayerAverage,
        ).forEach { it.error = null }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
