package com.example.sportsgd.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsgd.domain.model.Player
import com.example.sportsgd.domain.model.SportActivity
import com.example.sportsgd.domain.repository.ActivityRepository
import com.example.sportsgd.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

data class PlayerDetailUiState(
    val player: Player? = null,
    val nextActivity: SportActivity? = null,
    val isLoading: Boolean = true,
    val notFound: Boolean = false,
)

class PlayerDetailViewModel(
    val playerId: Long,
    playerRepository: PlayerRepository,
    activityRepository: ActivityRepository,
) : ViewModel() {

    val uiState: StateFlow<PlayerDetailUiState> = if (playerId <= 0L) {
        flowOf(PlayerDetailUiState(isLoading = false, notFound = true))
    } else {
        combine(
            playerRepository.observePlayer(playerId),
            activityRepository.observeUpcomingActivities(),
        ) { player, activities ->
            PlayerDetailUiState(
                player = player,
                nextActivity = player?.let { selectedPlayer ->
                    activities.filter { it.playerId == selectedPlayer.id }
                        .minByOrNull(SportActivity::startAt)
                },
                isLoading = false,
                notFound = player == null,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PlayerDetailUiState(),
    )
}
