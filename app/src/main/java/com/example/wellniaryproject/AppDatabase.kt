package com.example.wellniaryproject

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DailyRecord::class, DietLogEntity::class, UserProfile::class, ReminderSetting::class],
    version = 6,  // ⚠️ Update version number
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun dailyDao(): DailyDao
    abstract fun dietLogDao(): DietLogDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wellness_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
