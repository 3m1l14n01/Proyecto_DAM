package com.example.sportsgd

import android.content.Context
import com.example.sportsgd.data.local.DatabaseSeeder
import com.example.sportsgd.data.local.SportsGdDatabase
import com.example.sportsgd.data.repository.LocalActivityRepository
import com.example.sportsgd.data.repository.LocalAuthRepository
import com.example.sportsgd.data.repository.LocalGoalRepository
import com.example.sportsgd.data.repository.LocalNotificationRepository
import com.example.sportsgd.data.repository.LocalPlayerRepository
import com.example.sportsgd.data.repository.LocalRoutineRepository
import com.example.sportsgd.data.session.SessionManager
import com.example.sportsgd.domain.repository.ActivityRepository
import com.example.sportsgd.domain.repository.AuthRepository
import com.example.sportsgd.domain.repository.GoalRepository
import com.example.sportsgd.domain.repository.NotificationRepository
import com.example.sportsgd.domain.repository.PlayerRepository
import com.example.sportsgd.domain.repository.RoutineRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel

class AppContainer(context: Context) {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: SportsGdDatabase = SportsGdDatabase.create(context)
    val sessionManager = SessionManager(context)

    private val seedJob = applicationScope.async {
        DatabaseSeeder(database).seed()
    }
    private val awaitSeed: suspend () -> Unit = { seedJob.await() }

    val authRepository: AuthRepository = LocalAuthRepository(sessionManager)
    val playerRepository: PlayerRepository = LocalPlayerRepository(
        playerDao = database.playerDao(),
        awaitSeed = awaitSeed,
    )
    val activityRepository: ActivityRepository = LocalActivityRepository(
        activityDao = database.activityDao(),
        awaitSeed = awaitSeed,
    )
    val goalRepository: GoalRepository = LocalGoalRepository(
        goalDao = database.goalDao(),
        awaitSeed = awaitSeed,
    )
    val routineRepository: RoutineRepository = LocalRoutineRepository(
        database = database,
        routineDao = database.routineDao(),
        routineStepDao = database.routineStepDao(),
        awaitSeed = awaitSeed,
    )
    val notificationRepository: NotificationRepository = LocalNotificationRepository(
        notificationDao = database.notificationDao(),
        awaitSeed = awaitSeed,
    )

    suspend fun awaitReady() {
        seedJob.await()
    }

    fun close() {
        applicationScope.cancel()
        database.close()
    }
}
