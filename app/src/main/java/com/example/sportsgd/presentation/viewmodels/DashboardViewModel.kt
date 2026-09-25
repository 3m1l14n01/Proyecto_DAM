package com.example.sportsgd.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsgd.domain.model.Goal
import com.example.sportsgd.domain.model.ActivityType
import com.example.sportsgd.domain.model.SportActivity
import com.example.sportsgd.domain.repository.ActivityRepository
import com.example.sportsgd.domain.repository.AuthRepository
import com.example.sportsgd.domain.repository.GoalRepository
import com.example.sportsgd.domain.repository.NotificationRepository
import com.example.sportsgd.domain.repository.PlayerRepository
import java.util.Calendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlin.math.roundToInt

data class DashboardUiState(
    val isLoading: Boolean = true,
    val registeredPlayers: Int = 0,
    val activePlayers: Int = 0,
    val injuredPlayers: Int = 0,
    val upcomingActivities: List<SportActivity> = emptyList(),
    val goals: List<Goal> = emptyList(),
    val unreadNotifications: Int = 0,
    val completedGoals: Int = 0,
    val averageGoalProgress: Int = 0,
    val todayTrainingSessions: Int = 0,
) {
    val nextActivity: SportActivity?
        get() = upcomingActivities.firstOrNull()

    val upcomingActivityCount: Int
        get() = upcomingActivities.size
}

private data class PlayerSummary(
    val registered: Int,
    val active: Int,
    val injured: Int,
)

private data class DashboardContent(
    val upcoming: List<SportActivity>,
    val goals: List<Goal>,
    val unread: Int,
    val todayTrainingSessions: Int,
)

class DashboardViewModel(
    playerRepository: PlayerRepository,
    activityRepository: ActivityRepository,
    goalRepository: GoalRepository,
    notificationRepository: NotificationRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val currentTime = MutableStateFlow(System.currentTimeMillis())

    private val playerSummary = combine(
        playerRepository.observePlayerCount(),
        playerRepository.observeActivePlayerCount(),
        playerRepository.observeInjuredPlayerCount(),
    ) { registered, active, injured ->
        PlayerSummary(registered = registered, active = active, injured = injured)
    }

    private val dashboardContent = combine(
        activityRepository.observeActivities(),
        goalRepository.observeGoals(),
        notificationRepository.observeUnreadCount(),
        currentTime,
    ) { activities, goals, unread, now ->
        val startOfDay = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val endOfDay = Calendar.getInstance().apply {
            timeInMillis = startOfDay
            add(Calendar.DAY_OF_MONTH, 1)
        }.timeInMillis
        DashboardContent(
            upcoming = activities.filter { it.endAt >= now }.sortedBy(SportActivity::startAt),
            goals = goals,
            unread = unread,
            todayTrainingSessions = activities.count {
                it.type == ActivityType.TRAINING && it.startAt in startOfDay until endOfDay
            },
        )
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        playerSummary,
        dashboardContent,
    ) { players, content ->
        DashboardUiState(
            isLoading = false,
            registeredPlayers = players.registered,
            activePlayers = players.active,
            injuredPlayers = players.injured,
            upcomingActivities = content.upcoming,
            goals = content.goals,
            unreadNotifications = content.unread,
            todayTrainingSessions = content.todayTrainingSessions,
            completedGoals = content.goals.count(Goal::isCompleted),
            averageGoalProgress = content.goals
                .takeIf { it.isNotEmpty() }
                ?.map(Goal::progressPercent)
                ?.average()
                ?.roundToInt()
                ?: 0,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState(),
    )

    fun logout() {
        authRepository.logout()
    }

    fun refreshTime() {
        currentTime.value = System.currentTimeMillis()
    }
}
