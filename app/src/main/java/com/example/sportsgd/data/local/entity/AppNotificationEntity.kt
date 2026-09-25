package com.example.sportsgd.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.sportsgd.domain.model.NotificationType

@Entity(
    tableName = "notifications",
    indices = [Index("created_at"), Index("is_read")],
)
data class AppNotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val message: String,
    val type: NotificationType,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "is_read")
    val isRead: Boolean,
    val destination: String?,
)
