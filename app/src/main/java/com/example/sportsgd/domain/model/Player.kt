package com.example.sportsgd.domain.model

enum class PlayerStatus {
    ACTIVE,
    INJURED,
    RECOVERING,
}

data class Player(
    val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String = "",
    val birthDate: String = "",
    val sport: String,
    val position: String,
    val team: String,
    val semester: Int,
    val average: Double,
    val status: PlayerStatus = PlayerStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis(),
) {
    val fullName: String
        get() = listOf(firstName, lastName)
            .filter { it.isNotBlank() }
            .joinToString(" ")

    val name: String
        get() = fullName
}
