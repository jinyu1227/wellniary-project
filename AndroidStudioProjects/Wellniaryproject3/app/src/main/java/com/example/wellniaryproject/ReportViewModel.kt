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
import com.google.firebase.firestore.ktx.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
class ReportViewModel(application: Application) : AndroidViewModel(application) {

    private val _weeklyData = MutableStateFlow<List<DailyRecord>>(emptyList())
    val weeklyData: StateFlow<List<DailyRecord>> = _weeklyData

    data class MealCategoryCount(
        val staple: Int = 0,
        val meat: Int = 0,
        val vegetable: Int = 0,
        val other: Int = 0
    )

    private val _dietCategoryRatio = MutableStateFlow(MealCategoryCount())
    val dietCategoryRatio: StateFlow<MealCategoryCount> = _dietCategoryRatio

    init {
        val currentWeekStart = LocalDate.now().with(java.time.DayOfWeek.MONDAY)
        fetchWeekData(currentWeekStart)
        fetchDietDataForPieChart(currentWeekStart.plusDays(6)) // ✅ 传递当前周的周日
    }

    fun fetchWeekData(weekStart: LocalDate) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = Firebase.database.reference.child("records").child(uid)

        viewModelScope.launch {
            try {
                val snapshot = ref.get().await()
                val dates = (0..6).map { weekStart.plusDays(it.toLong()) }

                val result = dates.map { date ->
                    val dateStr = date.toString()
                    val dailySnapshot = snapshot.child(dateStr)
                    val record = dailySnapshot.getValue(DailyRecord::class.java)
                    Log.i("🔥 ReportViewModel", "Loaded: $dateStr => $record")
                    record ?: DailyRecord(date = dateStr, waterCount = 0, weight = 0f)
                }

                Log.i("🔥 ReportViewModel", "Weekly result: $result")
                _weeklyData.value = result
            } catch (e: Exception) {
                Log.e("🔥 ReportViewModel", "❌ Firebase fetch failed: ${e.message}")
            }
        }
    }

//    private fun fetchDietDataForPieChart() {
//        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
//        val db = Firebase.firestore
//        val today = LocalDate.now()
//        val dateThreshold = today.minusDays(6)
//
//        db.collection("dietLogs")
//            .whereEqualTo("uid", uid)
//            .get()
//            .addOnSuccessListener { result ->
//                var stapleCount = 0
//                var meatCount = 0
//                var vegCount = 0
//                var otherCount = 0
//
//                for (doc in result) {
//                    val dateStr = doc.getString("date") ?: continue
//                    val date = LocalDate.parse(dateStr)
//                    if (date.isBefore(dateThreshold)) continue
//
//                    if (!doc.getString("staple").isNullOrBlank()) stapleCount++
//                    if (!doc.getString("meat").isNullOrBlank()) meatCount++
//                    if (!doc.getString("vegetable").isNullOrBlank()) vegCount++
//                    if (!doc.getString("other").isNullOrBlank()) otherCount++
//                }
//
//                _dietCategoryRatio.value = MealCategoryCount(
//                    staple = stapleCount,
//                    meat = meatCount,
//                    vegetable = vegCount,
//                    other = otherCount
//                )
//            }
//    }

    fun fetchDietDataForPieChart(baseDate: LocalDate) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = Firebase.firestore
        val dateThreshold = baseDate.minusDays(6)

        db.collection("dietLogs")
            .whereEqualTo("uid", uid)
            .get()
            .addOnSuccessListener { result ->
                var stapleCount = 0
                var meatCount = 0
                var vegCount = 0
                var otherCount = 0

                for (doc in result) {
                    val dateStr = doc.getString("date") ?: continue
                    val date = LocalDate.parse(dateStr)
                    if (date.isBefore(dateThreshold) || date.isAfter(baseDate)) continue

                    if (!doc.getString("staple").isNullOrBlank()) stapleCount++
                    if (!doc.getString("meat").isNullOrBlank()) meatCount++
                    if (!doc.getString("vegetable").isNullOrBlank()) vegCount++
                    if (!doc.getString("other").isNullOrBlank()) otherCount++
                }

                _dietCategoryRatio.value = MealCategoryCount(
                    staple = stapleCount,
                    meat = meatCount,
                    vegetable = vegCount,
                    other = otherCount
                )
            }
    }
}
