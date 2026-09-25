package com.example.sportsgd.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsgd.domain.model.SportActivity
import com.example.sportsgd.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class CalendarUiState(
    val isLoading: Boolean = true,
    val activities: List<SportActivity> = emptyList(),
    val upcomingActivities: List<SportActivity> = emptyList(),
)

class CalendarViewModel(
    activityRepository: ActivityRepository,
) : ViewModel() {
    private val currentTime = MutableStateFlow(System.currentTimeMillis())

    val uiState: StateFlow<CalendarUiState> = combine(
        activityRepository.observeActivities(), currentTime,
    ) { activities, now ->
            val sorted = activities.sortedBy(SportActivity::startAt)
            CalendarUiState(
                isLoading = false,
                activities = sorted,
                upcomingActivities = sorted.filter { it.endAt >= now },
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CalendarUiState(),
        )

    fun refreshTime() {
        currentTime.value = System.currentTimeMillis()
    }
}
