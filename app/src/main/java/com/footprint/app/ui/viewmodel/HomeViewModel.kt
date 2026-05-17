package com.footprint.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.footprint.app.data.ActiveTripSessionStore
import com.footprint.app.data.LocationRepository
import com.footprint.app.data.TrackingPreferencesStore
import com.footprint.app.data.TrackingRuntimeStateStore
import com.footprint.app.data.export.TimelineExportFormatter
import com.footprint.app.data.local.LocationPoint
import com.footprint.app.location.TrackingController
import com.footprint.app.location.TrackingMode
import com.footprint.app.location.TrackingState
import com.footprint.app.timeline.DistanceCalculator
import com.footprint.app.timeline.TimelineDataUiState
import com.footprint.app.timeline.TimelineDataUiStateFactory
import com.footprint.app.timeline.TimelineRange
import com.footprint.app.timeline.VisitDetector
import com.footprint.app.timeline.VisitSegment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TimelineOption {
    TODAY,
    THIS_WEEK,
    THIS_MONTH,
    THIS_YEAR,
    LIFETIME,
    CUSTOM
}

data class TimelineStatsUi(
    val pointCount: Int = 0,
    val distanceKmLabel: String = "0.00 km",
    val firstRecordedLabel: String = "-",
    val lastRecordedLabel: String = "-"
) {
    companion object
}

data class HomeUiState(
    val selectedTrackingMode: TrackingMode = TrackingMode.BALANCED,
    val trackingState: TrackingState = TrackingState.stopped(),
    val isActiveTripRequested: Boolean = false,
    val canStartTracking: Boolean = false,
    val selectedOption: TimelineOption = TimelineOption.TODAY,
    val customStartMillis: Long = 0L,
    val customEndMillis: Long = 0L,
    val selectedRange: TimelineRange = TimelineRange.Today,
    val timelineState: TimelineDataUiState = TimelineDataUiState.Loading,
    val points: List<LocationPoint> = emptyList(),
    val visits: List<VisitSegment> = emptyList(),
    val stats: TimelineStatsUi = TimelineStatsUi(),
    val exportMessage: String? = null,
    val dataRefreshKey: Int = 0
)

