package com.example.wellniaryproject

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class DietLogViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).dietLogDao()
    private val repository = DietLogRepository(dao)

    fun insertLog(log: DietLogEntity) = viewModelScope.launch {
        repository.insert(log)
    }

    fun deleteLog(id: Int) = viewModelScope.launch {
        repository.deleteById(id)
    }

    fun deleteAllForUser(uid: String) = viewModelScope.launch {
        repository.deleteByUser(uid)
    }

    fun updateLog(
        id: Int,
        date: String,
        mealType: String,
        staple: String,
        meat: String,
        vegetable: String,
        other: String
    ) = viewModelScope.launch {
        repository.updateLog(id, date, mealType, staple, meat, vegetable, other)
    }

    fun getLogsForUser(uid: String): Flow<List<DietLogEntity>> {
        return repository.getLogsByUser(uid)
    }
}
