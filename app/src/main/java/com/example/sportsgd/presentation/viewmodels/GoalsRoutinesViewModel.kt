package com.example.sportsgd.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsgd.domain.model.Goal
import com.example.sportsgd.domain.model.Player
import com.example.sportsgd.domain.model.Routine
import com.example.sportsgd.domain.model.RoutineStep
import com.example.sportsgd.domain.model.UserRole
import com.example.sportsgd.domain.repository.AuthRepository
import com.example.sportsgd.domain.repository.GoalRepository
import com.example.sportsgd.domain.repository.PlayerRepository
import com.example.sportsgd.domain.repository.RoutineRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GoalsRoutinesUiState(
    val isLoading: Boolean = true,
    val players: List<Player> = emptyList(),
    val goals: List<Goal> = emptyList(),
    val routines: List<Routine> = emptyList(),
    val selectedRoutine: Routine? = null,
    val inProgressRoutine: Routine? = null,
    val isMutating: Boolean = false,
    val actionResult: RoutineActionResult? = null,
) {
    val completedRoutineCount: Int
        get() = routines.count(Routine::isCompleted)
}

sealed interface RoutineActionResult {
    data class Started(val routineId: Long) : RoutineActionResult
    data class Completed(val routineId: Long) : RoutineActionResult
    data class StepUpdated(val routineId: Long, val stepId: Long, val completed: Boolean) :
        RoutineActionResult
    data class Reset(val routineId: Long) : RoutineActionResult
    data class GoalCreated(val goalId: Long) : RoutineActionResult
    data class RoutineCreated(val routineId: Long) : RoutineActionResult
    data class Failure(val message: String) : RoutineActionResult
}

data class GoalFormInput(
    val playerId: Long,
    val title: String,
    val description: String,
    val currentValue: String,
    val targetValue: String,
    val unit: String,
    val deadline: String,
)

data class GoalFormErrors(
    val player: String? = null,
    val title: String? = null,
    val currentValue: String? = null,
    val targetValue: String? = null,
    val unit: String? = null,
    val deadline: String? = null,
) {
    val hasErrors: Boolean get() = listOf(player, title, currentValue, targetValue, unit, deadline)
        .any { it != null }
}

data class RoutineFormInput(
    val playerId: Long,
    val goalId: Long?,
    val title: String,
    val description: String,
    val durationMinutes: String,
    val steps: String,
)

data class RoutineFormErrors(
    val player: String? = null,
    val goal: String? = null,
    val title: String? = null,
    val durationMinutes: String? = null,
    val steps: String? = null,
) {
    val hasErrors: Boolean get() = listOf(player, goal, title, durationMinutes, steps)
        .any { it != null }
}

