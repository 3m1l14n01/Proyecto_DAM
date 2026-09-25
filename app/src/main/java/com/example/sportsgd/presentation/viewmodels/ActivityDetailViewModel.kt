package com.example.sportsgd.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsgd.domain.model.SportActivity
import com.example.sportsgd.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class ActivityDetailUiState(
    val activity: SportActivity? = null,
    val isLoading: Boolean = true,
    val notFound: Boolean = false,
)

class ActivityDetailViewModel(
    val activityId: Long,
    activityRepository: ActivityRepository,
) : ViewModel() {

    val uiState: StateFlow<ActivityDetailUiState> = if (activityId <= 0L) {
        flowOf(ActivityDetailUiState(isLoading = false, notFound = true))
    } else {
        activityRepository.observeActivity(activityId).map { activity ->
            ActivityDetailUiState(
                activity = activity,
                isLoading = false,
                notFound = activity == null,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ActivityDetailUiState(),
    )
}
