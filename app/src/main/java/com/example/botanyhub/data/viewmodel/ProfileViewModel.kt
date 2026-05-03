package com.example.botanyhub.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.botanyhub.data.UserProfile
import com.example.botanyhub.data.repository.PlantRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = PlantRepository(app)

    val profile = repo.getUserProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val plants = repo.getAllPlants()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateName(name: String) {
        viewModelScope.launch {
            val current = profile.value ?: UserProfile(id = 1)
            repo.upsertUserProfile(current.copy(name = name))
        }
    }

    fun setNameAndFinishOnboarding(name: String) {
        viewModelScope.launch {
            val current = profile.value ?: UserProfile(id = 1)
            repo.upsertUserProfile(current.copy(name = name))
            getApplication<Application>()
                .getSharedPreferences("botanyhub_prefs", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("onboarding_done", true)
                .apply()
        }
    }

    fun isOnboardingDone(): Boolean {
        return getApplication<Application>()
            .getSharedPreferences("botanyhub_prefs", Context.MODE_PRIVATE)
            .getBoolean("onboarding_done", false)
    }
}