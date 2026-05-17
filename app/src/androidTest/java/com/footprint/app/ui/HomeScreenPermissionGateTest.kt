package com.footprint.app.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.footprint.app.timeline.TimelineDataUiState
import com.footprint.app.ui.viewmodel.HomeUiState
import org.junit.Rule
import org.junit.Test

class HomeScreenPermissionGateTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun startTrackingDisabled_andGuidanceVisible_whenPermissionsMissing() {
        composeRule.setContent {
            HomeScreen(
                uiState = HomeUiState(
                    canStartTracking = false,
                    timelineState = TimelineDataUiState.Empty
                ),
                onTimelineOptionSelected = {},
                onCustomStartChanged = {},
                onCustomEndChanged = {},
                onRetryLoad = {},
                onStartTracking = {},
                onStopTracking = {},
                onStartActiveTrip = {},
                onStopActiveTrip = {},
                onOpenSettings = {},
                onOpenPrivacy = {},
                onManagePermissions = {},
                onExportCsv = { "" },
                onExportGeoJson = { "" },
                onMarkExportResult = {},
                onVisitSelected = {}
            )
        }

        composeRule.onNodeWithText("Start tracking").assertIsNotEnabled()
        composeRule.onNodeWithText(
            "Tracking needs foreground location, background location, and notifications (Android 13+) to be ready."
        ).assertExists()
        composeRule.onNodeWithText("Review permissions").assertExists()
    }
}
