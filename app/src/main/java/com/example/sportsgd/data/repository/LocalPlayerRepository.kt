package com.example.sportsgd.data.repository

import com.example.sportsgd.data.local.dao.PlayerDao
import com.example.sportsgd.data.mapper.toDomain
import com.example.sportsgd.data.mapper.toEntity
import com.example.sportsgd.domain.model.Player
import com.example.sportsgd.domain.model.PlayerStatus
import com.example.sportsgd.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class LocalPlayerRepository(
    private val playerDao: PlayerDao,
    private val awaitSeed: suspend () -> Unit = {},
) : PlayerRepository {
    override fun observePlayers(): Flow<List<Player>> = seededFlow(awaitSeed) {
        playerDao.observeAll().map { players -> players.map { it.toDomain() } }
    }

    override fun observePlayer(playerId: Long): Flow<Player?> = seededFlow(awaitSeed) {
        playerDao.observeById(playerId).map { it?.toDomain() }
    }

    override fun observePlayerCount(): Flow<Int> = seededFlow(awaitSeed, playerDao::observeCount)

    override fun observeActivePlayerCount(): Flow<Int> = seededFlow(awaitSeed) {
        playerDao.observeCountByStatus(PlayerStatus.ACTIVE)
    }

    override fun observeInjuredPlayerCount(): Flow<Int> = seededFlow(awaitSeed) {
        combine(
            playerDao.observeCountByStatus(PlayerStatus.INJURED),
            playerDao.observeCountByStatus(PlayerStatus.RECOVERING),
        ) { injured, recovering -> injured + recovering }
    }

    override suspend fun getPlayer(playerId: Long): Player? {
        awaitSeed()
        return playerDao.getById(playerId)?.toDomain()
    }

    override suspend fun savePlayer(player: Player): Long {
        awaitSeed()
        return if (player.id == 0L) {
            playerDao.insert(player.toEntity())
        } else {
            playerDao.update(player.toEntity())
            player.id
        }
    }

    override suspend fun deletePlayer(playerId: Long) {
        awaitSeed()
        playerDao.deleteById(playerId)
    }
}
