package com.example.botanyhub.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watering_logs")
data class WateringLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val plantId: Int = 0,
    val wateredAt: Long = System.currentTimeMillis()
)