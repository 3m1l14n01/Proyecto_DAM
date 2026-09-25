package com.example.sportsgd.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "routine_steps",
    foreignKeys = [
        ForeignKey(
            entity = RoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routine_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("routine_id"),
        Index(value = ["routine_id", "order_index"], unique = true),
    ],
)
data class RoutineStepEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "routine_id")
    val routineId: Long,
    @ColumnInfo(name = "order_index")
    val orderIndex: Int,
    val title: String,
    val description: String,
    @ColumnInfo(name = "duration_seconds")
    val durationSeconds: Int,
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean,
    @ColumnInfo(name = "completed_at")
    val completedAt: Long?,
)
