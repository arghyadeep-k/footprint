package com.footprint.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.activeTripSessionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "active_trip_session"
)

data class ActiveTripSession(
    val isActive: Boolean,
    val startedAtEpochMillis: Long?
)

class ActiveTripSessionStore(
    private val context: Context
) {
    val sessionFlow: Flow<ActiveTripSession> = context.activeTripSessionDataStore.data
        .map { prefs ->
            ActiveTripSession(
                isActive = prefs[Keys.IS_ACTIVE] ?: false,
                startedAtEpochMillis = prefs[Keys.STARTED_AT_EPOCH_MILLIS]
            )
        }
        .distinctUntilChanged()

    suspend fun startActiveTrip(startedAtEpochMillis: Long) {
        context.activeTripSessionDataStore.edit { prefs ->
            prefs[Keys.IS_ACTIVE] = true
            prefs[Keys.STARTED_AT_EPOCH_MILLIS] = startedAtEpochMillis
        }
    }

    suspend fun stopActiveTrip() {
        context.activeTripSessionDataStore.edit { prefs ->
            prefs[Keys.IS_ACTIVE] = false
            prefs.remove(Keys.STARTED_AT_EPOCH_MILLIS)
        }
    }

    private object Keys {
        val IS_ACTIVE = booleanPreferencesKey("active_trip_is_active")
        val STARTED_AT_EPOCH_MILLIS = longPreferencesKey("active_trip_started_at_epoch_millis")
    }
}
