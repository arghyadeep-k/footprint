package com.footprint.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.footprint.app.location.TrackingMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.trackingPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "tracking_preferences"
)

class TrackingPreferencesStore(
    private val context: Context
) {
    val trackingModeFlow: Flow<TrackingMode> = context.trackingPreferencesDataStore.data
        .map { preferences ->
            TrackingMode.fromStorageValue(preferences[Keys.TRACKING_MODE]).asPreferredBackgroundMode()
        }
        .distinctUntilChanged()

    suspend fun setTrackingMode(mode: TrackingMode) {
        context.trackingPreferencesDataStore.edit { preferences ->
            preferences[Keys.TRACKING_MODE] = mode.asPreferredBackgroundMode().storageValue
        }
    }

    private fun TrackingMode.asPreferredBackgroundMode(): TrackingMode {
        return if (this == TrackingMode.ACTIVE) TrackingMode.BALANCED else this
    }

    private object Keys {
        val TRACKING_MODE = stringPreferencesKey("tracking_mode")
    }
}
