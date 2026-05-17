package com.footprint.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.footprint.app.data.TrackingPreferencesStore
import com.footprint.app.location.TrackingMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val trackingPreferencesStore: TrackingPreferencesStore
) : ViewModel() {

    private val _selectedMode = MutableStateFlow(TrackingMode.BALANCED)
    val selectedMode: StateFlow<TrackingMode> = _selectedMode.asStateFlow()

    init {
        viewModelScope.launch {
            trackingPreferencesStore.trackingModeFlow.collect {
                _selectedMode.value = it
            }
        }
    }

    fun selectMode(mode: TrackingMode) {
        viewModelScope.launch {
            trackingPreferencesStore.setTrackingMode(mode)
        }
    }
}
