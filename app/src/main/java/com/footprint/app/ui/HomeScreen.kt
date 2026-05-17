package com.footprint.app.ui

import android.app.DatePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.footprint.app.data.LocationRepository
import com.footprint.app.data.export.TimelineExportFormatter
import com.footprint.app.data.local.LocationPoint
import com.footprint.app.location.TrackingMode
import com.footprint.app.location.TrackingState
import com.footprint.app.location.TrackingStateUiMapper
import com.footprint.app.map.MapScreen
import com.footprint.app.timeline.DistanceCalculator
import com.footprint.app.timeline.TimelineDataUiState
import com.footprint.app.timeline.TimelineDataUiStateFactory
import com.footprint.app.timeline.TimelineRange
import com.footprint.app.timeline.VisitDetector
import com.footprint.app.timeline.VisitSegment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private enum class TimelineOption {
    TODAY,
    THIS_WEEK,
    THIS_MONTH,
    THIS_YEAR,
    LIFETIME,
    CUSTOM
}

@Composable
fun HomeScreen(
    selectedTrackingMode: TrackingMode,
    trackingState: TrackingState,
    isActiveTripRequested: Boolean,
    dataRefreshKey: Int,
    locationRepository: LocationRepository,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit,
    onStartActiveTrip: () -> Unit,
    onStopActiveTrip: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onManagePermissions: () -> Unit,
    canStartTracking: Boolean
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val now = System.currentTimeMillis()
    val initialStart = remember { now - (3L * 24 * 60 * 60 * 1000) }
    val initialEnd = remember { now }

    var selectedOption by remember { mutableStateOf(TimelineOption.TODAY) }
    var customStartMillis by remember { mutableStateOf(initialStart) }
    var customEndMillis by remember { mutableStateOf(initialEnd) }

    val selectedRange = remember(selectedOption, customStartMillis, customEndMillis) {
        selectedOption.toRange(customStartMillis, customEndMillis)
    }

    var loadAttempt by remember(selectedRange, dataRefreshKey) { mutableStateOf(0) }
    var timelineState by remember(selectedRange, dataRefreshKey) {
        mutableStateOf<TimelineDataUiState>(TimelineDataUiState.Loading)
    }

    val csvExporter = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            val snapshot = (timelineState as? TimelineDataUiState.Success)?.points.orEmpty()
            coroutineScope.launch(Dispatchers.IO) {
                val csv = TimelineExportFormatter.toCsv(snapshot)
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(csv.toByteArray(Charsets.UTF_8))
                }
            }
        }
    }

    val geoJsonExporter = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/geo+json")
    ) { uri ->
        if (uri != null) {
            val snapshot = (timelineState as? TimelineDataUiState.Success)?.points.orEmpty()
            coroutineScope.launch(Dispatchers.IO) {
                val geoJson = TimelineExportFormatter.toGeoJson(snapshot)
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(geoJson.toByteArray(Charsets.UTF_8))
                }
            }
        }
    }

    LaunchedEffect(selectedRange, dataRefreshKey, loadAttempt) {
        timelineState = TimelineDataUiState.Loading
        timelineState = try {
            val loadedPoints = withContext(Dispatchers.IO) {
                locationRepository.getPointsForTimelineRange(selectedRange)
            }
            TimelineDataUiStateFactory.fromPoints(loadedPoints)
        } catch (error: Throwable) {
            TimelineDataUiStateFactory.fromError(error)
        }
    }

    val points = (timelineState as? TimelineDataUiState.Success)?.points.orEmpty()
    val stats = remember(points) { TimelineStats.from(points) }
    val visits = remember(points) { VisitDetector.detectVisits(points) }
    val currentTimelineState = timelineState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Footprint",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Footprint uses your location history to draw your travel line on a map.",
            style = MaterialTheme.typography.bodyLarge
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Tracking status", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = TrackingStateUiMapper.statusLabel(trackingState),
                    color = if (trackingState.status == TrackingState.Status.RUNNING) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = "Effective mode: ${TrackingStateUiMapper.effectiveModeLabel(trackingState)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Timeline", style = MaterialTheme.typography.titleMedium)
                TimelineSelector(
                    selected = selectedOption,
                    onSelect = { selectedOption = it }
                )

                if (selectedOption == TimelineOption.CUSTOM) {
                    CustomRangePicker(
                        startMillis = customStartMillis,
                        endMillis = customEndMillis,
                        onStartChanged = { customStartMillis = it },
                        onEndChanged = { customEndMillis = it }
                    )
                }

                Text(
                    text = "Selected: ${selectedRange.label()}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Stats", style = MaterialTheme.typography.titleMedium)
                when (currentTimelineState) {
                    TimelineDataUiState.Loading -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            CircularProgressIndicator()
                            Text("Loading timeline stats...")
                        }
                    }

                    is TimelineDataUiState.Error -> {
                        Text("Could not load timeline data.")
                        Text(
                            text = currentTimelineState.message,
                            style = MaterialTheme.typography.bodySmall
                        )
                        OutlinedButton(
                            onClick = { loadAttempt += 1 },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Retry")
                        }
                    }

                    TimelineDataUiState.Empty,
                    is TimelineDataUiState.Success -> {
                        Text("Saved points: ${stats.pointCount}")
                        Text("Approx distance: ${stats.distanceKmLabel}")
                        Text("First recorded: ${stats.firstRecordedLabel}")
                        Text("Last recorded: ${stats.lastRecordedLabel}")

                        OutlinedButton(
                            onClick = {
                                val fileName = "footprint_${selectedRange.fileSuffix()}_${System.currentTimeMillis()}.csv"
                                csvExporter.launch(fileName)
                            },
                            enabled = timelineState is TimelineDataUiState.Success,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Export CSV")
                        }

                        OutlinedButton(
                            onClick = {
                                val fileName = "footprint_${selectedRange.fileSuffix()}_${System.currentTimeMillis()}.geojson"
                                geoJsonExporter.launch(fileName)
                            },
                            enabled = timelineState is TimelineDataUiState.Success,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Export GeoJSON")
                        }
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Map", style = MaterialTheme.typography.titleMedium)
                MapScreen(
                    timelineState = timelineState,
                    visitSegments = visits,
                    selectedRange = selectedRange,
                    onRetry = { loadAttempt += 1 },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                )
            }
        }

        if (timelineState is TimelineDataUiState.Success || timelineState is TimelineDataUiState.Empty) {
            PlacesVisitedList(visits = visits)
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Tracking", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Current mode: ${selectedTrackingMode.notificationLabel}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = if (trackingState.isActiveTripRunning) {
                        "Active trip tracking is running (high battery use)."
                    } else if (isActiveTripRequested) {
                        "Active trip is armed and will run while tracking is active."
                    } else {
                        "Active trip is off."
                    },
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedButton(
                    onClick = onStartTracking,
                    enabled = canStartTracking,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Start tracking")
                }
                if (!canStartTracking) {
                    Text(
                        text = "Tracking needs foreground location, background location, and notifications (Android 13+) to be ready.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                OutlinedButton(
                    onClick = onStopTracking,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Stop tracking")
                }
                OutlinedButton(
                    onClick = onStartActiveTrip,
                    enabled = canStartTracking && !trackingState.isActiveTripRunning,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Start active trip (2h)")
                }
                OutlinedButton(
                    onClick = onStopActiveTrip,
                    enabled = isActiveTripRequested || trackingState.isActiveTripRunning,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Stop active trip")
                }
                OutlinedButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tracking settings")
                }
                OutlinedButton(
                    onClick = onOpenPrivacy,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Privacy & data controls")
                }
            }
        }

        OutlinedButton(
            onClick = onManagePermissions,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Text("Review permissions")
        }
    }
}

