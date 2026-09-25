package com.example.sportsgd.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.sportsgd.data.local.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY is_completed ASC, created_at DESC")
    fun observeAll(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE player_id = :playerId ORDER BY is_completed ASC, created_at DESC")
    fun observeForPlayer(playerId: Long): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :goalId LIMIT 1")
    fun observeById(goalId: Long): Flow<GoalEntity?>

    @Query("SELECT * FROM goals WHERE id = :goalId LIMIT 1")
    suspend fun getById(goalId: Long): GoalEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(goal: GoalEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSeeds(goals: List<GoalEntity>): List<Long>

    @Update
    suspend fun update(goal: GoalEntity)

    @Query(
        """
        UPDATE goals
        SET current_value = :currentValue, is_completed = :isCompleted
        WHERE id = :goalId
        """,
    )
    suspend fun updateProgress(goalId: Long, currentValue: Int, isCompleted: Boolean)

    @Query("DELETE FROM goals WHERE id = :goalId")
    suspend fun deleteById(goalId: Long)
}
