package com.example.wellniaryproject

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*


import com.google.firebase.firestore.ktx.firestore

import java.util.*
import android.util.Log

class HealthViewModel : ViewModel() {

    // Variables fully driven by Firebase
    var currentMugCount by mutableStateOf<Int?>(null)
    var dailyMugTarget by mutableStateOf<Int?>(null)

    var currentWeight by mutableStateOf<Float?>(null)
    var targetWeight by mutableStateOf<Float?>(null)

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

    fun loadHealthDataFromFirebase() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val ref = FirebaseDatabase.getInstance()
            .reference
            .child("records")
            .child(uid)
            .child(today)

        ref.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val waterCount = snapshot.child("waterCount").getValue(Int::class.java)
                val weight = snapshot.child("weight").getValue(Int::class.java)

                currentMugCount = waterCount
                currentWeight = weight?.toFloat()

                checkAchievementStatus()
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Log.e("FirebaseListen", "Failed to listen: ${error.message}")
            }
        })
    }

    fun loadGoals() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance().reference.child("userGoals").child(uid)

        ref.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val waterGoal = snapshot.child("waterGoal").getValue(Int::class.java)
                val weightGoal = snapshot.child("weightGoal").getValue(Float::class.java)

                dailyMugTarget = waterGoal
                targetWeight = weightGoal

                checkAchievementStatus()
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Log.e("GoalListener", "Failed to listen to goal changes: ${error.message}")
            }
        })
    }

    fun checkAchievementStatus() {
        waterGoalAchieved = currentMugCount != null && dailyMugTarget != null && currentMugCount!! >= dailyMugTarget!!
        weightGoalAchieved = currentWeight != null && targetWeight != null && currentWeight!! <= targetWeight!!
    }

    fun saveHealthData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val data = mapOf(
            "date" to today,
            "waterCount" to (currentMugCount ?: 0),
            "weight" to (currentWeight ?: 0f)
        )

        FirebaseDatabase.getInstance()
            .getReference("records")
            .child(uid)
            .child(today)
            .setValue(data)
    }

    init {
        checkLoginStreak()
        loadHealthDataFromFirebase()
        loadGoals()
    }
}
