package com.example.sportsgd.data.repository

import androidx.room.withTransaction
import com.example.sportsgd.data.local.SportsGdDatabase
import com.example.sportsgd.data.local.dao.RoutineDao
import com.example.sportsgd.data.local.dao.RoutineStepDao
import com.example.sportsgd.data.local.entity.AppNotificationEntity
import com.example.sportsgd.data.mapper.toDomain
import com.example.sportsgd.data.mapper.toEntity
import com.example.sportsgd.domain.model.NotificationType
import com.example.sportsgd.domain.model.Routine
import com.example.sportsgd.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalRoutineRepository(
    private val database: SportsGdDatabase,
    private val routineDao: RoutineDao,
    private val routineStepDao: RoutineStepDao,
    private val awaitSeed: suspend () -> Unit = {},
) : RoutineRepository {
    override fun observeRoutines(): Flow<List<Routine>> = seededFlow(awaitSeed) {
        routineDao.observeAllWithSteps().map { routines -> routines.map { it.toDomain() } }
    }

    override fun observeRoutinesForPlayer(playerId: Long): Flow<List<Routine>> =
        seededFlow(awaitSeed) {
            routineDao.observeForPlayerWithSteps(playerId).map { routines -> routines.map { it.toDomain() } }
        }

    override fun observeRoutine(routineId: Long): Flow<Routine?> = seededFlow(awaitSeed) {
        routineDao.observeByIdWithSteps(routineId).map { it?.toDomain() }
    }

    override suspend fun getRoutine(routineId: Long): Routine? {
        awaitSeed()
        return routineDao.getByIdWithSteps(routineId)?.toDomain()
    }

    override suspend fun saveRoutine(routine: Routine): Long {
        awaitSeed()
        return database.withTransaction {
            val routineId = if (routine.id == 0L) {
                routineDao.insert(routine.toEntity())
            } else {
                routineDao.update(routine.toEntity())
                routine.id
            }

            routineStepDao.deleteForRoutine(routineId)
            if (routine.steps.isNotEmpty()) {
                routineStepDao.insertAll(
                    routine.steps.map { step ->
                        step.toEntity(parentRoutineId = routineId, persistedId = 0)
                    },
                )
            }
            routineId
        }
    }

    override suspend fun startRoutine(routineId: Long, startedAt: Long): Boolean {
        awaitSeed()
        return database.withTransaction {
            val routine = routineDao.getByIdWithSteps(routineId) ?: return@withTransaction false
            if (routine.routine.isCompleted) return@withTransaction false
            routineDao.markStarted(routineId, startedAt) > 0
        }
    }

    override suspend fun setStepCompleted(
        routineId: Long,
        stepId: Long,
        completed: Boolean,
        changedAt: Long,
    ): Boolean {
        awaitSeed()
        return database.withTransaction {
            val routine = routineDao.getByIdWithSteps(routineId) ?: return@withTransaction false
            if (routine.routine.isCompleted) return@withTransaction false
            routineStepDao.updateCompletion(
                routineId = routineId,
                stepId = stepId,
                completed = completed,
                completedAt = changedAt.takeIf { completed },
            ) > 0
        }
    }

    override suspend fun completeRoutine(routineId: Long, completedAt: Long): Boolean {
        awaitSeed()
        return database.withTransaction {
            val routine = routineDao.getByIdWithSteps(routineId) ?: return@withTransaction false
            if (routine.routine.isCompleted) return@withTransaction false
            routineStepDao.completeAll(routineId, completedAt)
            if (routineDao.updateCompletion(routineId, completed = true, completedAt = completedAt) == 0) {
                return@withTransaction false
            }
            incrementLinkedGoal(routine.routine.goalId)
            addCompletionNotification(
                routineId = routineId,
                title = routine.routine.title,
                goalId = routine.routine.goalId,
                completedAt = completedAt,
            )
            true
        }
    }

    override suspend fun resetRoutine(routineId: Long): Boolean {
        awaitSeed()
        return database.withTransaction {
            val routine = routineDao.getByIdWithSteps(routineId) ?: return@withTransaction false
            if (routine.routine.isCompleted) return@withTransaction false
            routineStepDao.resetForRoutine(routineId)
            routineDao.reset(routineId) > 0
        }
    }

    override suspend fun deleteRoutine(routineId: Long) {
        awaitSeed()
        routineDao.deleteById(routineId)
    }

    private suspend fun incrementLinkedGoal(goalId: Long?) {
        val linkedGoalId = goalId ?: return
        val goal = database.goalDao().getById(linkedGoalId) ?: return
        val updatedValue = (goal.currentValue + 1).coerceAtMost(goal.targetValue.coerceAtLeast(1))
        database.goalDao().updateProgress(
            goalId = linkedGoalId,
            currentValue = updatedValue,
            isCompleted = updatedValue >= goal.targetValue,
        )
    }

    private suspend fun addCompletionNotification(
        routineId: Long,
        title: String,
        goalId: Long?,
        completedAt: Long,
    ) {
        database.notificationDao().insert(
            AppNotificationEntity(
                title = "Rutina completada",
                message = "Completaste $title. Tu progreso quedó guardado.",
                type = NotificationType.ROUTINE,
                createdAt = completedAt,
                isRead = false,
                destination = goalId?.let { "goal/$it" } ?: "routine/$routineId",
            ),
        )
    }
}
