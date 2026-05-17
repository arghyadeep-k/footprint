package com.footprint.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.footprint.app.data.ActiveTripSessionStore
import com.footprint.app.data.LocationRepository
import com.footprint.app.data.RetentionPolicyStore
import com.footprint.app.data.TrackingPreferencesStore
import com.footprint.app.data.TrackingRuntimeStateStore
import com.footprint.app.location.TrackingController

class HomeViewModelFactory(
    private val locationRepository: LocationRepository,
    private val trackingController: TrackingController,
    private val trackingPreferencesStore: TrackingPreferencesStore,
    private val trackingRuntimeStateStore: TrackingRuntimeStateStore,
    private val activeTripSessionStore: ActiveTripSessionStore
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(
                locationRepository = locationRepository,
                trackingController = trackingController,
                trackingPreferencesStore = trackingPreferencesStore,
                trackingRuntimeStateStore = trackingRuntimeStateStore,
                activeTripSessionStore = activeTripSessionStore
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

class SettingsViewModelFactory(
    private val trackingPreferencesStore: TrackingPreferencesStore
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(trackingPreferencesStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

class PrivacyViewModelFactory(
    private val locationRepository: LocationRepository,
    private val trackingController: TrackingController,
    private val retentionPolicyStore: RetentionPolicyStore
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PrivacyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PrivacyViewModel(
                locationRepository = locationRepository,
                trackingController = trackingController,
                retentionPolicyStore = retentionPolicyStore
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
