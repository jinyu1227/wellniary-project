package com.example.wellniaryproject

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class HealthViewModel : ViewModel() {


    var currentMugCount by mutableStateOf(0)


    var dailyMugTarget by mutableStateOf(8)


    var currentWeight by mutableStateOf(75f)


    var targetWeight by mutableStateOf(70f)

    var waterGoalAchieved by mutableStateOf(false)
    var weightGoalAchieved by mutableStateOf(false)


    var consecutiveLoginUnlocked by mutableStateOf(false)
    private set


    fun checkLoginStreak() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val calendar = Calendar.getInstance()
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val last7Days = (0..6).map {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -it)
            formatter.format(calendar.time)
        }

        Firebase.firestore.collection("loginHistory")
            .document(uid)
            .collection("dates")
            .get()
            .addOnSuccessListener { snapshot ->
                val loggedDates = snapshot.documents.map { it.id }
                val all7Logged = last7Days.all { it in loggedDates }
                consecutiveLoginUnlocked = all7Logged
            }
    }

    init {
        checkLoginStreak()
    }
}

