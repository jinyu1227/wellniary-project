package com.example.wellniaryproject

import androidx.room.Entity
import androidx.room.PrimaryKey

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey var uid: String = "",
    var email: String = "",
    var username: String = "",
    var birthday: String = "",
    var gender: String = "",
    var state: String = "",
    var height: String = "",
    var weight: String = ""
)


