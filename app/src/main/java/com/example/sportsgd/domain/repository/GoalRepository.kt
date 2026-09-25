package com.example.sportsgd.domain.repository

import com.example.sportsgd.domain.model.Goal
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    fun observeGoals(): Flow<List<Goal>>

    fun observeGoalsForPlayer(playerId: Long): Flow<List<Goal>>

    fun observeGoal(goalId: Long): Flow<Goal?>

    suspend fun getGoal(goalId: Long): Goal?

    suspend fun saveGoal(goal: Goal): Long

    suspend fun updateProgress(goalId: Long, currentValue: Int)

    suspend fun deleteGoal(goalId: Long)
}
