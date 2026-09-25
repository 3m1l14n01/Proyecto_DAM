package com.example.sportsgd.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sportsgd.SportsGdApplication

class AppViewModelFactory(
    private val application: SportsGdApplication,
    private val playerId: Long? = null,
    private val activityId: Long? = null,
    private val routineId: Long? = null,
) : ViewModelProvider.Factory {

    private val container
        get() = application.container

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(LoginViewModel::class.java) -> LoginViewModel(
            authRepository = container.authRepository,
        )

        modelClass.isAssignableFrom(DashboardViewModel::class.java) -> DashboardViewModel(
            playerRepository = container.playerRepository,
            activityRepository = container.activityRepository,
            goalRepository = container.goalRepository,
            notificationRepository = container.notificationRepository,
            authRepository = container.authRepository,
        )

        modelClass.isAssignableFrom(PlayersViewModel::class.java) -> PlayersViewModel(
            playerRepository = container.playerRepository,
            authRepository = container.authRepository,
        )

        modelClass.isAssignableFrom(PlayerDetailViewModel::class.java) -> PlayerDetailViewModel(
            playerId = playerId ?: 0L,
            playerRepository = container.playerRepository,
            activityRepository = container.activityRepository,
        )

        modelClass.isAssignableFrom(CalendarViewModel::class.java) -> CalendarViewModel(
            activityRepository = container.activityRepository,
        )

        modelClass.isAssignableFrom(ActivityDetailViewModel::class.java) -> ActivityDetailViewModel(
            activityId = activityId ?: 0L,
            activityRepository = container.activityRepository,
        )

        modelClass.isAssignableFrom(GoalsRoutinesViewModel::class.java) ->
            GoalsRoutinesViewModel(
                goalRepository = container.goalRepository,
                routineRepository = container.routineRepository,
                playerRepository = container.playerRepository,
                authRepository = container.authRepository,
            ).also { viewModel ->
                routineId?.takeIf { it > 0L }?.let(viewModel::selectRoutine)
            }

        modelClass.isAssignableFrom(NotificationsViewModel::class.java) -> NotificationsViewModel(
            notificationRepository = container.notificationRepository,
        )

        else -> error("Unsupported ViewModel class: ${modelClass.name}")
    } as T
}
