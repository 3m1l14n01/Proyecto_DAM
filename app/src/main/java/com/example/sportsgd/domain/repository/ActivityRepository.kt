package com.example.sportsgd.domain.repository

import com.example.sportsgd.domain.model.SportActivity
import kotlinx.coroutines.flow.Flow

interface ActivityRepository {
    fun observeActivities(): Flow<List<SportActivity>>

    fun observeUpcomingActivities(fromMillis: Long = System.currentTimeMillis()): Flow<List<SportActivity>>

    fun observeActivity(activityId: Long): Flow<SportActivity?>

    fun observeUpcomingCount(fromMillis: Long = System.currentTimeMillis()): Flow<Int>

    suspend fun getActivity(activityId: Long): SportActivity?

    suspend fun saveActivity(activity: SportActivity): Long

    suspend fun deleteActivity(activityId: Long)
}