@Composable
private fun PlacesVisitedList(visits: List<VisitSegment>) {
    val formatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Places Visited", style = MaterialTheme.typography.titleMedium)

            if (visits.isEmpty()) {
                Text("No stationary visits detected for this timeline yet.")
            } else {
                visits.forEachIndexed { index, visit ->
                    Text("Place ${index + 1}")
                    Text(
                        "Lat/Lng: ${"%.5f".format(Locale.getDefault(), visit.latitude)}, ${"%.5f".format(Locale.getDefault(), visit.longitude)}"
                    )
                    Text("Arrival: ${formatter.format(Date(visit.arrivalEpochMillis))}")
                    Text("Departure: ${formatter.format(Date(visit.departureEpochMillis))}")
                    Text("Duration: ${formatDuration(visit.durationMillis)}")
                }
            }
        }
    }
}

@Composable
private fun TimelineSelector(
    selected: TimelineOption,
    onSelect: (TimelineOption) -> Unit
) {
    val options = listOf(
        "Today" to TimelineOption.TODAY,
        "This Week" to TimelineOption.THIS_WEEK,
        "This Month" to TimelineOption.THIS_MONTH,
        "This Year" to TimelineOption.THIS_YEAR,
        "Lifetime" to TimelineOption.LIFETIME,
        "Custom" to TimelineOption.CUSTOM
    )

    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (label, option) ->
            AssistChip(
                onClick = { onSelect(option) },
                label = { Text(label) }
            )
        }
    }
}

