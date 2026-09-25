package com.example.sportsgd.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "goals",
    foreignKeys = [
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["player_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("player_id")],
)
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "player_id")
    val playerId: Long,
    val title: String,
    val description: String,
    @ColumnInfo(name = "current_value")
    val currentValue: Int,
    @ColumnInfo(name = "target_value")
    val targetValue: Int,
    val unit: String,
    @ColumnInfo(name = "deadline_at")
    val deadlineAt: Long?,
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
)
