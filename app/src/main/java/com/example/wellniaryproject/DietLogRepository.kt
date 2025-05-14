package com.example.wellniaryproject

import kotlinx.coroutines.flow.Flow

class DietLogRepository(private val dao: DietLogDao) {

    suspend fun insert(log: DietLogEntity) {
        dao.insertLog(log)
    }

    suspend fun deleteById(id: Int) {
        dao.deleteLogById(id)
    }

    suspend fun deleteByUser(uid: String) {
        dao.deleteLogsByUid(uid)
    }

    suspend fun updateLog(
        id: Int,
        date: String,
        mealType: String,
        staple: String,
        meat: String,
        vegetable: String,
        other: String
    ) {
        dao.updateLogById(id, date, mealType, staple, meat, vegetable, other)
    }

    fun getLogsByUser(uid: String): Flow<List<DietLogEntity>> {
        return dao.getLogsByUid(uid)
    }

    suspend fun getAllLogs(): List<DietLogEntity> {
        return dao.getAllLogs()
    }
}

