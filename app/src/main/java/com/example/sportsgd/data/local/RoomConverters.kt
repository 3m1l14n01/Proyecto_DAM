package com.example.sportsgd.data.local

import androidx.room.TypeConverter
import com.example.sportsgd.domain.model.ActivityType
import com.example.sportsgd.domain.model.NotificationType
import com.example.sportsgd.domain.model.PlayerStatus

class RoomConverters {
    @TypeConverter
    fun playerStatusToString(value: PlayerStatus): String = value.name

    @TypeConverter
    fun stringToPlayerStatus(value: String): PlayerStatus =
        enumValues<PlayerStatus>().firstOrNull { it.name == value } ?: PlayerStatus.ACTIVE

    @TypeConverter
    fun activityTypeToString(value: ActivityType): String = value.name

    @TypeConverter
    fun stringToActivityType(value: String): ActivityType =
        enumValues<ActivityType>().firstOrNull { it.name == value } ?: ActivityType.OTHER

    @TypeConverter
    fun notificationTypeToString(value: NotificationType): String = value.name

    @TypeConverter
    fun stringToNotificationType(value: String): NotificationType =
        enumValues<NotificationType>().firstOrNull { it.name == value } ?: NotificationType.GENERAL
}
