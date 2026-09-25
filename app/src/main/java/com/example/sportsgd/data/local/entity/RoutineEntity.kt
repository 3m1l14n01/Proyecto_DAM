package com.example.sportsgd.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "routines",
    foreignKeys = [
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["player_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goal_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("player_id"), Index("goal_id")],
)
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "player_id")
    val playerId: Long,
    @ColumnInfo(name = "goal_id")
    val goalId: Long?,
    val title: String,
    val description: String,
    @ColumnInfo(name = "estimated_duration_minutes")
    val estimatedDurationMinutes: Int,
    @ColumnInfo(name = "started_at")
    val startedAt: Long?,
    @ColumnInfo(name = "completed_at")
    val completedAt: Long?,
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
)
