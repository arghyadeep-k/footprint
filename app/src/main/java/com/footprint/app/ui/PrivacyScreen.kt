package com.footprint.app.ui

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
import androidx.compose.ui.unit.dp

@Composable
fun PrivacyScreen(
    actionMessage: String?,
    isWorking: Boolean,
    onPauseTracking: () -> Unit,
    onDeleteAllHistory: () -> Unit,
    onDeleteHistoryOlderThan30Days: () -> Unit,
    onBack: () -> Unit
) {
    var pendingAction by remember { mutableStateOf<PrivacyDeleteAction?>(null) }

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
                    }
                )
            }
        )
    }
}

private enum class PrivacyDeleteAction {
    DeleteAll,
    DeleteOlderThan30Days
}
