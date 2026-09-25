package com.example.sportsgd.presentation.fragments

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.sportsgd.R
import com.example.sportsgd.SportsGdApplication
import com.example.sportsgd.databinding.DialogCreateGoalBinding
import com.example.sportsgd.databinding.DialogCreateRoutineBinding
import com.example.sportsgd.databinding.FragmentGoalsBinding
import com.example.sportsgd.databinding.ItemGoalSummaryBinding
import com.example.sportsgd.databinding.ItemRoutineSummaryBinding
import com.example.sportsgd.domain.model.Routine
import com.example.sportsgd.domain.model.UserRole
import com.example.sportsgd.presentation.viewmodels.AppViewModelFactory
import com.example.sportsgd.presentation.viewmodels.GoalFormErrors
import com.example.sportsgd.presentation.viewmodels.GoalFormInput
import com.example.sportsgd.presentation.viewmodels.GoalsRoutinesUiState
import com.example.sportsgd.presentation.viewmodels.GoalsRoutinesViewModel
import com.example.sportsgd.presentation.viewmodels.RoutineActionResult
import com.example.sportsgd.presentation.viewmodels.RoutineFormErrors
import com.example.sportsgd.presentation.viewmodels.RoutineFormInput
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class GoalsFragment : BaseAppFragment(R.layout.fragment_goals) {
    private var binding: FragmentGoalsBinding? = null
    private val viewModel: GoalsRoutinesViewModel by activityViewModels {
        AppViewModelFactory(requireActivity().application as SportsGdApplication)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentBinding = FragmentGoalsBinding.bind(view)
        binding = currentBinding
        setupAppChrome(view, MainSection.GOALS)

        currentBinding.btnStartRoutine.setOnClickListener {
            viewModel.uiState.value.routines.firstOrNull()?.let(::openRoutine)
                ?: Snackbar.make(view, "No hay una rutina disponible.", Snackbar.LENGTH_SHORT).show()
        }
        val session = (requireActivity().application as SportsGdApplication)
            .container.authRepository.session.value
        val canManage = session?.role == UserRole.COACH
        currentBinding.btnCreateGoal.isVisible = canManage
        currentBinding.btnCreateRoutine.isVisible = canManage
        currentBinding.btnCreateGoal.setOnClickListener { showGoalDialog() }
        currentBinding.btnCreateRoutine.setOnClickListener { showRoutineDialog() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val safeBinding = binding ?: return@collect
                    val featuredGoal = state.goals.firstOrNull()
                    safeBinding.tvGoalTitle.text = featuredGoal?.title ?: "No hay metas activas"
                    safeBinding.progressGoals.progress = featuredGoal?.progressPercent ?: 0
                    safeBinding.tvGoalProgress.text = featuredGoal?.let {
                        "${it.currentValue} de ${it.targetValue} ${it.unit} · ${it.progressPercent}%"
                    } ?: "Crea una meta para comenzar"
                    val featuredRoutine = state.routines.firstOrNull()
                    safeBinding.tvRoutineTitle.text = featuredRoutine?.title ?: "Sin rutina programada"
                    safeBinding.tvRoutineMeta.text = featuredRoutine?.let {
                        "${it.steps.size} ejercicios · ${it.estimatedDurationMinutes} minutos" +
                            if (it.isCompleted) " · Completada" else ""
                    }.orEmpty()
                    safeBinding.btnStartRoutine.text = when {
                        featuredRoutine?.isCompleted == true -> getString(R.string.view_summary)
                        featuredRoutine?.startedAt != null -> getString(R.string.continue_routine)
                        else -> getString(R.string.start_routine)
                    }
                    safeBinding.btnStartRoutine.isEnabled = featuredRoutine != null && !state.isMutating
                    safeBinding.btnCreateGoal.isEnabled = !state.isLoading && !state.isMutating
                    safeBinding.btnCreateRoutine.isEnabled = !state.isLoading && !state.isMutating
                    renderLists(safeBinding, state)

                    when (val action = state.actionResult) {
                        is RoutineActionResult.Started -> {
                            viewModel.clearActionResult()
                            findNavController().navigateSafely(
                                R.id.routineActiveFragment,
                                Bundle().apply { putLong("routineId", action.routineId) },
                            )
                        }
                        is RoutineActionResult.GoalCreated -> {
                            viewModel.clearActionResult()
                            Snackbar.make(view, R.string.goal_saved, Snackbar.LENGTH_SHORT).show()
                        }
                        is RoutineActionResult.RoutineCreated -> {
                            viewModel.clearActionResult()
                            Snackbar.make(view, R.string.routine_saved, Snackbar.LENGTH_SHORT).show()
                        }
                        is RoutineActionResult.Failure -> {
                            viewModel.clearActionResult()
                            Snackbar.make(view, action.message, Snackbar.LENGTH_LONG).show()
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun renderLists(binding: FragmentGoalsBinding, state: GoalsRoutinesUiState) {
        val names = state.players.associate { it.id to it.fullName }
        binding.tvNoGoals.isVisible = state.goals.isEmpty() && !state.isLoading
        binding.tvNoRoutines.isVisible = state.routines.isEmpty() && !state.isLoading
        binding.goalsList.removeAllViews()
        state.goals.forEach { goal ->
            val item = ItemGoalSummaryBinding.inflate(layoutInflater, binding.goalsList, false)
            item.tvGoalItemTitle.text = goal.title
            item.tvGoalItemPlayer.text = names[goal.playerId] ?: getString(R.string.not_available)
            item.progressGoalItem.progress = goal.progressPercent
            item.tvGoalItemProgress.text =
                "${goal.currentValue} de ${goal.targetValue} ${goal.unit} · ${goal.progressPercent}%"
            binding.goalsList.addView(item.root)
        }

        binding.routinesList.removeAllViews()
        state.routines.forEach { routine ->
            val item = ItemRoutineSummaryBinding.inflate(layoutInflater, binding.routinesList, false)
            item.tvRoutineItemTitle.text = routine.title
            item.tvRoutineItemMeta.text =
                "${names[routine.playerId] ?: getString(R.string.not_available)} · " +
                    "${routine.steps.size} pasos · ${routine.estimatedDurationMinutes} min"
            item.tvRoutineItemProgress.text =
                if (routine.isCompleted) "Completada · 100%" else "Avance: ${routine.progressPercent}%"
            item.btnRoutineItemAction.text = when {
                routine.isCompleted -> getString(R.string.view_summary)
                routine.startedAt != null -> getString(R.string.continue_routine)
                else -> getString(R.string.start_routine)
            }
            item.btnRoutineItemAction.isEnabled = !state.isMutating
            item.btnRoutineItemAction.setOnClickListener { openRoutine(routine) }
            binding.routinesList.addView(item.root)
        }
    }

    private fun openRoutine(routine: Routine) {
        if (routine.isCompleted) {
            findNavController().navigateSafely(
                R.id.routineCompletedFragment,
                Bundle().apply { putLong("routineId", routine.id) },
            )
        } else if (routine.startedAt != null) {
            findNavController().navigateSafely(
                R.id.routineActiveFragment,
                Bundle().apply { putLong("routineId", routine.id) },
            )
        } else {
            viewModel.startRoutine(routine.id)
        }
    }

    private fun showGoalDialog() {
        val players = viewModel.uiState.value.players
        if (players.isEmpty()) {
            Snackbar.make(requireView(), R.string.players_required_for_goals, Snackbar.LENGTH_LONG).show()
            return
        }
        val form = DialogCreateGoalBinding.inflate(layoutInflater)
        val labels = players.map { "${it.fullName} · ${it.email}" }
        form.actvGoalPlayer.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, labels),
        )
        var selectedPlayerId = players.first().id
        form.actvGoalPlayer.setText(labels.first(), false)
        form.actvGoalPlayer.setOnItemClickListener { _, _, position, _ ->
            selectedPlayerId = players[position].id
            form.tilGoalPlayer.error = null
        }
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.create_goal)
            .setView(form.root)
            .setNegativeButton(R.string.cancel_action, null)
            .setPositiveButton(R.string.save_action, null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val errors = viewModel.createGoal(
                    GoalFormInput(
                        playerId = selectedPlayerId,
                        title = form.etGoalTitle.text?.toString().orEmpty(),
                        description = form.etGoalDescription.text?.toString().orEmpty(),
                        currentValue = form.etGoalCurrent.text?.toString().orEmpty(),
                        targetValue = form.etGoalTarget.text?.toString().orEmpty(),
                        unit = form.etGoalUnit.text?.toString().orEmpty(),
                        deadline = form.etGoalDeadline.text?.toString().orEmpty(),
                    ),
                )
                showGoalErrors(form, errors)
                if (!errors.hasErrors) dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showRoutineDialog() {
        val state = viewModel.uiState.value
        val players = state.players
        if (players.isEmpty()) {
            Snackbar.make(requireView(), R.string.players_required_for_goals, Snackbar.LENGTH_LONG).show()
            return
        }
        val form = DialogCreateRoutineBinding.inflate(layoutInflater)
        val playerLabels = players.map { it.fullName }
        form.actvRoutinePlayer.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, playerLabels),
        )
        var selectedPlayerId = players.first().id
        var selectedGoalId: Long? = null
        var goalOptions = state.goals.filter { it.playerId == selectedPlayerId }
        fun refreshGoalOptions() {
            goalOptions = state.goals.filter { it.playerId == selectedPlayerId }
            val labels = listOf(getString(R.string.routine_without_goal)) + goalOptions.map { it.title }
            form.actvRoutineGoal.setAdapter(
                ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, labels),
            )
            form.actvRoutineGoal.setText(labels.first(), false)
            selectedGoalId = null
        }
        form.actvRoutinePlayer.setText(playerLabels.first(), false)
        refreshGoalOptions()
        form.actvRoutinePlayer.setOnItemClickListener { _, _, position, _ ->
            selectedPlayerId = players[position].id
            form.tilRoutinePlayer.error = null
            refreshGoalOptions()
        }
        form.actvRoutineGoal.setOnItemClickListener { _, _, position, _ ->
            selectedGoalId = goalOptions.getOrNull(position - 1)?.id
            form.tilRoutineGoal.error = null
        }
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.create_routine)
            .setView(form.root)
            .setNegativeButton(R.string.cancel_action, null)
            .setPositiveButton(R.string.save_action, null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val errors = viewModel.createRoutine(
                    RoutineFormInput(
                        playerId = selectedPlayerId,
                        goalId = selectedGoalId,
                        title = form.etRoutineTitle.text?.toString().orEmpty(),
                        description = form.etRoutineDescription.text?.toString().orEmpty(),
                        durationMinutes = form.etRoutineDuration.text?.toString().orEmpty(),
                        steps = form.etRoutineSteps.text?.toString().orEmpty(),
                    ),
                )
                showRoutineErrors(form, errors)
                if (!errors.hasErrors) dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showGoalErrors(form: DialogCreateGoalBinding, errors: GoalFormErrors) {
        form.tilGoalPlayer.error = errors.player
        form.tilGoalTitle.error = errors.title
        form.tilGoalCurrent.error = errors.currentValue
        form.tilGoalTarget.error = errors.targetValue
        form.tilGoalUnit.error = errors.unit
        form.tilGoalDeadline.error = errors.deadline
    }

    private fun showRoutineErrors(form: DialogCreateRoutineBinding, errors: RoutineFormErrors) {
        form.tilRoutinePlayer.error = errors.player
        form.tilRoutineGoal.error = errors.goal
        form.tilRoutineTitle.error = errors.title
        form.tilRoutineDuration.error = errors.durationMinutes
        form.tilRoutineSteps.error = errors.steps
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
