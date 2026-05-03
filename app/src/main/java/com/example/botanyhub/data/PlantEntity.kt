package com.example.botanyhub.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plants")
data class PlantEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val commonName: String = "",
    val scientificName: String = "",
    val family: String = "",
    val imageUri: String = "",
    val plantNetImageUrl: String = "",
    val isIndoor: Boolean = true,
    val sunlight: String = "medium",
    val waterFrequencyDays: Int = 7,
    val notes: String = "",
    val addedDate: Long = System.currentTimeMillis()
)