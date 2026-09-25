package com.example.sportsgd.domain.model

import kotlin.math.roundToInt

data class Goal(
    val id: Long = 0,
    val playerId: Long,
    val title: String,
    val description: String = "",
    val currentValue: Int,
    val targetValue: Int,
    val unit: String,
    val deadlineAt: Long? = null,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
) {
    val progressFraction: Float
        get() = when {
            targetValue <= 0 -> 0f
            else -> (currentValue.toFloat() / targetValue).coerceIn(0f, 1f)
        }

    val progressPercent: Int
        get() = (progressFraction * 100).roundToInt()
}
