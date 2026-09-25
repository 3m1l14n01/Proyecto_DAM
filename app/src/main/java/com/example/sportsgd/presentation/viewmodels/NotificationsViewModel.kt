package com.example.sportsgd.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsgd.domain.model.AppNotification
import com.example.sportsgd.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NotificationsUiState(
    val isLoading: Boolean = true,
    val notifications: List<AppNotification> = emptyList(),
    val unreadCount: Int = 0,
    val isMutating: Boolean = false,
    val errorMessage: String? = null,
)

class NotificationsViewModel(
    private val notificationRepository: NotificationRepository,
) : ViewModel() {

    private val isMutating = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<NotificationsUiState> = combine(
        notificationRepository.observeNotifications(),
        isMutating,
        errorMessage,
    ) { notifications, mutating, error ->
        val sorted = notifications.sortedByDescending(AppNotification::createdAt)
        NotificationsUiState(
            isLoading = false,
            notifications = sorted,
            unreadCount = sorted.count { !it.isRead },
            isMutating = mutating,
            errorMessage = error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NotificationsUiState(),
    )

    fun markAsRead(notificationId: Long) {
        if (notificationId <= 0L) return
        mutate { notificationRepository.markAsRead(notificationId) }
    }

    fun markAsRead(notification: AppNotification) {
        if (!notification.isRead) markAsRead(notification.id)
    }

    fun markAllAsRead() {
        mutate(notificationRepository::markAllAsRead)
    }

    fun clearError() {
        errorMessage.value = null
    }

    private fun mutate(operation: suspend () -> Unit) {
        if (isMutating.value) return
        viewModelScope.launch {
            isMutating.value = true
            errorMessage.value = null
            runCatching { operation() }
                .onFailure { errorMessage.value = "No fue posible actualizar las notificaciones." }
            isMutating.value = false
        }
    }
}
