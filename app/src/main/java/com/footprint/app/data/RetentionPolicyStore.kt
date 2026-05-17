package com.footprint.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.retentionPolicyDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "retention_policy"
)

class RetentionPolicyStore(
    private val context: Context
) {
    val policyFlow: Flow<RetentionPolicy> = context.retentionPolicyDataStore.data
        .map { preferences ->
            RetentionPolicy.fromStorageValue(preferences[Keys.RETENTION_POLICY])
        }
        .distinctUntilChanged()

    suspend fun setPolicy(policy: RetentionPolicy) {
        context.retentionPolicyDataStore.edit { preferences ->
            preferences[Keys.RETENTION_POLICY] = policy.storageValue
        }
    }

    private object Keys {
        val RETENTION_POLICY = stringPreferencesKey("retention_policy")
    }
}
