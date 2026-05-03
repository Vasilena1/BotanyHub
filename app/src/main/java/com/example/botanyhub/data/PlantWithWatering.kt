package com.example.botanyhub.data

data class PlantWithWatering(
    val plant: PlantEntity,
    val lastWateredDate: Long?,
    val nextWateringDate: Long
) {
    val isDueToday: Boolean
        get() = nextWateringDate <= System.currentTimeMillis()
}