class HomeViewModel(
    private val locationRepository: LocationRepository,
    private val trackingController: TrackingController,
    private val trackingPreferencesStore: TrackingPreferencesStore,
    private val trackingRuntimeStateStore: TrackingRuntimeStateStore,
    private val activeTripSessionStore: ActiveTripSessionStore
) : ViewModel() {

    private val now = System.currentTimeMillis()
    private val initialStart = now - (3L * 24 * 60 * 60 * 1000)
    private val initialEnd = now

    private val _uiState = MutableStateFlow(
        HomeUiState(
            customStartMillis = initialStart,
            customEndMillis = initialEnd,
            selectedRange = TimelineOption.TODAY.toRange(initialStart, initialEnd)
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeTrackingState()
        reloadTimelineData()
    }

    private fun observeTrackingState() {
        viewModelScope.launch {
            combine(
                trackingPreferencesStore.trackingModeFlow,
                trackingRuntimeStateStore.trackingStateFlow,
                activeTripSessionStore.sessionFlow
            ) { preferredMode, trackingState, activeTrip ->
                Triple(preferredMode, trackingState, activeTrip.isActive)
            }.collect { (preferredMode, trackingState, isActiveTripRequested) ->
                _uiState.update {
                    it.copy(
                        selectedTrackingMode = preferredMode,
                        trackingState = trackingState,
                        isActiveTripRequested = isActiveTripRequested
                    )
                }
            }
        }
    }

    fun updatePermissionReadiness(canStartTracking: Boolean) {
        _uiState.update { it.copy(canStartTracking = canStartTracking) }
    }

    fun onTimelineOptionSelected(option: TimelineOption) {
        _uiState.update {
            val range = option.toRange(it.customStartMillis, it.customEndMillis)
            it.copy(selectedOption = option, selectedRange = range)
        }
        reloadTimelineData()
    }

    fun onCustomStartChanged(startMillis: Long) {
        _uiState.update {
            val range = it.selectedOption.toRange(startMillis, it.customEndMillis)
            it.copy(customStartMillis = startMillis, selectedRange = range)
        }
        reloadTimelineData()
    }

    fun onCustomEndChanged(endMillis: Long) {
        _uiState.update {
            val range = it.selectedOption.toRange(it.customStartMillis, endMillis)
            it.copy(customEndMillis = endMillis, selectedRange = range)
        }
        reloadTimelineData()
    }

    fun reloadTimelineData() {
        viewModelScope.launch {
            _uiState.update { it.copy(timelineState = TimelineDataUiState.Loading) }
            val range = _uiState.value.selectedRange
            val state = try {
                val loadedPoints = locationRepository.getPointsForTimelineRange(range)
                TimelineDataUiStateFactory.fromPoints(loadedPoints)
            } catch (error: Throwable) {
                TimelineDataUiStateFactory.fromError(error)
            }

            val points = (state as? TimelineDataUiState.Success)?.points.orEmpty()
            _uiState.update {
                it.copy(
                    timelineState = state,
                    points = points,
                    visits = VisitDetector.detectVisits(points),
                    stats = TimelineStatsUi.from(points)
                )
            }
        }
    }

    fun refreshAfterDataChange() {
        _uiState.update { it.copy(dataRefreshKey = it.dataRefreshKey + 1) }
        reloadTimelineData()
    }

    fun onStartTracking(onPermissionMissing: () -> Unit) {
        if (!_uiState.value.canStartTracking) {
            onPermissionMissing()
            return
        }
        trackingController.startTracking()
    }

    fun onStopTracking() {
        trackingController.stopTracking()
    }

    fun onStartActiveTrip(onPermissionMissing: () -> Unit) {
        if (!_uiState.value.canStartTracking) {
            onPermissionMissing()
            return
        }
        trackingController.startActiveTrip()
    }

    fun onStopActiveTrip() {
        trackingController.stopActiveTrip()
    }

    fun exportCsv(): String {
        val csv = TimelineExportFormatter.toCsv(_uiState.value.points)
        return csv
    }

    fun exportGeoJson(): String {
        val geo = TimelineExportFormatter.toGeoJson(_uiState.value.points)
        return geo
    }

    fun markExportResult(message: String?) {
        _uiState.update { it.copy(exportMessage = message) }
    }

    companion object {
        fun TimelineOption.toRange(customStartMillis: Long, customEndMillis: Long): TimelineRange {
            return when (this) {
                TimelineOption.TODAY -> TimelineRange.Today
                TimelineOption.THIS_WEEK -> TimelineRange.ThisWeek
                TimelineOption.THIS_MONTH -> TimelineRange.ThisMonth
                TimelineOption.THIS_YEAR -> TimelineRange.ThisYear
                TimelineOption.LIFETIME -> TimelineRange.Lifetime
                TimelineOption.CUSTOM -> TimelineRange.Custom(customStartMillis, customEndMillis)
            }
        }

        fun label(range: TimelineRange): String {
            return when (range) {
                TimelineRange.Today -> "Today"
                TimelineRange.Yesterday -> "Yesterday"
                TimelineRange.ThisWeek -> "This Week"
                TimelineRange.ThisMonth -> "This Month"
                TimelineRange.ThisYear -> "This Year"
                TimelineRange.Lifetime -> "Lifetime"
                is TimelineRange.Custom -> "Custom"
            }
        }

        fun fileSuffix(range: TimelineRange): String {
            return when (range) {
                TimelineRange.Today -> "today"
                TimelineRange.Yesterday -> "yesterday"
                TimelineRange.ThisWeek -> "this_week"
                TimelineRange.ThisMonth -> "this_month"
                TimelineRange.ThisYear -> "this_year"
                TimelineRange.Lifetime -> "lifetime"
                is TimelineRange.Custom -> "custom"
            }
        }
    }
}

fun TimelineStatsUi.Companion.from(points: List<LocationPoint>): TimelineStatsUi {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val first = points.firstOrNull()?.recordedAtEpochMillis
    val last = points.lastOrNull()?.recordedAtEpochMillis
    val distanceKm = DistanceCalculator.totalDistanceMeters(points) / 1000.0

    return TimelineStatsUi(
        pointCount = points.size,
        distanceKmLabel = String.format(Locale.getDefault(), "%.2f km", distanceKm),
        firstRecordedLabel = first?.let { formatter.format(Date(it)) } ?: "-",
        lastRecordedLabel = last?.let { formatter.format(Date(it)) } ?: "-"
    )
}
