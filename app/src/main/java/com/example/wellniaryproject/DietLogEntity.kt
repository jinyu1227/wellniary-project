package com.example.wellniaryproject

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diet_logs")
data class DietLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val uid: String,
    val date: String,       // e.g., "2025-05-12"
    val mealType: String,   // Breakfast, Lunch, Dinner
    val staple: String,
    val meat: String,
    val vegetable: String,
    val other: String,
    val isSynced: Boolean = false
)

