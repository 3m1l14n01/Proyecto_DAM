package com.example.sportsgd.data.mapper

import com.example.sportsgd.data.local.entity.ActivityEntity
import com.example.sportsgd.data.local.entity.AppNotificationEntity
import com.example.sportsgd.data.local.entity.GoalEntity
import com.example.sportsgd.data.local.entity.PlayerEntity
import com.example.sportsgd.data.local.entity.RoutineEntity
import com.example.sportsgd.data.local.entity.RoutineStepEntity
import com.example.sportsgd.data.local.relation.RoutineWithSteps
import com.example.sportsgd.domain.model.AppNotification
import com.example.sportsgd.domain.model.Goal
import com.example.sportsgd.domain.model.Player
import com.example.sportsgd.domain.model.Routine
import com.example.sportsgd.domain.model.RoutineStep
import com.example.sportsgd.domain.model.SportActivity

fun PlayerEntity.toDomain() = Player(
    id = id,
    firstName = firstName,
    lastName = lastName,
    email = email,
    phone = phone,
    birthDate = birthDate,
    sport = sport,
    position = position,
    team = team,
    semester = semester,
    average = average,
    status = status,
    createdAt = createdAt,
)

fun Player.toEntity() = PlayerEntity(
    id = id,
    firstName = firstName.trim(),
    lastName = lastName.trim(),
    email = email.trim().lowercase(),
    phone = phone.trim(),
    birthDate = birthDate.trim(),
    sport = sport.trim(),
    position = position.trim(),
    team = team.trim(),
    semester = semester,
    average = average,
    status = status,
    createdAt = createdAt,
)

fun ActivityEntity.toDomain() = SportActivity(
    id = id,
    title = title,
    description = description,
    type = type,
    startAt = startAt,
    endAt = endAt,
    location = location,
    playerId = playerId,
    createdAt = createdAt,
)

fun SportActivity.toEntity() = ActivityEntity(
    id = id,
    title = title.trim(),
    description = description.trim(),
    type = type,
    startAt = startAt,
    endAt = endAt,
    location = location.trim(),
    playerId = playerId,
    createdAt = createdAt,
)

fun GoalEntity.toDomain() = Goal(
    id = id,
    playerId = playerId,
    title = title,
    description = description,
    currentValue = currentValue,
    targetValue = targetValue,
    unit = unit,
    deadlineAt = deadlineAt,
    isCompleted = isCompleted,
    createdAt = createdAt,
)

fun Goal.toEntity() = GoalEntity(
    id = id,
    playerId = playerId,
    title = title.trim(),
    description = description.trim(),
    currentValue = currentValue,
    targetValue = targetValue,
    unit = unit.trim(),
    deadlineAt = deadlineAt,
    isCompleted = isCompleted,
    createdAt = createdAt,
)

fun RoutineStepEntity.toDomain() = RoutineStep(
    id = id,
    routineId = routineId,
    orderIndex = orderIndex,
    title = title,
    description = description,
    durationSeconds = durationSeconds,
    isCompleted = isCompleted,
    completedAt = completedAt,
)

fun RoutineStep.toEntity(
    parentRoutineId: Long = routineId,
    persistedId: Long = id,
) = RoutineStepEntity(
    id = persistedId,
    routineId = parentRoutineId,
    orderIndex = orderIndex,
    title = title.trim(),
    description = description.trim(),
    durationSeconds = durationSeconds,
    isCompleted = isCompleted,
    completedAt = completedAt,
)

fun RoutineWithSteps.toDomain() = Routine(
    id = routine.id,
    playerId = routine.playerId,
    goalId = routine.goalId,
    title = routine.title,
    description = routine.description,
    estimatedDurationMinutes = routine.estimatedDurationMinutes,
    startedAt = routine.startedAt,
    completedAt = routine.completedAt,
    isCompleted = routine.isCompleted,
    createdAt = routine.createdAt,
    steps = steps.sortedBy(RoutineStepEntity::orderIndex).map(RoutineStepEntity::toDomain),
)

fun Routine.toEntity() = RoutineEntity(
    id = id,
    playerId = playerId,
    goalId = goalId,
    title = title.trim(),
    description = description.trim(),
    estimatedDurationMinutes = estimatedDurationMinutes,
    startedAt = startedAt,
    completedAt = completedAt,
    isCompleted = isCompleted,
    createdAt = createdAt,
)

fun AppNotificationEntity.toDomain() = AppNotification(
    id = id,
    title = title,
    message = message,
    type = type,
    createdAt = createdAt,
    isRead = isRead,
    destination = destination,
)

fun AppNotification.toEntity() = AppNotificationEntity(
    id = id,
    title = title.trim(),
    message = message.trim(),
    type = type,
    createdAt = createdAt,
    isRead = isRead,
    destination = destination,
)
