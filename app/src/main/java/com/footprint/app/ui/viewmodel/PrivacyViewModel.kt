package com.footprint.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.footprint.app.data.LocationRepository
import com.footprint.app.location.TrackingController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface PrivacyActionState {
    data object Idle : PrivacyActionState
    data object Working : PrivacyActionState
    data class Success(val message: String) : PrivacyActionState
    data class Error(val message: String) : PrivacyActionState
}

class PrivacyViewModel(
    private val locationRepository: LocationRepository,
    private val trackingController: TrackingController
) : ViewModel() {

    private val _actionState = MutableStateFlow<PrivacyActionState>(PrivacyActionState.Idle)
    val actionState: StateFlow<PrivacyActionState> = _actionState.asStateFlow()

    fun pauseTracking(onSuccess: (() -> Unit)? = null) {
        trackingController.stopTracking()
        _actionState.value = PrivacyActionState.Success("Tracking paused")
        onSuccess?.invoke()
    }

    fun deleteAllHistory(onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            _actionState.value = PrivacyActionState.Working
            runCatching {
                locationRepository.deleteAllPoints()
            }.onSuccess {
                _actionState.value = PrivacyActionState.Success("Deleted all location history")
                onSuccess?.invoke()
            }.onFailure {
                _actionState.value = PrivacyActionState.Error("Failed to delete location history")
            }
        }
    }

    fun deleteHistoryOlderThan30Days(onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            _actionState.value = PrivacyActionState.Working
            runCatching {
                val cutoff = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
                locationRepository.deletePointsOlderThan(cutoff)
            }.onSuccess {
                _actionState.value = PrivacyActionState.Success("Deleted history older than 30 days")
                onSuccess?.invoke()
            }.onFailure {
                _actionState.value = PrivacyActionState.Error("Failed to delete older history")
            }
        }
    }

    fun clearActionState() {
        _actionState.update { PrivacyActionState.Idle }
    }
}
