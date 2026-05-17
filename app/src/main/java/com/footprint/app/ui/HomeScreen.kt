package com.footprint.app.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.footprint.app.location.TrackingStateUiMapper
import com.footprint.app.location.ActiveTripUiMapper
import com.footprint.app.map.MapScreen
import com.footprint.app.timeline.TimelineDataUiState
import com.footprint.app.timeline.VisitSegment
import com.footprint.app.timeline.VisitSelectionMapper
import com.footprint.app.ui.viewmodel.HomeUiState
import com.footprint.app.ui.viewmodel.HomeViewModel
import com.footprint.app.ui.viewmodel.TimelineOption
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onTimelineOptionSelected: (TimelineOption) -> Unit,
    onCustomStartChanged: (Long) -> Unit,
    onCustomEndChanged: (Long) -> Unit,
    onRetryLoad: () -> Unit,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit,
    onStartActiveTrip: () -> Unit,
    onStopActiveTrip: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onManagePermissions: () -> Unit,
    onExportCsv: () -> String,
    onExportGeoJson: () -> String,
    onMarkExportResult: (String?) -> Unit,
    onVisitSelected: (Int) -> Unit
) {
    val context = LocalContext.current

    val csvExporter = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri == null) {
            onMarkExportResult("Export cancelled")
            return@rememberLauncherForActivityResult
        }
        runCatching {
            val csv = onExportCsv()
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(csv.toByteArray(Charsets.UTF_8))
            }
        }.onSuccess {
            onMarkExportResult("CSV exported")
        }.onFailure {
            onMarkExportResult("CSV export failed")
        }
    }

    val geoJsonExporter = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/geo+json")
    ) { uri ->
        if (uri == null) {
            onMarkExportResult("Export cancelled")
            return@rememberLauncherForActivityResult
        }
        runCatching {
            val geoJson = onExportGeoJson()
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(geoJson.toByteArray(Charsets.UTF_8))
            }
        }.onSuccess {
            onMarkExportResult("GeoJSON exported")
        }.onFailure {
            onMarkExportResult("GeoJSON export failed")
        }
    }

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
                    text = TrackingStateUiMapper.statusLabel(uiState.trackingState),
                    color = if (uiState.trackingState.status == com.footprint.app.location.TrackingState.Status.RUNNING) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = "Effective mode: ${TrackingStateUiMapper.effectiveModeLabel(uiState.trackingState)}",
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
                    selected = uiState.selectedOption,
                    onSelect = onTimelineOptionSelected
                )
                OutlinedButton(
                    onClick = onRetryLoad,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Refresh timeline")
                }

                if (uiState.selectedOption == TimelineOption.CUSTOM) {
                    CustomRangePicker(
                        startMillis = uiState.customStartMillis,
                        endMillis = uiState.customEndMillis,
                        onStartChanged = onCustomStartChanged,
                        onEndChanged = onCustomEndChanged
                    )
                }

                Text(
                    text = "Selected: ${HomeViewModel.label(uiState.selectedRange)}",
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
                when (val timelineState = uiState.timelineState) {
                    TimelineDataUiState.Loading -> {
                        if (uiState.points.isNotEmpty()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                CircularProgressIndicator()
                                Text("Refreshing timeline data…")
                            }
                            Text("Saved points: ${uiState.stats.pointCount}")
                            Text("Approx distance: ${uiState.stats.distanceKmLabel}")
                            Text("First recorded: ${uiState.stats.firstRecordedLabel}")
                            Text("Last recorded: ${uiState.stats.lastRecordedLabel}")
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                CircularProgressIndicator()
                                Text("Loading timeline stats...")
                            }
                        }
                    }

                    is TimelineDataUiState.Error -> {
                        Text("Could not load timeline data.")
                        Text(
                            text = timelineState.message,
                            style = MaterialTheme.typography.bodySmall
                        )
                        OutlinedButton(
                            onClick = onRetryLoad,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Retry")
                        }
                    }

                    TimelineDataUiState.Empty,
                    is TimelineDataUiState.Success -> {
                        Text("Saved points: ${uiState.stats.pointCount}")
                        Text("Approx distance: ${uiState.stats.distanceKmLabel}")
                        Text("First recorded: ${uiState.stats.firstRecordedLabel}")
                        Text("Last recorded: ${uiState.stats.lastRecordedLabel}")

                        OutlinedButton(
                            onClick = {
                                val fileName = "footprint_${HomeViewModel.fileSuffix(uiState.selectedRange)}_${System.currentTimeMillis()}.csv"
                                csvExporter.launch(fileName)
                            },
                            enabled = uiState.timelineState is TimelineDataUiState.Success,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Export CSV")
                        }

                        OutlinedButton(
                            onClick = {
                                val fileName = "footprint_${HomeViewModel.fileSuffix(uiState.selectedRange)}_${System.currentTimeMillis()}.geojson"
                                geoJsonExporter.launch(fileName)
                            },
                            enabled = uiState.timelineState is TimelineDataUiState.Success,
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Quick Summary", style = MaterialTheme.typography.titleMedium)
                SummaryStatRow("Distance per day", uiState.summaryMetrics.distancePerDayLabel)
                SummaryStatRow("Points per day", uiState.summaryMetrics.pointsPerDayLabel)
                SummaryStatRow("Detected visits", uiState.summaryMetrics.visitCount.toString())
                SummaryStatRow("Average daily distance", uiState.summaryMetrics.averageDailyDistanceLabel)
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Map", style = MaterialTheme.typography.titleMedium)
                MapScreen(
                    timelineState = uiState.timelineState,
                    points = uiState.points,
                    visitSegments = uiState.visits,
                    selectedVisitIndex = uiState.selectedVisitIndex,
                    selectedRange = uiState.selectedRange,
                    onRetry = onRetryLoad,
                    onVisitSelected = onVisitSelected,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                )
            }
        }

        if (uiState.timelineState is TimelineDataUiState.Success || uiState.timelineState is TimelineDataUiState.Empty) {
            val selectedVisit = VisitSelectionMapper.selectedVisit(
                visits = uiState.visits,
                selectedIndex = uiState.selectedVisitIndex
            )
            selectedVisit?.let { visit ->
                SelectedVisitCard(visit = visit)
            }
            PlacesVisitedList(
                visits = uiState.visits,
                selectedVisitIndex = uiState.selectedVisitIndex,
                onSelectVisit = onVisitSelected
            )
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Tracking", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Current mode: ${uiState.selectedTrackingMode.notificationLabel}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = if (uiState.trackingState.isActiveTripRunning) {
                        "Active trip tracking is running for an intentional trip. It uses higher accuracy and higher battery."
                    } else if (uiState.isActiveTripRequested) {
                        "Active trip is armed and will run while tracking is active. This mode is temporary and higher battery."
                    } else {
                        "Active trip is off. Use it only for intentional trips that need higher accuracy."
                    },
                    style = MaterialTheme.typography.bodySmall
                )
                if (uiState.isActiveTripRequested || uiState.trackingState.isActiveTripRunning) {
                    Text(
                        text = ActiveTripUiMapper.remainingLabel(uiState.activeTripRemainingMillis)
                            ?: "Remaining time unavailable",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    text = ActiveTripUiMapper.timeoutPolicyLabel(),
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedButton(
                    onClick = onStartTracking,
                    enabled = uiState.canStartTracking,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Start tracking")
                }
                if (!uiState.canStartTracking) {
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
                    enabled = uiState.canStartTracking && !uiState.trackingState.isActiveTripRunning,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Start active trip (2h)")
                }
                OutlinedButton(
                    onClick = onStopActiveTrip,
                    enabled = uiState.isActiveTripRequested || uiState.trackingState.isActiveTripRunning,
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

        uiState.exportMessage?.let {
            Text(it, style = MaterialTheme.typography.bodySmall)
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
private fun SelectedVisitCard(visit: VisitSegment) {
    val formatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Selected Place", style = MaterialTheme.typography.titleMedium)
            Text(
                "Lat/Lng: ${"%.5f".format(Locale.getDefault(), visit.latitude)}, ${"%.5f".format(Locale.getDefault(), visit.longitude)}"
            )
            Text("Arrival: ${formatter.format(Date(visit.arrivalEpochMillis))}")
            Text("Departure: ${formatter.format(Date(visit.departureEpochMillis))}")
            Text("Duration: ${formatDuration(visit.durationMillis)}")
            Text("Points in cluster: ${visit.pointCount}")
        }
    }
}

@Composable
private fun PlacesVisitedList(
    visits: List<VisitSegment>,
    selectedVisitIndex: Int?,
    onSelectVisit: (Int) -> Unit
) {
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
                    OutlinedButton(
                        onClick = { onSelectVisit(index) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                if (selectedVisitIndex == index) "Place ${index + 1} (Selected)" else "Place ${index + 1}"
                            )
                            Text(
                                "Lat/Lng: ${"%.5f".format(Locale.getDefault(), visit.latitude)}, ${"%.5f".format(Locale.getDefault(), visit.longitude)}"
                            )
                            Text("Arrival: ${formatter.format(Date(visit.arrivalEpochMillis))}")
                            Text("Departure: ${formatter.format(Date(visit.departureEpochMillis))}")
                            Text("Duration: ${formatDuration(visit.durationMillis)}")
                            Text("Points: ${visit.pointCount}")
                        }
                    }
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
        "Last 24h" to TimelineOption.LAST_24_HOURS,
        "Last 7d" to TimelineOption.LAST_7_DAYS,
        "Last 30d" to TimelineOption.LAST_30_DAYS,
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
        Text("Custom start", style = MaterialTheme.typography.bodySmall)
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
                showTimePicker(
                    context = context,
                    initialMillis = startMillis
                ) { hour, minute ->
                    onStartChanged(updateTimeComponent(startMillis, hour, minute))
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start time: ${formatTime(startMillis)}")
        }

        Text("Custom end", style = MaterialTheme.typography.bodySmall)
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
        OutlinedButton(
            onClick = {
                showTimePicker(
                    context = context,
                    initialMillis = endMillis
                ) { hour, minute ->
                    onEndChanged(updateTimeComponent(endMillis, hour, minute))
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("End time: ${formatTime(endMillis)}")
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
            val existing = Calendar.getInstance().apply { timeInMillis = initialMillis }
            val selected = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                set(Calendar.HOUR_OF_DAY, existing.get(Calendar.HOUR_OF_DAY))
                set(Calendar.MINUTE, existing.get(Calendar.MINUTE))
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

private fun showTimePicker(
    context: android.content.Context,
    initialMillis: Long,
    onSelected: (hourOfDay: Int, minute: Int) -> Unit
) {
    val calendar = Calendar.getInstance().apply { timeInMillis = initialMillis }
    TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            onSelected(hourOfDay, minute)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    ).show()
}

private fun updateTimeComponent(
    baseEpochMillis: Long,
    hourOfDay: Int,
    minute: Int
): Long {
    return Calendar.getInstance().apply {
        timeInMillis = baseEpochMillis
        set(Calendar.HOUR_OF_DAY, hourOfDay)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun formatDate(epochMillis: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return formatter.format(Date(epochMillis))
}

private fun formatTime(epochMillis: Long): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
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

@Composable
private fun SummaryStatRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}
