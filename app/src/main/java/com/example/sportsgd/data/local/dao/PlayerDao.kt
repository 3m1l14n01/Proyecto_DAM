package com.example.sportsgd.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.sportsgd.data.local.entity.PlayerEntity
import com.example.sportsgd.domain.model.PlayerStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Query("SELECT * FROM players ORDER BY first_name COLLATE NOCASE, last_name COLLATE NOCASE")
    fun observeAll(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players WHERE id = :playerId LIMIT 1")
    fun observeById(playerId: Long): Flow<PlayerEntity?>

    @Query("SELECT * FROM players WHERE id = :playerId LIMIT 1")
    suspend fun getById(playerId: Long): PlayerEntity?

    @Query("SELECT COUNT(*) FROM players")
    fun observeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM players WHERE status = :status")
    fun observeCountByStatus(status: PlayerStatus): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(player: PlayerEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSeeds(players: List<PlayerEntity>): List<Long>

    @Update
    suspend fun update(player: PlayerEntity)

    @Query("DELETE FROM players WHERE id = :playerId")
    suspend fun deleteById(playerId: Long)
}
