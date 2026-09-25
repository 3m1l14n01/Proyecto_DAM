package com.example.sportsgd.presentation.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sportsgd.R
import com.example.sportsgd.SportsGdApplication
import com.example.sportsgd.databinding.FragmentRoutineActiveBinding
import com.example.sportsgd.presentation.adapters.RoutineStepAdapter
import com.example.sportsgd.presentation.viewmodels.AppViewModelFactory
import com.example.sportsgd.presentation.viewmodels.GoalsRoutinesViewModel
import com.example.sportsgd.presentation.viewmodels.RoutineActionResult
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class RoutineActiveFragment : BaseAppFragment(R.layout.fragment_routine_active) {
    private var binding: FragmentRoutineActiveBinding? = null
    private val routineId: Long by lazy { arguments?.getLong("routineId", -1L) ?: -1L }
    private val viewModel: GoalsRoutinesViewModel by activityViewModels {
        AppViewModelFactory(
            application = requireActivity().application as SportsGdApplication,
            routineId = routineId,
        )
    }
    private var missingHandled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.selectRoutine(routineId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentBinding = FragmentRoutineActiveBinding.bind(view)
        binding = currentBinding
        setupAppChrome(view)
        val adapter = RoutineStepAdapter { step, completed ->
            viewModel.toggleStep(routineId, step.id, completed)
        }
        currentBinding.rvRoutineSteps.layoutManager = LinearLayoutManager(requireContext())
        currentBinding.rvRoutineSteps.adapter = adapter
        currentBinding.btnFinishRoutine.setOnClickListener { viewModel.completeRoutine(routineId) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val safeBinding = binding ?: return@collect
                    val routine = state.routines.firstOrNull { it.id == routineId }
                    if (routine != null) {
                        safeBinding.tvRoutineTitle.text = "Rutina en curso"
                        safeBinding.tvRoutineMeta.text = "${routine.title} · ${routine.estimatedDurationMinutes} min"
                        safeBinding.tvRoutineStatus.text = if (routine.isCompleted) "COMPLETADA" else "EN PROGRESO"
                        safeBinding.btnFinishRoutine.isEnabled = !state.isMutating
                        adapter.submitList(routine.steps)
                    } else if (!state.isLoading && !missingHandled) {
                        missingHandled = true
                        Snackbar.make(view, "No se encontró la rutina solicitada.", Snackbar.LENGTH_LONG).show()
                        findNavController().popBackStack()
                    }

                    when (val action = state.actionResult) {
                        is RoutineActionResult.Completed -> {
                            viewModel.clearActionResult()
                            findNavController().navigateSafely(
                                R.id.routineCompletedFragment,
                                Bundle().apply { putLong("routineId", action.routineId) },
                            )
                        }
                        is RoutineActionResult.Failure -> {
                            Snackbar.make(view, action.message, Snackbar.LENGTH_LONG).show()
                            viewModel.clearActionResult()
                        }
                        is RoutineActionResult.StepUpdated -> viewModel.clearActionResult()
                        else -> Unit
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        binding?.rvRoutineSteps?.adapter = null
        binding = null
        super.onDestroyView()
    }
}
