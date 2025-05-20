package com.example.wellniaryproject

import androidx.room.*

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminder_settings")
    suspend fun getAll(): List<ReminderSetting>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(setting: ReminderSetting)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(settings: List<ReminderSetting>)

    @Query("DELETE FROM reminder_settings")
    suspend fun clearAll()
}


