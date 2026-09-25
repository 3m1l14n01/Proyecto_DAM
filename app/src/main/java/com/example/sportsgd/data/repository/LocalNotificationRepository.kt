package com.example.sportsgd.data.repository

import com.example.sportsgd.data.local.dao.NotificationDao
import com.example.sportsgd.data.mapper.toDomain
import com.example.sportsgd.data.mapper.toEntity
import com.example.sportsgd.domain.model.AppNotification
import com.example.sportsgd.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalNotificationRepository(
    private val notificationDao: NotificationDao,
    private val awaitSeed: suspend () -> Unit = {},
) : NotificationRepository {
    override fun observeNotifications(): Flow<List<AppNotification>> = seededFlow(awaitSeed) {
        notificationDao.observeAll().map { notifications -> notifications.map { it.toDomain() } }
    }

    override fun observeUnreadCount(): Flow<Int> = seededFlow(awaitSeed, notificationDao::observeUnreadCount)

    override suspend fun saveNotification(notification: AppNotification): Long {
        awaitSeed()
        return notificationDao.insert(notification.toEntity())
    }

    override suspend fun markAsRead(notificationId: Long) {
        awaitSeed()
        notificationDao.markAsRead(notificationId)
    }

    override suspend fun markAllAsRead() {
        awaitSeed()
        notificationDao.markAllAsRead()
    }

    override suspend fun deleteNotification(notificationId: Long) {
        awaitSeed()
        notificationDao.deleteById(notificationId)
    }
}
