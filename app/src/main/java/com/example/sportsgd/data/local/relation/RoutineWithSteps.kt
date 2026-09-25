package com.example.sportsgd.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.sportsgd.data.local.entity.RoutineEntity
import com.example.sportsgd.data.local.entity.RoutineStepEntity

data class RoutineWithSteps(
    @Embedded
    val routine: RoutineEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "routine_id",
    )
    val steps: List<RoutineStepEntity>,
)
