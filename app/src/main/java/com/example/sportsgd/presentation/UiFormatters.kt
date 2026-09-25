package com.example.sportsgd.presentation

import com.example.sportsgd.domain.model.Player
import com.example.sportsgd.domain.model.PlayerStatus
import com.example.sportsgd.domain.model.SportActivity
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Player.initials(): String = listOf(firstName, lastName)
    .filter { it.isNotBlank() }
    .take(2)
    .joinToString("") { it.first().uppercaseChar().toString() }
    .ifBlank { "JG" }

fun Player.summary(): String = "$sport · $semester.º semestre"

fun PlayerStatus.displayName(): String = when (this) {
    PlayerStatus.ACTIVE -> "Activo"
    PlayerStatus.INJURED -> "Lesionado"
    PlayerStatus.RECOVERING -> "En recuperación"
}

fun SportActivity.scheduleText(): String {
    val date = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.forLanguageTag("es-MX")).format(Date(startAt))
    val time = SimpleDateFormat("HH:mm", Locale.forLanguageTag("es-MX")).format(Date(startAt))
    return listOf(date, time, location).filter { it.isNotBlank() }.joinToString(" · ")
}

fun SportActivity.statusText(): String = when (type.name) {
    "ACADEMIC" -> "Pendiente de entrega"
    "MATCH" -> "Evento programado"
    else -> "Actividad programada"
}
