package com.example.wellniaryproject

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DietLogDao {

    @Insert
    suspend fun insertLog(log: DietLogEntity)

    @Query("SELECT * FROM diet_logs ORDER BY date DESC")
    suspend fun getAllLogs(): List<DietLogEntity>

    @Query("SELECT * FROM diet_logs WHERE uid = :userId ORDER BY date DESC")
    fun getLogsByUid(userId: String): Flow<List<DietLogEntity>>

    @Query("SELECT * FROM diet_logs WHERE uid = :userId AND isSynced = 0")
    suspend fun getUnsyncedLogsByUid(userId: String): List<DietLogEntity>

    @Query("UPDATE diet_logs SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markLogsAsSynced(ids: List<Int>)

    @Query("DELETE FROM diet_logs WHERE id = :logId")
    suspend fun deleteLogById(logId: Int)

    @Query("DELETE FROM diet_logs WHERE uid = :userId")
    suspend fun deleteLogsByUid(userId: String)

    @Query("""
        UPDATE diet_logs SET 
            date = :date,
            mealType = :mealType,
            staple = :staple,
            meat = :meat,
            vegetable = :vegetable,
            other = :other
        WHERE id = :id
    """)
    suspend fun updateLogById(
        id: Int,
        date: String,
        mealType: String,
        staple: String,
        meat: String,
        vegetable: String,
        other: String
    )
}
