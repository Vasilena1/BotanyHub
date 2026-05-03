package com.example.botanyhub.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.botanyhub.data.PlantEntity
import com.example.botanyhub.data.WateringLog
import com.example.botanyhub.data.repository.PlantRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PlantDetailViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = PlantRepository(app)

    private val _plantId = MutableStateFlow(0)

    val plant: StateFlow<PlantEntity?> = _plantId
        .flatMapLatest { id ->
            if (id == 0) flowOf<PlantEntity?>(null)
            else flow<PlantEntity?> { emit(repo.getPlantById(id)) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val wateringLogs: StateFlow<List<WateringLog>> = _plantId
        .flatMapLatest { id ->
            if (id == 0) flowOf(emptyList())
            else repo.getWateringLogs(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadPlant(id: Int) {
        _plantId.value = id
    }

    fun waterNow() {
        viewModelScope.launch {
            if (_plantId.value != 0) repo.logWatering(_plantId.value)
        }
    }

    fun updatePlant(plant: PlantEntity) {
        viewModelScope.launch { repo.updatePlant(plant) }
    }
}