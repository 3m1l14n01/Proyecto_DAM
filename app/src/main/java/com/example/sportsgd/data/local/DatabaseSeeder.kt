package com.example.sportsgd.data.local

import androidx.room.withTransaction
import com.example.sportsgd.data.local.entity.ActivityEntity
import com.example.sportsgd.data.local.entity.AppNotificationEntity
import com.example.sportsgd.data.local.entity.GoalEntity
import com.example.sportsgd.data.local.entity.PlayerEntity
import com.example.sportsgd.data.local.entity.RoutineEntity
import com.example.sportsgd.data.local.entity.RoutineStepEntity
import com.example.sportsgd.domain.model.ActivityType
import com.example.sportsgd.domain.model.NotificationType
import com.example.sportsgd.domain.model.PlayerStatus

object DemoDataIds {
    const val ANA_PLAYER_ID = 1L
    const val DIEGO_PLAYER_ID = 2L
    const val SOFIA_PLAYER_ID = 3L

    const val TRAINING_ACTIVITY_ID = 1L
    const val EVIDENCE_ACTIVITY_ID = 2L
    const val MATCH_ACTIVITY_ID = 3L

    const val WEEKLY_GOAL_ID = 1L
    const val LOWER_BODY_ROUTINE_ID = 1L
    const val SPEED_ROUTINE_ID = LOWER_BODY_ROUTINE_ID
}

