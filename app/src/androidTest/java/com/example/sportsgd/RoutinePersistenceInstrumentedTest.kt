package com.example.sportsgd

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.sportsgd.data.local.DatabaseSeeder
import com.example.sportsgd.data.local.DemoDataIds
import com.example.sportsgd.data.local.SportsGdDatabase
import com.example.sportsgd.data.repository.LocalRoutineRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoutinePersistenceInstrumentedTest {
    @Test
    fun completingRoutineUpdatesRoomStateExactlyOnce() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(
            context,
            SportsGdDatabase::class.java,
        ).build()
        try {
            DatabaseSeeder(database).seed()
            val repository = LocalRoutineRepository(
                database = database,
                routineDao = database.routineDao(),
                routineStepDao = database.routineStepDao(),
            )
            val routineId = DemoDataIds.LOWER_BODY_ROUTINE_ID
            val stepIds = database.routineStepDao().getForRoutine(routineId).map { it.id }
            stepIds.forEach { stepId ->
                repository.setStepCompleted(routineId, stepId, true, System.currentTimeMillis())
            }
            val lastStepId = stepIds.last()
            repository.setStepCompleted(routineId, lastStepId, false, System.currentTimeMillis())
            repository.setStepCompleted(routineId, lastStepId, true, System.currentTimeMillis())
            assertEquals(2, database.goalDao().getById(DemoDataIds.WEEKLY_GOAL_ID)?.currentValue)
            assertFalse(requireNotNull(database.routineDao().getByIdWithSteps(routineId)).routine.isCompleted)
            assertEquals(
                0,
                database.notificationDao().observeAll().first().count { it.title == "Rutina completada" },
            )

            assertTrue(repository.completeRoutine(routineId, System.currentTimeMillis()))
            assertFalse(repository.completeRoutine(routineId, System.currentTimeMillis()))
            assertFalse(repository.startRoutine(Long.MAX_VALUE, System.currentTimeMillis()))
            assertFalse(
                repository.setStepCompleted(
                    routineId = routineId,
                    stepId = Long.MAX_VALUE,
                    completed = true,
                    changedAt = System.currentTimeMillis(),
                ),
            )

            val goal = requireNotNull(database.goalDao().getById(DemoDataIds.WEEKLY_GOAL_ID))
            val routine = requireNotNull(database.routineDao().getByIdWithSteps(routineId))
            val completionNotices = database.notificationDao().observeAll().first()
                .filter { it.title == "Rutina completada" }

            assertEquals(3, goal.currentValue)
            assertTrue(goal.isCompleted)
            assertTrue(routine.routine.isCompleted)
            assertTrue(routine.steps.all { it.isCompleted })
            assertEquals(1, completionNotices.size)
        } finally {
            database.close()
        }
    }
}
