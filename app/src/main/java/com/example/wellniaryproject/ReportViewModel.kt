package com.example.wellniaryproject.viewmodel

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wellniaryproject.DailyRecord
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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
        fetchLast7Days()
        fetchDietDataForPieChart()
    }

    private fun fetchLast7Days() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = Firebase.database.reference.child("records").child(uid)

        viewModelScope.launch {
            val today = LocalDate.now()
            val dateList = (0..6).map { today.minusDays(it.toLong()) }.reversed()

            ref.get().addOnSuccessListener { snapshot ->
                val result = dateList.map { date ->
                    val dateStr = date.toString()
                    snapshot.child(dateStr).getValue<DailyRecord>() ?: DailyRecord(
                        date = dateStr,
                        waterCount = 0,
                        weight = 0f
                    )
                }
                _weeklyData.value = result
            }
        }
    }

    private fun fetchDietDataForPieChart() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = Firebase.firestore
        val today = LocalDate.now()
        val dateThreshold = today.minusDays(6)

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
                    if (date.isBefore(dateThreshold)) continue

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
