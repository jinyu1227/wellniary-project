package com.example.wellniaryproject

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DailyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: DailyRecord)

    @Query("SELECT * FROM daily_record WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): DailyRecord?
}
