package com.example.sportsgd.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.sportsgd.data.local.dao.ActivityDao
import com.example.sportsgd.data.local.dao.GoalDao
import com.example.sportsgd.data.local.dao.NotificationDao
import com.example.sportsgd.data.local.dao.PlayerDao
import com.example.sportsgd.data.local.dao.RoutineDao
import com.example.sportsgd.data.local.dao.RoutineStepDao
import com.example.sportsgd.data.local.entity.ActivityEntity
import com.example.sportsgd.data.local.entity.AppNotificationEntity
import com.example.sportsgd.data.local.entity.GoalEntity
import com.example.sportsgd.data.local.entity.PlayerEntity
import com.example.sportsgd.data.local.entity.RoutineEntity
import com.example.sportsgd.data.local.entity.RoutineStepEntity

@Database(
    entities = [
        PlayerEntity::class,
        ActivityEntity::class,
        GoalEntity::class,
        RoutineEntity::class,
        RoutineStepEntity::class,
        AppNotificationEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(RoomConverters::class)
abstract class SportsGdDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao

    abstract fun activityDao(): ActivityDao

    abstract fun goalDao(): GoalDao

    abstract fun routineDao(): RoutineDao

    abstract fun routineStepDao(): RoutineStepDao

    abstract fun notificationDao(): NotificationDao

    companion object {
        const val DATABASE_NAME = "sportsgd.db"

        fun create(context: Context): SportsGdDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                SportsGdDatabase::class.java,
                DATABASE_NAME,
            ).build()
    }
}
