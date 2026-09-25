package com.example.sportsgd.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.sportsgd.domain.model.PlayerStatus

@Entity(
    tableName = "players",
    indices = [Index(value = ["email"], unique = true)],
)
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "first_name")
    val firstName: String,
    @ColumnInfo(name = "last_name")
    val lastName: String,
    val email: String,
    val phone: String,
    @ColumnInfo(name = "birth_date")
    val birthDate: String,
    val sport: String,
    val position: String,
    val team: String,
    val semester: Int,
    val average: Double,
    val status: PlayerStatus,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
)
