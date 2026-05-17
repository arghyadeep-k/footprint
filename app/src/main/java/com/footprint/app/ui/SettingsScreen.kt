package com.footprint.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.footprint.app.location.TrackingMode

@Composable
fun SettingsScreen(
    selectedMode: TrackingMode,
    onSelectMode: (TrackingMode) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Tracking Settings", style = MaterialTheme.typography.headlineMedium)

        Text(
            text = "Choose your default background tracking mode. Active trip tracking is started separately from Home and uses higher battery.",
            style = MaterialTheme.typography.bodyMedium
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(TrackingMode.LOW_POWER, TrackingMode.BALANCED).forEach { mode ->
                    ModeRow(
                        mode = mode,
                        selected = selectedMode == mode,
                        onSelect = { onSelectMode(mode) }
                    )
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Active trip is temporary high-accuracy tracking for intentional trips and automatically times out after about 2 hours.",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 14.dp),
            onClick = onBack
        ) {
            Text("Back")
        }
    }
}

@Composable
private fun ModeRow(
    mode: TrackingMode,
    selected: Boolean,
    onSelect: () -> Unit
) {
    OutlinedButton(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth()
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(
            text = mode.notificationLabel,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
