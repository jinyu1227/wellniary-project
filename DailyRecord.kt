package com.example.wellniaryproject

import androidx.room.Entity
import androidx.room.PrimaryKey

// Required no-arg constructor for Firebase
@Entity(tableName = "daily_record")
data class DailyRecord(
    @PrimaryKey val date: String = "",
    val waterCount: Int = 0,
    val weight: Float = 0f,
    val waterGoal: Int = 8,
    val weightGoal: Float = 60f
)
