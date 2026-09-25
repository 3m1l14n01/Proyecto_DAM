package com.example.sportsgd.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsgd.domain.model.Player
import com.example.sportsgd.domain.model.PlayerStatus
import com.example.sportsgd.domain.model.UserRole
import com.example.sportsgd.domain.repository.AuthRepository
import com.example.sportsgd.domain.repository.PlayerRepository
import java.util.Calendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PlayerFormInput(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String = "",
    val ageOrBirthDate: String,
    val sport: String,
    val position: String,
    val team: String,
    val semester: String,
    val average: String,
    val status: PlayerStatus = PlayerStatus.ACTIVE,
)

data class PlayerFormErrors(
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val ageOrBirthDate: String? = null,
    val sport: String? = null,
    val position: String? = null,
    val team: String? = null,
    val semester: String? = null,
    val average: String? = null,
) {
    val hasErrors: Boolean
        get() = listOf(
            firstName,
            lastName,
            email,
            ageOrBirthDate,
            sport,
            position,
            team,
            semester,
            average,
        ).any { it != null }
}

sealed interface PlayerSaveResult {
    data object Saving : PlayerSaveResult
    data class Success(val playerId: Long) : PlayerSaveResult
    data class Invalid(val errors: PlayerFormErrors) : PlayerSaveResult
    data class Failure(val message: String) : PlayerSaveResult
}

class PlayersViewModel(
    private val playerRepository: PlayerRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    val players: StateFlow<List<Player>> = playerRepository.observePlayers().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    private val mutableSaveResult = MutableStateFlow<PlayerSaveResult?>(null)
    val saveResult: StateFlow<PlayerSaveResult?> = mutableSaveResult.asStateFlow()

    fun addPlayer(player: Player) {
        addPlayer(
            PlayerFormInput(
                firstName = player.firstName,
                lastName = player.lastName,
                email = player.email,
                phone = player.phone,
                ageOrBirthDate = player.birthDate,
                sport = player.sport,
                position = player.position,
                team = player.team,
                semester = player.semester.toString(),
                average = player.average.toString(),
                status = player.status,
            ),
        )
    }

    fun addPlayer(input: PlayerFormInput) {
        if (authRepository.session.value?.role != UserRole.COACH) {
            mutableSaveResult.value = PlayerSaveResult.Failure("Solo un entrenador puede registrar jugadores.")
            return
        }
        val errors = validate(input)
        if (errors.hasErrors) {
            mutableSaveResult.value = PlayerSaveResult.Invalid(errors)
            return
        }

        val semester = input.semester.trim().toInt()
        val average = input.average.trim().replace(',', '.').toDouble()
        val player = Player(
            firstName = input.firstName.trim(),
            lastName = input.lastName.trim(),
            email = input.email.trim().lowercase(),
            phone = input.phone.trim(),
            birthDate = input.ageOrBirthDate.trim(),
            sport = input.sport.trim(),
            position = input.position.trim(),
            team = input.team.trim(),
            semester = semester,
            average = average,
            status = input.status,
        )

        viewModelScope.launch {
            mutableSaveResult.value = PlayerSaveResult.Saving
            mutableSaveResult.value = runCatching {
                PlayerSaveResult.Success(playerRepository.savePlayer(player))
            }.getOrElse {
                PlayerSaveResult.Failure("No fue posible guardar al jugador.")
            }
        }
    }

    fun clearSaveResult() {
        mutableSaveResult.value = null
    }

    private fun validate(input: PlayerFormInput): PlayerFormErrors {
        val semester = input.semester.trim().toIntOrNull()
        val average = input.average.trim().replace(',', '.').toDoubleOrNull()
        return PlayerFormErrors(
            firstName = requiredNameError(input.firstName, "nombre"),
            lastName = requiredNameError(input.lastName, "apellido"),
            email = when {
                input.email.isBlank() -> "Ingresa el correo."
                !EMAIL_REGEX.matches(input.email.trim()) -> "Ingresa un correo válido."
                else -> null
            },
            ageOrBirthDate = when {
                input.ageOrBirthDate.isBlank() -> "Ingresa la edad o fecha de nacimiento."
                !isValidAgeOrBirthDate(input.ageOrBirthDate) ->
                    "Usa una edad válida o una fecha AAAA-MM-DD."
                else -> null
            },
            sport = input.sport.requiredError("Selecciona o ingresa el deporte."),
            position = input.position.requiredError("Ingresa la posición."),
            team = input.team.requiredError("Ingresa el equipo."),
            semester = when {
                semester == null -> "Ingresa un semestre numérico."
                semester !in 1..12 -> "El semestre debe estar entre 1 y 12."
                else -> null
            },
            average = when {
                average == null -> "Ingresa un promedio numérico."
                average !in 0.0..10.0 -> "El promedio debe estar entre 0 y 10."
                else -> null
            },
        )
    }

    private fun requiredNameError(value: String, label: String): String? = when {
        value.isBlank() -> "Ingresa el $label."
        value.trim().length < 2 -> "El $label debe tener al menos 2 caracteres."
        else -> null
    }

    private fun isValidAgeOrBirthDate(rawValue: String): Boolean {
        val value = rawValue.trim()
        AGE_REGEX.matchEntire(value)?.groupValues?.get(1)?.toIntOrNull()?.let { age ->
            return age in 12..100
        }

        val match = DATE_REGEX.matchEntire(value) ?: return false
        val (year, month, day) = match.destructured
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        if (year.toInt() !in (currentYear - 100)..(currentYear - 12)) return false
        return runCatching {
            Calendar.getInstance().apply {
                isLenient = false
                clear()
                set(year.toInt(), month.toInt() - 1, day.toInt())
                timeInMillis
            }
        }.isSuccess
    }

    private fun String.requiredError(message: String): String? = message.takeIf { isBlank() }

    private companion object {
        val EMAIL_REGEX = Regex(
            pattern = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            option = RegexOption.IGNORE_CASE,
        )
        val AGE_REGEX = Regex("^(\\d{1,3})(?:\\s*años?)?$", RegexOption.IGNORE_CASE)
        val DATE_REGEX = Regex("^(\\d{4})[-/](\\d{1,2})[-/](\\d{1,2})$")
    }
}
