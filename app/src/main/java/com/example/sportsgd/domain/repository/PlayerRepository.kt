package com.example.sportsgd.domain.repository

import com.example.sportsgd.domain.model.Player
import kotlinx.coroutines.flow.Flow

interface PlayerRepository {
    fun observePlayers(): Flow<List<Player>>

    fun observePlayer(playerId: Long): Flow<Player?>

    fun observePlayerCount(): Flow<Int>

    fun observeActivePlayerCount(): Flow<Int>

    fun observeInjuredPlayerCount(): Flow<Int>

    suspend fun getPlayer(playerId: Long): Player?

    suspend fun savePlayer(player: Player): Long

    suspend fun deletePlayer(playerId: Long)
}
