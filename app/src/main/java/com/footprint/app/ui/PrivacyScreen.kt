package com.footprint.app.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.footprint.app.data.RetentionPolicy
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun PrivacyScreen(
    actionMessage: String?,
    isWorking: Boolean,
    retentionPolicy: RetentionPolicy,
    onPauseTracking: () -> Unit,
    onDeleteAllHistory: () -> Unit,
    onDeleteHistoryOlderThan30Days: () -> Unit,
    onDeleteHistoryInRange: (Long, Long) -> Unit,
    onImportCsv: () -> Unit,
    onSetRetentionPolicy: (RetentionPolicy) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var pendingAction by remember { mutableStateOf<PrivacyDeleteAction?>(null) }
    var customStartMillis by remember {
        mutableStateOf(System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000))
    }
    var customEndMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Privacy & Data", style = MaterialTheme.typography.headlineMedium)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Footprint stores your location history locally on this device.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "You can disable background location permission at any time from Android Settings.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Retention and deletion actions are local-only and happen on this device.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onPauseTracking,
                    enabled = !isWorking,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text("Pause tracking")
                }

                OutlinedButton(
                    onClick = { pendingAction = PrivacyDeleteAction.DeleteOlderThan30Days },
                    enabled = !isWorking,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text("Delete history older than 30 days")
                }

                OutlinedButton(
                    onClick = { pendingAction = PrivacyDeleteAction.DeleteAll },
                    enabled = !isWorking,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text("Delete all location history")
                }
                OutlinedButton(
                    onClick = onImportCsv,
                    enabled = !isWorking,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text("Import CSV history")
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Delete by date range", style = MaterialTheme.typography.titleMedium)
                OutlinedButton(
                    onClick = {
                        showDatePicker(context, customStartMillis) { selected ->
                            customStartMillis = preserveTimeComponent(customStartMillis, selected)
                        }
                    },
                    enabled = !isWorking,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Start date: ${formatDate(customStartMillis)}")
                }
                OutlinedButton(
                    onClick = {
                        showTimePicker(context, customStartMillis) { hour, minute ->
                            customStartMillis = updateTimeComponent(customStartMillis, hour, minute)
                        }
                    },
                    enabled = !isWorking,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Start time: ${formatTime(customStartMillis)}")
                }
                OutlinedButton(
                    onClick = {
                        showDatePicker(context, customEndMillis) { selected ->
                            customEndMillis = preserveTimeComponent(customEndMillis, selected)
                        }
                    },
                    enabled = !isWorking,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("End date: ${formatDate(customEndMillis)}")
                }
                OutlinedButton(
                    onClick = {
                        showTimePicker(context, customEndMillis) { hour, minute ->
                            customEndMillis = updateTimeComponent(customEndMillis, hour, minute)
                        }
                    },
                    enabled = !isWorking,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("End time: ${formatTime(customEndMillis)}")
                }
                OutlinedButton(
                    onClick = {
                        pendingAction = PrivacyDeleteAction.DeleteRange(customStartMillis, customEndMillis)
                    },
                    enabled = !isWorking,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Delete selected date range")
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Automatic retention", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Stored locally on-device. Choose how long history is kept.",
                    style = MaterialTheme.typography.bodySmall
                )
                RetentionOptionRow(
                    label = "Keep forever",
                    selected = retentionPolicy == RetentionPolicy.KEEP_FOREVER,
                    enabled = !isWorking,
                    onClick = { onSetRetentionPolicy(RetentionPolicy.KEEP_FOREVER) }
                )
                RetentionOptionRow(
                    label = "Delete older than 30 days",
                    selected = retentionPolicy == RetentionPolicy.DELETE_OLDER_THAN_30_DAYS,
                    enabled = !isWorking,
                    onClick = { onSetRetentionPolicy(RetentionPolicy.DELETE_OLDER_THAN_30_DAYS) }
                )
                RetentionOptionRow(
                    label = "Delete older than 90 days",
                    selected = retentionPolicy == RetentionPolicy.DELETE_OLDER_THAN_90_DAYS,
                    enabled = !isWorking,
                    onClick = { onSetRetentionPolicy(RetentionPolicy.DELETE_OLDER_THAN_90_DAYS) }
                )
                RetentionOptionRow(
                    label = "Delete older than 1 year",
                    selected = retentionPolicy == RetentionPolicy.DELETE_OLDER_THAN_1_YEAR,
                    enabled = !isWorking,
                    onClick = { onSetRetentionPolicy(RetentionPolicy.DELETE_OLDER_THAN_1_YEAR) }
                )
            }
        }

        OutlinedButton(
            onClick = onBack,
            enabled = !isWorking,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Text("Back")
        }

        if (actionMessage != null) {
            Text(
                text = actionMessage,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    val action = pendingAction
    if (action != null) {
        AlertDialog(
            onDismissRequest = { pendingAction = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        when (action) {
                            PrivacyDeleteAction.DeleteAll -> onDeleteAllHistory()
                            PrivacyDeleteAction.DeleteOlderThan30Days -> onDeleteHistoryOlderThan30Days()
                            is PrivacyDeleteAction.DeleteRange -> {
                                onDeleteHistoryInRange(action.startEpochMillis, action.endEpochMillis)
                            }
                        }
                        pendingAction = null
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingAction = null }) {
                    Text("Cancel")
                }
            },
            title = { Text("Confirm deletion") },
            text = {
                Text(
                    when (action) {
                        PrivacyDeleteAction.DeleteAll -> "Delete all stored location history from this device?"
                        PrivacyDeleteAction.DeleteOlderThan30Days -> "Delete all location history older than 30 days?"
                        is PrivacyDeleteAction.DeleteRange -> {
                            "Delete location history from ${formatDateTime(action.startEpochMillis)} to " +
                                "${formatDateTime(action.endEpochMillis)}?"
                        }
                    }
                )
            }
        )
    }
}

@Composable
private fun RetentionOptionRow(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(if (selected) "$label (Selected)" else label)
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

private fun showTimePicker(
    context: android.content.Context,
    initialMillis: Long,
    onSelected: (hour: Int, minute: Int) -> Unit
) {
    val calendar = Calendar.getInstance().apply { timeInMillis = initialMillis }
    TimePickerDialog(
        context,
        { _, hourOfDay, minute -> onSelected(hourOfDay, minute) },
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

private fun preserveTimeComponent(
    baseEpochMillis: Long,
    selectedDateEpochMillis: Long
): Long {
    val base = Calendar.getInstance().apply { timeInMillis = baseEpochMillis }
    val date = Calendar.getInstance().apply { timeInMillis = selectedDateEpochMillis }
    date.set(Calendar.HOUR_OF_DAY, base.get(Calendar.HOUR_OF_DAY))
    date.set(Calendar.MINUTE, base.get(Calendar.MINUTE))
    date.set(Calendar.SECOND, 0)
    date.set(Calendar.MILLISECOND, 0)
    return date.timeInMillis
}

private fun formatDate(epochMillis: Long): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(epochMillis))
}

private fun formatTime(epochMillis: Long): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(epochMillis))
}

private fun formatDateTime(epochMillis: Long): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(epochMillis))
}

private sealed interface PrivacyDeleteAction {
    data object DeleteAll : PrivacyDeleteAction
    data object DeleteOlderThan30Days : PrivacyDeleteAction
    data class DeleteRange(val startEpochMillis: Long, val endEpochMillis: Long) : PrivacyDeleteAction
}
