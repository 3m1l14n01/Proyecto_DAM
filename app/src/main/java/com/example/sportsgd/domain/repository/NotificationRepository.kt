package com.example.sportsgd.domain.repository

import com.example.sportsgd.domain.model.AppNotification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun observeNotifications(): Flow<List<AppNotification>>

    fun observeUnreadCount(): Flow<Int>

    suspend fun saveNotification(notification: AppNotification): Long

    suspend fun markAsRead(notificationId: Long)

    suspend fun markAllAsRead()

    suspend fun deleteNotification(notificationId: Long)
}
