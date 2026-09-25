package com.example.sportsgd.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sportsgd.data.local.entity.RoutineStepEntity

@Dao
interface RoutineStepDao {
    @Query("SELECT * FROM routine_steps WHERE routine_id = :routineId ORDER BY order_index ASC")
    suspend fun getForRoutine(routineId: Long): List<RoutineStepEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(steps: List<RoutineStepEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSeeds(steps: List<RoutineStepEntity>): List<Long>

    @Query(
        """
        UPDATE routine_steps
        SET is_completed = :completed, completed_at = :completedAt
        WHERE id = :stepId AND routine_id = :routineId
        """,
    )
    suspend fun updateCompletion(
        routineId: Long,
        stepId: Long,
        completed: Boolean,
        completedAt: Long?,
    ): Int

    @Query(
        """
        UPDATE routine_steps
        SET is_completed = 1, completed_at = :completedAt
        WHERE routine_id = :routineId
        """,
    )
    suspend fun completeAll(routineId: Long, completedAt: Long): Int

    @Query(
        """
        UPDATE routine_steps
        SET is_completed = 0, completed_at = NULL
        WHERE routine_id = :routineId
        """,
    )
    suspend fun resetForRoutine(routineId: Long): Int

    @Query("DELETE FROM routine_steps WHERE routine_id = :routineId")
    suspend fun deleteForRoutine(routineId: Long)
}
