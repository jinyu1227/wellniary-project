package com.example.wellniaryproject

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE uid = :uid LIMIT 1")
    suspend fun getProfileByUid(uid: String): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)
}
