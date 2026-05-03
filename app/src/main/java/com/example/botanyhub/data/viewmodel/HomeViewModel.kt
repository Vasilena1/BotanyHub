package com.example.botanyhub.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.botanyhub.data.*
import com.example.botanyhub.data.repository.PlantRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = PlantRepository(app)

    val plants: StateFlow<List<PlantEntity>> = repo.getAllPlants()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val plantsWithWatering: StateFlow<List<PlantWithWatering>> =
        plants.map { list ->
            list.map { plant ->
                val lastLog = repo.getLastWatering(plant.id)
                val lastTs = lastLog?.wateredAt ?: plant.addedDate
                val nextTs = lastTs + TimeUnit.DAYS.toMillis(plant.waterFrequencyDays.toLong())
                PlantWithWatering(plant, lastLog?.wateredAt, nextTs)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val profile: StateFlow<UserProfile?> = repo.getUserProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch { repo.ensureDefaultProfile() }
    }

    fun waterPlant(plantId: Int) {
        viewModelScope.launch { repo.logWatering(plantId) }
    }

    fun deletePlant(plant: PlantEntity) {
        viewModelScope.launch { repo.deletePlant(plant) }
    }
}