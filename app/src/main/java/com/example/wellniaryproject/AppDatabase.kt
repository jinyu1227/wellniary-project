package com.example.wellniaryproject

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [UserProfile::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
}