@Composable
private fun CustomRangePicker(
    startMillis: Long,
    endMillis: Long,
    onStartChanged: (Long) -> Unit,
    onEndChanged: (Long) -> Unit
) {
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
            onClick = {
                showDatePicker(
                    context = context,
                    initialMillis = startMillis,
                    onSelected = onStartChanged
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start: ${formatDate(startMillis)}")
        }

        OutlinedButton(
            onClick = {
                showDatePicker(
                    context = context,
                    initialMillis = endMillis,
                    onSelected = onEndChanged
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("End: ${formatDate(endMillis)}")
        }
    }
}

private fun showDatePicker(
    context: android.content.Context,
    initialMillis: Long,
    onSelected: (Long) -> Unit
) {
    val calendar = Calendar.getInstance().apply { timeInMillis = initialMillis }
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selected = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            onSelected(selected)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

private data class TimelineStats(
    val pointCount: Int,
    val distanceKmLabel: String,
    val firstRecordedLabel: String,
    val lastRecordedLabel: String
) {
    companion object {
        fun from(points: List<LocationPoint>): TimelineStats {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val first = points.firstOrNull()?.recordedAtEpochMillis
            val last = points.lastOrNull()?.recordedAtEpochMillis
            val distanceKm = DistanceCalculator.totalDistanceMeters(points) / 1000.0

            return TimelineStats(
                pointCount = points.size,
                distanceKmLabel = String.format(Locale.getDefault(), "%.2f km", distanceKm),
                firstRecordedLabel = first?.let { formatter.format(Date(it)) } ?: "-",
                lastRecordedLabel = last?.let { formatter.format(Date(it)) } ?: "-"
            )
        }
    }
}

private fun TimelineOption.toRange(customStartMillis: Long, customEndMillis: Long): TimelineRange {
    return when (this) {
        TimelineOption.TODAY -> TimelineRange.Today
        TimelineOption.THIS_WEEK -> TimelineRange.ThisWeek
        TimelineOption.THIS_MONTH -> TimelineRange.ThisMonth
        TimelineOption.THIS_YEAR -> TimelineRange.ThisYear
        TimelineOption.LIFETIME -> TimelineRange.Lifetime
        TimelineOption.CUSTOM -> TimelineRange.Custom(customStartMillis, customEndMillis)
    }
}

private fun TimelineRange.label(): String {
    return when (this) {
        TimelineRange.Today -> "Today"
        TimelineRange.Yesterday -> "Yesterday"
        TimelineRange.ThisWeek -> "This Week"
        TimelineRange.ThisMonth -> "This Month"
        TimelineRange.ThisYear -> "This Year"
        TimelineRange.Lifetime -> "Lifetime"
        is TimelineRange.Custom -> "Custom"
    }
}

private fun TimelineRange.fileSuffix(): String {
    return when (this) {
        TimelineRange.Today -> "today"
        TimelineRange.Yesterday -> "yesterday"
        TimelineRange.ThisWeek -> "this_week"
        TimelineRange.ThisMonth -> "this_month"
        TimelineRange.ThisYear -> "this_year"
        TimelineRange.Lifetime -> "lifetime"
        is TimelineRange.Custom -> "custom"
    }
}

private fun formatDate(epochMillis: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return formatter.format(Date(epochMillis))
}

private fun formatDuration(durationMillis: Long): String {
    val minutes = durationMillis / 60_000L
    val hours = minutes / 60
    val remainingMinutes = minutes % 60
    return if (hours > 0) {
        "${hours}h ${remainingMinutes}m"
    } else {
        "${remainingMinutes}m"
    }
}
