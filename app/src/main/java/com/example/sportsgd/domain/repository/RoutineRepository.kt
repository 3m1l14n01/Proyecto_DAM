package com.example.sportsgd.domain.repository

import com.example.sportsgd.domain.model.Routine
import kotlinx.coroutines.flow.Flow

interface RoutineRepository {
    fun observeRoutines(): Flow<List<Routine>>

    fun observeRoutinesForPlayer(playerId: Long): Flow<List<Routine>>

    fun observeRoutine(routineId: Long): Flow<Routine?>

    suspend fun getRoutine(routineId: Long): Routine?

    suspend fun saveRoutine(routine: Routine): Long

    suspend fun startRoutine(routineId: Long, startedAt: Long = System.currentTimeMillis()): Boolean

    suspend fun setStepCompleted(
        routineId: Long,
        stepId: Long,
        completed: Boolean,
        changedAt: Long = System.currentTimeMillis(),
    ): Boolean

    suspend fun completeRoutine(routineId: Long, completedAt: Long = System.currentTimeMillis()): Boolean

    suspend fun resetRoutine(routineId: Long): Boolean

    suspend fun deleteRoutine(routineId: Long)
}
