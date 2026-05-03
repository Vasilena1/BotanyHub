package com.example.botanyhub.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.botanyhub.data.PlantCareTips
import com.example.botanyhub.data.PlantNetResult
import com.example.botanyhub.data.repository.PlantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ScanUiState {
    object Idle : ScanUiState()
    object Loading : ScanUiState()
    data class Success(
        val results: List<PlantNetResult>,
        val imageUri: Uri
    ) : ScanUiState()
    data class Error(val message: String) : ScanUiState()
}

sealed class CareTipsState {
    object Idle : CareTipsState()
    object Loading : CareTipsState()
    data class Ready(val tips: PlantCareTips) : CareTipsState()
    object NotFound : CareTipsState()
}

class ScanViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = PlantRepository(app)

    private val _state = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val state: StateFlow<ScanUiState> = _state

    private val _careTipsState = MutableStateFlow<CareTipsState>(CareTipsState.Idle)
    val careTipsState: StateFlow<CareTipsState> = _careTipsState

    // Временно пази tips до AddPlantScreen
    private var _pendingCareTips: PlantCareTips? = null
    fun consumeCareTips(): PlantCareTips? {
        val tips = _pendingCareTips
        _pendingCareTips = null
        return tips
    }

    fun identify(uri: Uri) {
        viewModelScope.launch {
            _state.value = ScanUiState.Loading
            try {
                val response = repo.identifyPlant(uri)
                _state.value = ScanUiState.Success(
                    results = response.results.take(3),
                    imageUri = uri
                )
            } catch (e: Exception) {
                _state.value = ScanUiState.Error(e.message ?: "Identification failed")
            }
        }
    }

    fun fetchCareTipsAndAdd(
        result: PlantNetResult,
        onReady: (PlantCareTips?) -> Unit
    ) {
        viewModelScope.launch {
            _careTipsState.value = CareTipsState.Loading
            val tips = try {
                repo.fetchCareTips(result.species.scientificName ?: "")
            } catch (e: Exception) {
                null
            }
            _pendingCareTips = tips
            _careTipsState.value = if (tips != null) CareTipsState.Ready(tips)
            else CareTipsState.NotFound
            onReady(tips)
        }
    }

    fun reset() {
        _state.value = ScanUiState.Idle
        _careTipsState.value = CareTipsState.Idle
        _pendingCareTips = null
    }
}