package com.example.wellniaryproject

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminder_settings")
data class ReminderSetting(
    @PrimaryKey val label: String,
    val enabled: Boolean,
    val day: String,
    val time: String
)
