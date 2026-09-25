package com.example.sportsgd.domain.model

enum class ActivityType {
    TRAINING,
    MATCH,
    ACADEMIC,
    OTHER,
}

data class SportActivity(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val type: ActivityType,
    val startAt: Long,
    val endAt: Long,
    val location: String = "",
    val playerId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
)