class GoalsRoutinesViewModel(
    private val goalRepository: GoalRepository,
    private val routineRepository: RoutineRepository,
    private val playerRepository: PlayerRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val selectedRoutineId = MutableStateFlow<Long?>(null)
    private val isMutating = MutableStateFlow(false)
    private val actionResult = MutableStateFlow<RoutineActionResult?>(null)

    private val catalog = combine(
        goalRepository.observeGoals(),
        routineRepository.observeRoutines(),
        playerRepository.observePlayers(),
    ) { goals, routines, players -> Triple(goals, routines, players) }

    val uiState: StateFlow<GoalsRoutinesUiState> = combine(
        catalog,
        selectedRoutineId,
        isMutating,
        actionResult,
    ) { (goals, routines, players), selectedId, mutating, result ->
        val sortedRoutines = routines.sortedWith(
            compareBy<Routine> { it.isCompleted }.thenByDescending { it.startedAt ?: it.createdAt },
        )
        GoalsRoutinesUiState(
            isLoading = false,
            players = players,
            goals = goals.sortedBy { it.deadlineAt ?: Long.MAX_VALUE },
            routines = sortedRoutines,
            selectedRoutine = sortedRoutines.firstOrNull { it.id == selectedId },
            inProgressRoutine = sortedRoutines.firstOrNull {
                it.startedAt != null && !it.isCompleted
            },
            isMutating = mutating,
            actionResult = result,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GoalsRoutinesUiState(),
    )

    fun selectRoutine(routineId: Long?) {
        selectedRoutineId.value = routineId?.takeIf { it > 0L }
    }

    /** Returns field errors immediately so a dialog stays open until its data is valid. */
    fun createGoal(input: GoalFormInput): GoalFormErrors {
        if (authRepository.session.value?.role != UserRole.COACH) {
            return GoalFormErrors(player = "Solo un entrenador puede crear metas.")
        }
        val target = input.targetValue.trim().toIntOrNull()
        val current = input.currentValue.trim().ifBlank { "0" }.toIntOrNull()
        val deadline = parseDeadline(input.deadline)
        val errors = GoalFormErrors(
            player = "Selecciona un jugador.".takeIf {
                uiState.value.players.none { player -> player.id == input.playerId }
            },
            title = "Escribe un título de 2 a 80 caracteres.".takeIf {
                input.title.trim().length !in 2..80
            },
            currentValue = "Ingresa un avance entre 0 y el objetivo.".takeIf {
                current == null || current < 0 || (target != null && current > target)
            },
            targetValue = "El objetivo debe estar entre 1 y 100 000.".takeIf {
                target == null || target !in 1..100_000
            },
            unit = "Escribe una unidad de medida.".takeIf { input.unit.isBlank() },
            deadline = "Usa AAAA-MM-DD para una fecha válida.".takeIf {
                input.deadline.isNotBlank() && deadline == null
            },
        )
        if (errors.hasErrors) return errors

        mutate(
            operation = {
                requireNotNull(playerRepository.getPlayer(input.playerId))
                val goalId = goalRepository.saveGoal(
                    Goal(
                        playerId = input.playerId,
                        title = input.title.trim(),
                        description = input.description.trim(),
                        currentValue = current ?: 0,
                        targetValue = target ?: 1,
                        unit = input.unit.trim(),
                        deadlineAt = deadline,
                    ),
                )
                RoutineActionResult.GoalCreated(goalId)
            },
            failureMessage = "No fue posible guardar la meta. Verifica el jugador.",
        )
        return GoalFormErrors()
    }

    fun createRoutine(input: RoutineFormInput): RoutineFormErrors {
        if (authRepository.session.value?.role != UserRole.COACH) {
            return RoutineFormErrors(player = "Solo un entrenador puede crear rutinas.")
        }
        val duration = input.durationMinutes.trim().toIntOrNull()
        val steps = input.steps.lineSequence().map(String::trim).filter(String::isNotEmpty).toList()
        val errors = RoutineFormErrors(
            player = "Selecciona un jugador.".takeIf {
                uiState.value.players.none { player -> player.id == input.playerId }
            },
            goal = "Selecciona una meta de este jugador.".takeIf {
                input.goalId != null && uiState.value.goals.none { goal ->
                    goal.id == input.goalId && goal.playerId == input.playerId
                }
            },
            title = "Escribe un título de 2 a 80 caracteres.".takeIf {
                input.title.trim().length !in 2..80
            },
            durationMinutes = "La duración debe estar entre 1 y 480 minutos.".takeIf {
                duration == null || duration !in 1..480
            },
            steps = "Escribe entre 1 y 20 pasos, uno por línea (máximo 80 caracteres).".takeIf {
                steps.size !in 1..20 || steps.any { it.length > 80 }
            },
        )
        if (errors.hasErrors) return errors

        mutate(
            operation = {
                requireNotNull(playerRepository.getPlayer(input.playerId))
                if (input.goalId != null) {
                    require(goalRepository.getGoal(input.goalId)?.playerId == input.playerId)
                }
                val stepSeconds = (duration ?: 1) * 60 / steps.size
                val routineId = routineRepository.saveRoutine(
                    Routine(
                        playerId = input.playerId,
                        goalId = input.goalId,
                        title = input.title.trim(),
                        description = input.description.trim(),
                        estimatedDurationMinutes = duration ?: 1,
                        steps = steps.mapIndexed { index, title ->
                            RoutineStep(orderIndex = index, title = title, durationSeconds = stepSeconds)
                        },
                    ),
                )
                RoutineActionResult.RoutineCreated(routineId)
            },
            failureMessage = "No fue posible guardar la rutina. Verifica jugador y meta.",
        )
        return RoutineFormErrors()
    }

    fun startRoutine(routineId: Long) {
        if (routineId <= 0L) return
        selectedRoutineId.value = routineId
        mutate(
            operation = { routineRepository.startRoutine(routineId) },
            success = { RoutineActionResult.Started(routineId) },
        )
    }

    fun completeRoutine(routineId: Long) {
        if (routineId <= 0L) return
        selectedRoutineId.value = routineId
        mutate(
            operation = { routineRepository.completeRoutine(routineId) },
            success = { RoutineActionResult.Completed(routineId) },
        )
    }

    fun toggleStep(routineId: Long, stepId: Long, completed: Boolean) {
        if (routineId <= 0L || stepId <= 0L) return
        selectedRoutineId.value = routineId
        mutate(
            operation = {
                routineRepository.setStepCompleted(
                    routineId = routineId,
                    stepId = stepId,
                    completed = completed,
                )
            },
            success = { RoutineActionResult.StepUpdated(routineId, stepId, completed) },
        )
    }

    fun resetRoutine(routineId: Long) {
        if (routineId <= 0L) return
        selectedRoutineId.value = routineId
        mutate(
            operation = { routineRepository.resetRoutine(routineId) },
            success = { RoutineActionResult.Reset(routineId) },
        )
    }

    fun clearActionResult() {
        actionResult.value = null
    }

    private fun mutate(
        operation: suspend () -> Boolean,
        success: () -> RoutineActionResult,
    ) {
        if (isMutating.value) return
        viewModelScope.launch {
            isMutating.value = true
            actionResult.value = null
            actionResult.value = runCatching {
                if (operation()) {
                    success()
                } else {
                    RoutineActionResult.Failure("La rutina cambió o ya no está disponible.")
                }
            }.getOrElse {
                RoutineActionResult.Failure("No fue posible actualizar la rutina.")
            }
            isMutating.value = false
        }
    }

    private fun mutate(
        operation: suspend () -> RoutineActionResult,
        failureMessage: String,
    ) {
        if (isMutating.value) return
        viewModelScope.launch {
            isMutating.value = true
            actionResult.value = null
            actionResult.value = runCatching { operation() }.getOrElse {
                RoutineActionResult.Failure(failureMessage)
            }
            isMutating.value = false
        }
    }

    private fun parseDeadline(raw: String): Long? {
        val value = raw.trim()
        if (value.isEmpty()) return null
        if (!Regex("\\d{4}-\\d{2}-\\d{2}").matches(value)) return null
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply { isLenient = false }
        val date = runCatching { format.parse(value) }.getOrNull() ?: return null
        return Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }
}

typealias GoalsViewModel = GoalsRoutinesViewModel
typealias RoutinesViewModel = GoalsRoutinesViewModel
