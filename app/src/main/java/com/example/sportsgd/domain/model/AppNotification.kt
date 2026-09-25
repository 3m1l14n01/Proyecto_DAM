package com.example.sportsgd.domain.model

enum class NotificationType {
    ACTIVITY,
    GOAL,
    ROUTINE,
    GENERAL,
}

data class AppNotification(
    val id: Long = 0,
    val title: String,
    val message: String,
    val type: NotificationType,
    val createdAt: Long,
    val isRead: Boolean = false,
    val destination: String? = null,
)
