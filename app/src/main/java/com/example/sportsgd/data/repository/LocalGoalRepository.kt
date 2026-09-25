package com.example.sportsgd.data.repository

import com.example.sportsgd.data.local.dao.GoalDao
import com.example.sportsgd.data.mapper.toDomain
import com.example.sportsgd.data.mapper.toEntity
import com.example.sportsgd.domain.model.Goal
import com.example.sportsgd.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalGoalRepository(
    private val goalDao: GoalDao,
    private val awaitSeed: suspend () -> Unit = {},
) : GoalRepository {
    override fun observeGoals(): Flow<List<Goal>> = seededFlow(awaitSeed) {
        goalDao.observeAll().map { goals -> goals.map { it.toDomain() } }
    }

    override fun observeGoalsForPlayer(playerId: Long): Flow<List<Goal>> = seededFlow(awaitSeed) {
        goalDao.observeForPlayer(playerId).map { goals -> goals.map { it.toDomain() } }
    }

    override fun observeGoal(goalId: Long): Flow<Goal?> = seededFlow(awaitSeed) {
        goalDao.observeById(goalId).map { it?.toDomain() }
    }

    override suspend fun getGoal(goalId: Long): Goal? {
        awaitSeed()
        return goalDao.getById(goalId)?.toDomain()
    }

    override suspend fun saveGoal(goal: Goal): Long {
        awaitSeed()
        val normalizedTarget = goal.targetValue.coerceAtLeast(1)
        val normalizedCurrent = goal.currentValue.coerceIn(0, normalizedTarget)
        val normalized = goal.copy(
            currentValue = normalizedCurrent,
            targetValue = normalizedTarget,
            isCompleted = normalizedCurrent >= normalizedTarget,
        )
        return if (goal.id == 0L) {
            goalDao.insert(normalized.toEntity())
        } else {
            goalDao.update(normalized.toEntity())
            goal.id
        }
    }

    override suspend fun updateProgress(goalId: Long, currentValue: Int) {
        awaitSeed()
        val goal = goalDao.getById(goalId) ?: return
        val normalized = currentValue.coerceIn(0, goal.targetValue.coerceAtLeast(1))
        goalDao.updateProgress(
            goalId = goalId,
            currentValue = normalized,
            isCompleted = normalized >= goal.targetValue,
        )
    }

    override suspend fun deleteGoal(goalId: Long) {
        awaitSeed()
        goalDao.deleteById(goalId)
    }
}
