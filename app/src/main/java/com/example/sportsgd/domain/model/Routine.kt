package com.example.sportsgd.domain.model

import kotlin.math.roundToInt

data class RoutineStep(
    val id: Long = 0,
    val routineId: Long = 0,
    val orderIndex: Int,
    val title: String,
    val description: String = "",
    val durationSeconds: Int,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
)

data class Routine(
    val id: Long = 0,
    val playerId: Long,
    val goalId: Long? = null,
    val title: String,
    val description: String = "",
    val estimatedDurationMinutes: Int,
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val steps: List<RoutineStep> = emptyList(),
) {
    val completedStepCount: Int
        get() = steps.count(RoutineStep::isCompleted)

    val progressFraction: Float
        get() = when {
            steps.isEmpty() -> if (isCompleted) 1f else 0f
            else -> completedStepCount.toFloat() / steps.size
        }

    val progressPercent: Int
        get() = (progressFraction * 100).roundToInt()
}
