package com.example.botanyhub.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Gardener",
    val avatarUri: String = "",
    val level: Int = 1,
    val xp: Int = 0,
    val streakDays: Int = 0
)