class DatabaseSeeder(
    private val database: SportsGdDatabase,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    suspend fun seed() {
        val now = clock()
        database.withTransaction {
            database.playerDao().insertSeeds(seedPlayers(now))
            database.activityDao().insertSeeds(seedActivities(now))
            refreshExpiredDemoActivities(now)
            database.goalDao().insertSeeds(seedGoals(now))
            database.routineDao().insertSeeds(seedRoutines(now))
            database.routineStepDao().insertSeeds(seedRoutineSteps())
            database.notificationDao().insertSeeds(seedNotifications(now))
        }
    }

    /** Keeps the fixed demo rows useful without touching activities created outside the seed. */
    private suspend fun refreshExpiredDemoActivities(now: Long) {
        val templates = seedActivities(now)
        templates.forEach { template ->
            val current = database.activityDao().getById(template.id) ?: return@forEach
            val scheduleExpired = current.endAt < now
            val repairedPlayerId = current.playerId ?: template.playerId
            if (!scheduleExpired && repairedPlayerId == current.playerId) return@forEach
            database.activityDao().update(
                current.copy(
                    startAt = if (scheduleExpired) template.startAt else current.startAt,
                    endAt = if (scheduleExpired) template.endAt else current.endAt,
                    playerId = repairedPlayerId,
                ),
            )
        }
    }

    private fun seedPlayers(now: Long) = listOf(
        PlayerEntity(
            id = DemoDataIds.ANA_PLAYER_ID,
            firstName = "Ana",
            lastName = "Martínez",
            email = "ana.martinez@sportsgd.mx",
            phone = "555 010 1001",
            birthDate = "2003-04-18",
            sport = "Voleibol",
            position = "Colocadora",
            team = "Selección universitaria",
            semester = 4,
            average = 9.2,
            status = PlayerStatus.ACTIVE,
            createdAt = now - DAYS_30,
        ),
        PlayerEntity(
            id = DemoDataIds.DIEGO_PLAYER_ID,
            firstName = "Diego",
            lastName = "Torres",
            email = "diego.torres@sportsgd.mx",
            phone = "555 010 1002",
            birthDate = "2002-11-03",
            sport = "Fútbol",
            position = "Portero",
            team = "Selección universitaria",
            semester = 3,
            average = 8.8,
            status = PlayerStatus.ACTIVE,
            createdAt = now - DAYS_20,
        ),
        PlayerEntity(
            id = DemoDataIds.SOFIA_PLAYER_ID,
            firstName = "Sofía",
            lastName = "Hernández",
            email = "sofia.hernandez@sportsgd.mx",
            phone = "555 010 1003",
            birthDate = "2004-07-29",
            sport = "Atletismo",
            position = "Velocista",
            team = "Representativo",
            semester = 2,
            average = 9.5,
            status = PlayerStatus.ACTIVE,
            createdAt = now - DAYS_10,
        ),
    )

    private fun seedActivities(now: Long) = listOf(
        ActivityEntity(
            id = DemoDataIds.TRAINING_ACTIVITY_ID,
            title = "Entrenamiento de saque",
            description = "Sesión enfocada en técnica, precisión y potencia de saque.",
            type = ActivityType.TRAINING,
            startAt = now + DAYS_1,
            endAt = now + DAYS_1 + MINUTES_90,
            location = "Cancha A",
            playerId = DemoDataIds.ANA_PLAYER_ID,
            createdAt = now,
        ),
        ActivityEntity(
            id = DemoDataIds.EVIDENCE_ACTIVITY_ID,
            title = "Entrega de evidencia física",
            description = "Sube la evidencia de tu actividad física antes de la fecha límite.",
            type = ActivityType.ACADEMIC,
            startAt = now + DAYS_3,
            endAt = now + DAYS_3 + MINUTES_30,
            location = "Plataforma SPORTSGD",
            playerId = null,
            createdAt = now,
        ),
        ActivityEntity(
            id = DemoDataIds.MATCH_ACTIVITY_ID,
            title = "Partido amistoso universitario",
            description = "Encuentro deportivo de preparación de la selección universitaria.",
            type = ActivityType.MATCH,
            startAt = now + DAYS_5,
            endAt = now + DAYS_5 + MINUTES_90,
            location = "Cancha central",
            playerId = null,
            createdAt = now,
        ),
    )

    private fun seedGoals(now: Long) = listOf(
        GoalEntity(
            id = DemoDataIds.WEEKLY_GOAL_ID,
            playerId = DemoDataIds.ANA_PLAYER_ID,
            title = "Completar 3 sesiones de fuerza",
            description = "Completa las tres sesiones de fuerza programadas.",
            currentValue = 2,
            targetValue = 3,
            unit = "sesiones",
            deadlineAt = now + DAYS_7,
            isCompleted = false,
            createdAt = now - DAYS_3,
        ),
    )

    private fun seedRoutines(now: Long) = listOf(
        RoutineEntity(
            id = DemoDataIds.LOWER_BODY_ROUTINE_ID,
            playerId = DemoDataIds.ANA_PLAYER_ID,
            goalId = DemoDataIds.WEEKLY_GOAL_ID,
            title = "Fuerza de tren inferior",
            description = "Rutina enfocada en fuerza y estabilidad del tren inferior.",
            estimatedDurationMinutes = 35,
            startedAt = null,
            completedAt = null,
            isCompleted = false,
            createdAt = now - DAYS_2,
        ),
    )

    private fun seedRoutineSteps() = listOf(
        RoutineStepEntity(
            id = 1,
            routineId = DemoDataIds.LOWER_BODY_ROUTINE_ID,
            orderIndex = 0,
            title = "Sentadillas",
            description = "3 series",
            durationSeconds = 10 * 60,
            isCompleted = false,
            completedAt = null,
        ),
        RoutineStepEntity(
            id = 2,
            routineId = DemoDataIds.LOWER_BODY_ROUTINE_ID,
            orderIndex = 1,
            title = "Zancadas",
            description = "3 series",
            durationSeconds = 10 * 60,
            isCompleted = false,
            completedAt = null,
        ),
        RoutineStepEntity(
            id = 3,
            routineId = DemoDataIds.LOWER_BODY_ROUTINE_ID,
            orderIndex = 2,
            title = "Puente de glúteos",
            description = "3 series",
            durationSeconds = 10 * 60,
            isCompleted = false,
            completedAt = null,
        ),
        RoutineStepEntity(
            id = 4,
            routineId = DemoDataIds.LOWER_BODY_ROUTINE_ID,
            orderIndex = 3,
            title = "Estiramientos",
            description = "5 min",
            durationSeconds = 5 * 60,
            isCompleted = false,
            completedAt = null,
        ),
    )

    private fun seedNotifications(now: Long) = listOf(
        AppNotificationEntity(
            id = 1,
            title = "Meta actualizada",
            message = "Has completado 2 de 3 sesiones de fuerza.",
            type = NotificationType.GOAL,
            createdAt = now - MINUTES_10,
            isRead = false,
            destination = "goal/${DemoDataIds.WEEKLY_GOAL_ID}",
        ),
        AppNotificationEntity(
            id = 2,
            title = "Recordatorio de actividad",
            message = "El entrenamiento de saque comienza mañana en Cancha A.",
            type = NotificationType.ACTIVITY,
            createdAt = now - HOURS_4,
            isRead = false,
            destination = "activity/${DemoDataIds.TRAINING_ACTIVITY_ID}",
        ),
        AppNotificationEntity(
            id = 3,
            title = "Evidencia pendiente",
            message = "Recuerda entregar tu evidencia física antes de la fecha límite.",
            type = NotificationType.ACTIVITY,
            createdAt = now - DAYS_1,
            isRead = true,
            destination = "activity/${DemoDataIds.EVIDENCE_ACTIVITY_ID}",
        ),
    )

    private companion object {
        const val MINUTE = 60_000L
        const val HOUR = 60 * MINUTE
        const val DAY = 24 * HOUR

        const val MINUTES_10 = 10 * MINUTE
        const val MINUTES_30 = 30 * MINUTE
        const val MINUTES_90 = 90 * MINUTE
        const val HOURS_4 = 4 * HOUR
        const val DAYS_1 = DAY
        const val DAYS_2 = 2 * DAY
        const val DAYS_3 = 3 * DAY
        const val DAYS_5 = 5 * DAY
        const val DAYS_7 = 7 * DAY
        const val DAYS_10 = 10 * DAY
        const val DAYS_20 = 20 * DAY
        const val DAYS_30 = 30 * DAY
    }
}
