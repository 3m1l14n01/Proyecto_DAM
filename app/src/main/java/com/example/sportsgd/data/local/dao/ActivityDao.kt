package com.example.sportsgd.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.sportsgd.data.local.entity.ActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities ORDER BY start_at ASC")
    fun observeAll(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE start_at >= :fromMillis ORDER BY start_at ASC")
    fun observeUpcoming(fromMillis: Long): Flow<List<ActivityEntity>>

    @Query("SELECT COUNT(*) FROM activities WHERE start_at >= :fromMillis")
    fun observeUpcomingCount(fromMillis: Long): Flow<Int>

    @Query("SELECT * FROM activities WHERE id = :activityId LIMIT 1")
    fun observeById(activityId: Long): Flow<ActivityEntity?>

    @Query("SELECT * FROM activities WHERE id = :activityId LIMIT 1")
    suspend fun getById(activityId: Long): ActivityEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(activity: ActivityEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSeeds(activities: List<ActivityEntity>): List<Long>

    @Update
    suspend fun update(activity: ActivityEntity)

    @Query("DELETE FROM activities WHERE id = :activityId")
    suspend fun deleteById(activityId: Long)
}
