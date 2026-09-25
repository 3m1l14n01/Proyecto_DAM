package com.example.sportsgd.data.repository

import com.example.sportsgd.data.local.dao.ActivityDao
import com.example.sportsgd.data.mapper.toDomain
import com.example.sportsgd.data.mapper.toEntity
import com.example.sportsgd.domain.model.SportActivity
import com.example.sportsgd.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalActivityRepository(
    private val activityDao: ActivityDao,
    private val awaitSeed: suspend () -> Unit = {},
) : ActivityRepository {
    override fun observeActivities(): Flow<List<SportActivity>> = seededFlow(awaitSeed) {
        activityDao.observeAll().map { activities -> activities.map { it.toDomain() } }
    }

    override fun observeUpcomingActivities(fromMillis: Long): Flow<List<SportActivity>> =
        seededFlow(awaitSeed) {
            activityDao.observeUpcoming(fromMillis).map { activities -> activities.map { it.toDomain() } }
        }

    override fun observeActivity(activityId: Long): Flow<SportActivity?> = seededFlow(awaitSeed) {
        activityDao.observeById(activityId).map { it?.toDomain() }
    }

    override fun observeUpcomingCount(fromMillis: Long): Flow<Int> = seededFlow(awaitSeed) {
        activityDao.observeUpcomingCount(fromMillis)
    }

    override suspend fun getActivity(activityId: Long): SportActivity? {
        awaitSeed()
        return activityDao.getById(activityId)?.toDomain()
    }

    override suspend fun saveActivity(activity: SportActivity): Long {
        awaitSeed()
        return if (activity.id == 0L) {
            activityDao.insert(activity.toEntity())
        } else {
            activityDao.update(activity.toEntity())
            activity.id
        }
    }

    override suspend fun deleteActivity(activityId: Long) {
        awaitSeed()
        activityDao.deleteById(activityId)
    }
}
