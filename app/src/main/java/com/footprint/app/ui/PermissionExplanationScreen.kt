package com.footprint.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PermissionExplanationScreen(
    permissionState: PermissionUiState,
    onRequestForegroundPermission: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onOpenBackgroundLocationSettings: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Location Access",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Footprint uses location history to draw your travel line on a map and help you review where you have been over time.",
            style = MaterialTheme.typography.bodyLarge
        )

        PermissionCard(
            title = "Step 1: Foreground location",
            body = "Allow precise or approximate location first. Footprint requests foreground location before any background access.",
            buttonText = if (permissionState.isForegroundGranted) {
                "Foreground location granted"
            } else {
                "Request foreground location permission"
            },
            onClick = onRequestForegroundPermission,
            enabled = !permissionState.isForegroundGranted
        )
        Text(
            text = "Status: ${PermissionUiTextMapper.foregroundStatusLabel(permissionState.foregroundStatus)}",
            style = MaterialTheme.typography.bodySmall
        )

        if (permissionState.foregroundStatus == ForegroundPermissionStatus.PERMANENTLY_DENIED) {
            PermissionCard(
                title = "Foreground permission blocked",
                body = "Android is currently blocking the foreground location prompt. Open app settings to enable location access.",
                buttonText = "Open app settings",
                onClick = onOpenAppSettings,
                enabled = true
            )
        }

        if (permissionState.isForegroundGranted) {
            PermissionCard(
                title = "Step 2: Background location",
                body = backgroundPermissionBodyText(),
                buttonText = if (permissionState.isBackgroundGranted) {
                    "Background location granted"
                } else {
                    "Open Android settings for background location"
                },
                onClick = onOpenBackgroundLocationSettings,
                enabled = !permissionState.isBackgroundGranted
            )
        }

        if (permissionState.requiresNotificationPermission) {
            PermissionCard(
                title = "Step 3: Notification permission (Android 13+)",
                body = "Footprint shows a persistent tracking notification while recording travel history. Android 13+ requires notification permission for this.",
                buttonText = if (permissionState.isNotificationGranted) {
                    "Notification permission granted"
                } else {
                    "Request notification permission"
                },
                onClick = onRequestNotificationPermission,
                enabled = !permissionState.isNotificationGranted
            )
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Permission summary", style = MaterialTheme.typography.titleMedium)
                Text("Foreground: ${PermissionUiTextMapper.foregroundStatusLabel(permissionState.foregroundStatus)}")
                if (permissionState.requiresBackgroundPermission) {
                    Text(
                        if (permissionState.isBackgroundGranted) {
                            "Background location granted"
                        } else {
                            "Background location not granted"
                        }
                    )
                }
                if (permissionState.requiresNotificationPermission) {
                    Text(
                        if (permissionState.isNotificationGranted) {
                            "Notification permission granted"
                        } else {
                            "Notification permission not granted"
                        }
                    )
                }
                if (permissionState.allRequiredPermissionsReady) {
                    Text(
                        text = "All required permissions are ready.",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 14.dp),
            onClick = onContinue,
            enabled = permissionState.allRequiredPermissionsReady
        ) {
            Text("Continue to home")
        }
    }
}

private fun backgroundPermissionBodyText(): String {
    return when {
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE ->
            "On Android 14+, location tracking should run with a clear ongoing notification. Enable background location in settings so Footprint can continue recording travel routes when the app is not open."
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q ->
            "On Android 10+, background location is managed in Android settings. Enable it there so Footprint can keep recording travel routes when the app is not open."
        else ->
            "Background location is available through foreground location behavior on this Android version."
    }
}

@Composable
private fun PermissionCard(
    title: String,
    body: String,
    buttonText: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = body, style = MaterialTheme.typography.bodyMedium)
            Button(
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(buttonText)
            }
        }
    }
}
