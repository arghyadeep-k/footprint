package com.footprint.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.footprint.app.data.LocationRepository
import com.footprint.app.data.RetentionPolicy
import com.footprint.app.data.RetentionPolicyHelper
import com.footprint.app.data.RetentionPolicyStore
import com.footprint.app.data.importing.CsvImportSummary
import com.footprint.app.data.importing.CsvImportSummaryFormatter
import com.footprint.app.data.importing.LocationCsvImportParser
import com.footprint.app.data.local.LocationPoint
import com.footprint.app.location.TrackingController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

sealed interface PrivacyActionState {
    data object Idle : PrivacyActionState
    data object Working : PrivacyActionState
    data class Success(val message: String) : PrivacyActionState
    data class Error(val message: String) : PrivacyActionState
}

class PrivacyViewModel(
    private val locationRepository: LocationRepository,
    private val trackingController: TrackingController,
    private val retentionPolicyStore: RetentionPolicyStore
) : ViewModel() {

    private val _actionState = MutableStateFlow<PrivacyActionState>(PrivacyActionState.Idle)
    val actionState: StateFlow<PrivacyActionState> = _actionState.asStateFlow()
    private val _retentionPolicy = MutableStateFlow(RetentionPolicy.KEEP_FOREVER)
    val retentionPolicy: StateFlow<RetentionPolicy> = _retentionPolicy.asStateFlow()

    init {
        viewModelScope.launch {
            retentionPolicyStore.policyFlow.collect { policy ->
                _retentionPolicy.value = policy
            }
        }
    }

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

    fun deleteHistoryBetween(
        startEpochMillis: Long,
        endEpochMillis: Long,
        onSuccess: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            _actionState.value = PrivacyActionState.Working
            runCatching {
                locationRepository.deletePointsBetween(startEpochMillis, endEpochMillis)
            }.onSuccess {
                _actionState.value = PrivacyActionState.Success("Deleted history in selected range")
                onSuccess?.invoke()
            }.onFailure {
                _actionState.value = PrivacyActionState.Error("Failed to delete selected range")
            }
        }
    }

    fun setRetentionPolicy(
        policy: RetentionPolicy,
        onSuccess: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            _actionState.value = PrivacyActionState.Working
            runCatching {
                retentionPolicyStore.setPolicy(policy)
                applyRetentionPolicy()
            }.onSuccess {
                _actionState.value = PrivacyActionState.Success("Retention policy updated")
                onSuccess?.invoke()
            }.onFailure {
                _actionState.value = PrivacyActionState.Error("Failed to update retention policy")
            }
        }
    }

    suspend fun applyRetentionPolicy(nowEpochMillis: Long = System.currentTimeMillis()) {
        val policy = _retentionPolicy.value
        val cutoff = RetentionPolicyHelper.cutoffEpochMillis(policy, nowEpochMillis) ?: return
        locationRepository.deletePointsOlderThan(cutoff)
    }

    fun clearActionState() {
        _actionState.update { PrivacyActionState.Idle }
    }

    fun reportActionError(message: String) {
        _actionState.value = PrivacyActionState.Error(message)
    }

    fun importCsvHistory(
        csvContent: String,
        onSuccess: ((CsvImportSummary) -> Unit)? = null
    ) {
        viewModelScope.launch {
            _actionState.value = PrivacyActionState.Working
            runCatching {
                val parseResult = LocationCsvImportParser.parse(csvContent)
                val parsed = parseResult.parsedPoints

                val importedCount = if (parsed.isEmpty()) {
                    0
                } else {
                    val minTimestamp = parsed.minOf { it.recordedAtEpochMillis }
                    val maxTimestamp = parsed.maxOf { it.recordedAtEpochMillis }
                    val existing = locationRepository.getPointsBetween(minTimestamp, maxTimestamp)
                    val existingKeys = existing
                        .asSequence()
                        .map { pointDedupKey(it.recordedAtEpochMillis, it.latitude, it.longitude) }
                        .toMutableSet()
                    val seenImportKeys = mutableSetOf<String>()

                    val toInsert = parsed.mapNotNull { parsedPoint ->
                        val key = pointDedupKey(
                            recordedAtEpochMillis = parsedPoint.recordedAtEpochMillis,
                            latitude = parsedPoint.latitude,
                            longitude = parsedPoint.longitude
                        )
                        when {
                            key in existingKeys -> null
                            !seenImportKeys.add(key) -> null
                            else -> LocationPoint(
                                latitude = parsedPoint.latitude,
                                longitude = parsedPoint.longitude,
                                accuracyMeters = parsedPoint.accuracyMeters,
                                altitudeMeters = null,
                                speedMetersPerSecond = null,
                                bearingDegrees = null,
                                provider = "csv_import",
                                recordedAtEpochMillis = parsedPoint.recordedAtEpochMillis,
                                batteryPercent = null,
                                trackingMode = parsedPoint.trackingMode,
                                source = "import_csv"
                            )
                        }
                    }
                    locationRepository.insertLocationPoints(toInsert).size
                }

                CsvImportSummary(
                    rowsRead = parseResult.rowsRead,
                    pointsImported = importedCount,
                    rowsSkipped = parseResult.rowsSkipped + (parsed.size - importedCount),
                    errors = parseResult.errors
                )
            }.onSuccess { summary ->
                _actionState.value = PrivacyActionState.Success(CsvImportSummaryFormatter.format(summary))
                onSuccess?.invoke(summary)
            }.onFailure {
                _actionState.value = PrivacyActionState.Error("CSV import failed")
            }
        }
    }

    private fun pointDedupKey(recordedAtEpochMillis: Long, latitude: Double, longitude: Double): String {
        return "$recordedAtEpochMillis|${"%.6f".format(Locale.US, latitude)}|${"%.6f".format(Locale.US, longitude)}"
    }
}
