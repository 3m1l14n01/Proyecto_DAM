package com.example.sportsgd.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.sportsgd.data.local.entity.RoutineEntity
import com.example.sportsgd.data.local.relation.RoutineWithSteps
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    @Transaction
    @Query("SELECT * FROM routines ORDER BY is_completed ASC, created_at DESC")
    fun observeAllWithSteps(): Flow<List<RoutineWithSteps>>

    @Transaction
    @Query(
        """
        SELECT * FROM routines
        WHERE player_id = :playerId
        ORDER BY is_completed ASC, created_at DESC
        """,
    )
    fun observeForPlayerWithSteps(playerId: Long): Flow<List<RoutineWithSteps>>

    @Transaction
    @Query("SELECT * FROM routines WHERE id = :routineId LIMIT 1")
    fun observeByIdWithSteps(routineId: Long): Flow<RoutineWithSteps?>

    @Transaction
    @Query("SELECT * FROM routines WHERE id = :routineId LIMIT 1")
    suspend fun getByIdWithSteps(routineId: Long): RoutineWithSteps?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(routine: RoutineEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSeeds(routines: List<RoutineEntity>): List<Long>

    @Update
    suspend fun update(routine: RoutineEntity)

    @Query(
        """
        UPDATE routines
        SET started_at = :startedAt, completed_at = NULL, is_completed = 0
        WHERE id = :routineId
        """,
    )
    suspend fun markStarted(routineId: Long, startedAt: Long): Int

    @Query(
        """
        UPDATE routines
        SET is_completed = :completed, completed_at = :completedAt
        WHERE id = :routineId
        """,
    )
    suspend fun updateCompletion(routineId: Long, completed: Boolean, completedAt: Long?): Int

    @Query(
        """
        UPDATE routines
        SET started_at = NULL, completed_at = NULL, is_completed = 0
        WHERE id = :routineId
        """,
    )
    suspend fun reset(routineId: Long): Int

    @Query("DELETE FROM routines WHERE id = :routineId")
    suspend fun deleteById(routineId: Long)
}
