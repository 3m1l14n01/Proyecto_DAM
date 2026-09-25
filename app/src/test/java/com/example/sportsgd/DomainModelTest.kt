package com.example.sportsgd

import com.example.sportsgd.domain.model.Goal
import com.example.sportsgd.domain.model.Player
import com.example.sportsgd.domain.model.PlayerStatus
import com.example.sportsgd.domain.model.Routine
import com.example.sportsgd.domain.model.RoutineStep
import org.junit.Assert.assertEquals
import org.junit.Test

class DomainModelTest {

    @Test
    fun goalProgress_isCalculatedAndClamped() {
        val activeGoal = goal(current = 2, target = 3)
        val exceededGoal = goal(current = 8, target = 3)

        assertEquals(67, activeGoal.progressPercent)
        assertEquals(100, exceededGoal.progressPercent)
    }

    @Test
    fun routineProgress_reflectsCompletedSteps() {
        val routine = Routine(
            id = 1,
            playerId = 1,
            title = "Fuerza de tren inferior",
            estimatedDurationMinutes = 35,
            steps = listOf(
                step(1, completed = true),
                step(2, completed = true),
                step(3, completed = false),
                step(4, completed = false),
            ),
        )

        assertEquals(2, routine.completedStepCount)
        assertEquals(50, routine.progressPercent)
    }

    @Test
    fun playerFullName_ignoresBlankComponents() {
        val player = Player(
            firstName = "Ana",
            lastName = "Martínez",
            email = "ana@sportsgd.mx",
            sport = "Voleibol",
            position = "Colocadora",
            team = "Selección universitaria",
            semester = 4,
            average = 9.2,
            status = PlayerStatus.ACTIVE,
        )

        assertEquals("Ana Martínez", player.fullName)
    }

    private fun goal(current: Int, target: Int) = Goal(
        playerId = 1,
        title = "Completar sesiones",
        currentValue = current,
        targetValue = target,
        unit = "sesiones",
    )

    private fun step(id: Long, completed: Boolean) = RoutineStep(
        id = id,
        routineId = 1,
        orderIndex = id.toInt(),
        title = "Paso $id",
        durationSeconds = 60,
        isCompleted = completed,
    )
}
