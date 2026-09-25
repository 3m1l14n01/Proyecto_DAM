package com.example.sportsgd

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.sportsgd.data.local.DatabaseSeeder
import com.example.sportsgd.data.local.DemoDataIds
import com.example.sportsgd.data.local.SportsGdDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseSeederInstrumentedTest {
    @Test
    fun expiredDemoScheduleIsRefreshedAndTrainingRemainsAssigned() = runBlocking {
        val database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            SportsGdDatabase::class.java,
        ).build()
        try {
            var now = 1_800_000_000_000L
            val seeder = DatabaseSeeder(database) { now }
            seeder.seed()
            val original = requireNotNull(
                database.activityDao().getById(DemoDataIds.TRAINING_ACTIVITY_ID),
            )
            assertEquals(DemoDataIds.ANA_PLAYER_ID, original.playerId)

            now += TEN_DAYS
            seeder.seed()
            val refreshed = requireNotNull(
                database.activityDao().getById(DemoDataIds.TRAINING_ACTIVITY_ID),
            )

            assertTrue(refreshed.startAt > now)
            assertEquals(DemoDataIds.ANA_PLAYER_ID, refreshed.playerId)
        } finally {
            database.close()
        }
    }

    private companion object {
        const val TEN_DAYS = 10L * 24 * 60 * 60 * 1_000
    }
}
