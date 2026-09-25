package com.example.sportsgd.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.sportsgd.domain.model.ActivityType

@Entity(
    tableName = "activities",
    foreignKeys = [
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["player_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("player_id"), Index("start_at")],
)
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val type: ActivityType,
    @ColumnInfo(name = "start_at")
    val startAt: Long,
    @ColumnInfo(name = "end_at")
    val endAt: Long,
    val location: String,
    @ColumnInfo(name = "player_id")
    val playerId: Long?,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
)
