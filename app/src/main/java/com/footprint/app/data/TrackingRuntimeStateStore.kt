package com.footprint.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.footprint.app.location.TrackingMode
import com.footprint.app.location.TrackingState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.trackingRuntimeDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "tracking_runtime_state"
)

class TrackingRuntimeStateStore(
    private val context: Context
) {
    val trackingStateFlow: Flow<TrackingState> = context.trackingRuntimeDataStore.data
        .map { prefs ->
            val status = TrackingState.Status.entries.firstOrNull {
                it.name == prefs[Keys.STATUS]
            } ?: TrackingState.Status.STOPPED

            val effectiveMode = TrackingMode.fromStorageValue(prefs[Keys.EFFECTIVE_MODE])
                .takeIf { status == TrackingState.Status.RUNNING }

            TrackingState(
                status = status,
                effectiveMode = effectiveMode,
                isActiveTripRunning = prefs[Keys.IS_ACTIVE_TRIP_RUNNING] ?: false,
                errorMessage = prefs[Keys.ERROR_MESSAGE]
            )
        }
        .distinctUntilChanged()

    suspend fun setState(state: TrackingState) {
        context.trackingRuntimeDataStore.edit { prefs ->
            prefs[Keys.STATUS] = state.status.name
            if (state.effectiveMode == null) {
                prefs.remove(Keys.EFFECTIVE_MODE)
            } else {
                prefs[Keys.EFFECTIVE_MODE] = state.effectiveMode.storageValue
            }

            if (state.errorMessage.isNullOrBlank()) {
                prefs.remove(Keys.ERROR_MESSAGE)
            } else {
                prefs[Keys.ERROR_MESSAGE] = state.errorMessage
            }
            prefs[Keys.IS_ACTIVE_TRIP_RUNNING] = state.isActiveTripRunning
        }
    }

    private object Keys {
        val STATUS = stringPreferencesKey("tracking_status")
        val EFFECTIVE_MODE = stringPreferencesKey("tracking_effective_mode")
        val ERROR_MESSAGE = stringPreferencesKey("tracking_error_message")
        val IS_ACTIVE_TRIP_RUNNING = booleanPreferencesKey("tracking_is_active_trip_running")
    }
}
