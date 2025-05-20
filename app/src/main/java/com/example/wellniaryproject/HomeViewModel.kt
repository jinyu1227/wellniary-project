package com.example.wellniaryproject

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).dailyDao()
    private val today = LocalDate.now().toString()
    private var isLoaded = false

    private val _record = MutableStateFlow<DailyRecord?>(null)
    val record: StateFlow<DailyRecord?> = _record

    private val _quote = MutableStateFlow("Loading quote...")
    val quote: StateFlow<String> = _quote

    private val _userGoals = MutableStateFlow<Pair<Int, Float>?>(null)
    val userGoals: StateFlow<Pair<Int, Float>?> = _userGoals

    fun loadDailyQuote() {
        viewModelScope.launch {
            try {
                val response = QuoteApi.service.getRandomQuote()
                _quote.value = "\"${response[0].q}\" — ${response[0].a}"
            } catch (e: Exception) {
                Log.e("QuoteError", "Failed to fetch quote: ${e.message}")
                _quote.value = "Stay strong and healthy!"
            }
        }
    }

    fun saveRecord(water: Int, weight: Float) {
        viewModelScope.launch {
            val record = DailyRecord(date = today, waterCount = water, weight = weight)
            dao.insert(record)
            _record.value = record

            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            val firebaseRef = Firebase.database.reference
            firebaseRef.child("records")
                .child(uid)
                .child(today)
                .setValue(record)
        }
    }

    fun loadRecordOnce() {
        if (isLoaded) return
        isLoaded = true

        viewModelScope.launch {
            val local = dao.getByDate(today)
            local?.let {
                _record.value = it
                Log.d("RoomTest", "Loaded local record: $it")
            }

            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            val firebaseRef = Firebase.database.reference.child("records").child(uid).child(today)

            firebaseRef.get().addOnSuccessListener { snapshot ->
                val rec = snapshot.getValue<DailyRecord>()
                rec?.let {
                    _record.value = it
                    Log.d("FirebaseTest", "Loaded remote record: $it")

                    viewModelScope.launch {
                        dao.insert(it)
                    }
                }
            }.addOnFailureListener {
                Log.e("FirebaseLoad", "Failed to load from Firebase: ${it.message}")
            }
        }
    }

    fun loadUserGoals() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val goalRef = Firebase.database.reference.child("userGoals").child(uid)

        goalRef.get().addOnSuccessListener { snapshot ->
            val water = snapshot.child("waterGoal").getValue(Int::class.java) ?: 8
            val weight = snapshot.child("weightGoal").getValue(Float::class.java) ?: 60f
            _userGoals.value = Pair(water, weight)
        }.addOnFailureListener {
            Log.e("GoalLoad", "Failed to load goals: ${it.message}")
        }
    }

    fun updateGoals(newWaterGoal: Int, newWeightGoal: Float) {
        _userGoals.value = Pair(newWaterGoal, newWeightGoal)

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val goalRef = Firebase.database.reference.child("userGoals").child(uid)
        val goalData = mapOf(
            "waterGoal" to newWaterGoal,
            "weightGoal" to newWeightGoal
        )
        goalRef.setValue(goalData).addOnFailureListener {
            Log.e("GoalUpdate", "Failed to update goals: ${it.message}")
        }
    }
}