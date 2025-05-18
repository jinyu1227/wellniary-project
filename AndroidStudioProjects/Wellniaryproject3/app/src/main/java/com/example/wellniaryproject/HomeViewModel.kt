package com.example.wellniaryproject

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wellniaryproject.QuoteApi
import com.example.wellniaryproject.AppDatabase
import com.example.wellniaryproject.DailyRecord
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
    private var isLoaded = false // ✅ 防止重复加载

    private val _record = MutableStateFlow<DailyRecord?>(null)
    val record: StateFlow<DailyRecord?> = _record

    // ✅ 每日健康提示（本地）
//    private val tips = listOf(
//        "Drink a cup of water when you wake up!",
//        "Take a short walk after meals.",
//        "Avoid sugary drinks when possible.",
//        "Get at least 7 hours of sleep.",
//        "Stretch for 5 minutes every hour.",
//        "Add vegetables to every meal.",
//        "Take deep breaths to relax your mind."
//    )
//    val dailyTip: String = tips[LocalDate.now().dayOfYear % tips.size]

    // ✅ 每日一句（ZenQuotes）
    private val _quote = MutableStateFlow("Loading quote...")
    val quote: StateFlow<String> = _quote

    fun loadDailyQuote() {
        viewModelScope.launch {
            try {
                val response = QuoteApi.service.getRandomQuote()
                _quote.value = "\"${response[0].q}\" — ${response[0].a}"
            } catch (e: Exception) {
                Log.e("QuoteError", "Failed to fetch quote: ${e.message}")
                _quote.value = "Stay strong and healthy!" // fallback 文案
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
            // ✅ 先从 Room 本地加载
            val local = dao.getByDate(today)
            local?.let {
                _record.value = it
                Log.d("RoomTest", "Loaded local record: $it")
            }

            // ✅ 如果用户已登录，从 Firebase 拉取最新数据
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
}
