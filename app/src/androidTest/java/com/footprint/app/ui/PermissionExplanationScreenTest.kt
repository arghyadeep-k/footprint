package com.footprint.app.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PermissionExplanationScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun backgroundPermissionStep_isShownOnlyAfterForegroundGranted() {
        composeRule.setContent {
            PermissionExplanationScreen(
                permissionState = PermissionUiState(
                    foregroundStatus = ForegroundPermissionStatus.NOT_REQUESTED,
                    isBackgroundGranted = false,
                    isNotificationGranted = false,
                    sdkInt = 34
                ),
                onRequestForegroundPermission = {},
                onRequestNotificationPermission = {},
                onOpenBackgroundLocationSettings = {},
                onOpenAppSettings = {},
                onContinue = {}
            )
        }

        composeRule.onAllNodesWithText("Step 2: Background location").assertCountEquals(0)

        composeRule.setContent {
            PermissionExplanationScreen(
                permissionState = PermissionUiState(
                    foregroundStatus = ForegroundPermissionStatus.GRANTED,
                    isBackgroundGranted = false,
                    isNotificationGranted = false,
                    sdkInt = 34
                ),
                onRequestForegroundPermission = {},
                onRequestNotificationPermission = {},
                onOpenBackgroundLocationSettings = {},
                onOpenAppSettings = {},
                onContinue = {}
            )
        }

        composeRule.onAllNodesWithText("Step 2: Background location").assertCountEquals(1)
    }

    @Test
    fun notificationStep_stagedForAndroid13PlusOnly() {
        composeRule.setContent {
            PermissionExplanationScreen(
                permissionState = PermissionUiState(
                    foregroundStatus = ForegroundPermissionStatus.GRANTED,
                    isBackgroundGranted = true,
                    isNotificationGranted = false,
                    sdkInt = 33
                ),
                onRequestForegroundPermission = {},
                onRequestNotificationPermission = {},
                onOpenBackgroundLocationSettings = {},
                onOpenAppSettings = {},
                onContinue = {}
            )
        }
        composeRule.onAllNodesWithText("Step 3: Notification permission (Android 13+)").assertCountEquals(1)

        composeRule.setContent {
            PermissionExplanationScreen(
                permissionState = PermissionUiState(
                    foregroundStatus = ForegroundPermissionStatus.GRANTED,
                    isBackgroundGranted = true,
                    isNotificationGranted = false,
                    sdkInt = 32
                ),
                onRequestForegroundPermission = {},
                onRequestNotificationPermission = {},
                onOpenBackgroundLocationSettings = {},
                onOpenAppSettings = {},
                onContinue = {}
            )
        }
        composeRule.onAllNodesWithText("Step 3: Notification permission (Android 13+)").assertCountEquals(0)
    }

    @Test
    fun continueDisabledUntilAllRequiredPermissionsReady() {
        composeRule.setContent {
            PermissionExplanationScreen(
                permissionState = PermissionUiState(
                    foregroundStatus = ForegroundPermissionStatus.GRANTED,
                    isBackgroundGranted = false,
                    isNotificationGranted = false,
                    sdkInt = 34
                ),
                onRequestForegroundPermission = {},
                onRequestNotificationPermission = {},
                onOpenBackgroundLocationSettings = {},
                onOpenAppSettings = {},
                onContinue = {}
            )
        }

        composeRule.onNodeWithText("Continue to home").assertIsNotEnabled()

        composeRule.setContent {
            PermissionExplanationScreen(
                permissionState = PermissionUiState(
                    foregroundStatus = ForegroundPermissionStatus.GRANTED,
                    isBackgroundGranted = true,
                    isNotificationGranted = true,
                    sdkInt = 34
                ),
                onRequestForegroundPermission = {},
                onRequestNotificationPermission = {},
                onOpenBackgroundLocationSettings = {},
                onOpenAppSettings = {},
                onContinue = {}
            )
        }

        composeRule.onNodeWithText("Continue to home").assertIsEnabled()
    }

    @Test
    fun foregroundDenied_showsDeniedStatusLabel() {
        composeRule.setContent {
            PermissionExplanationScreen(
                permissionState = PermissionUiState(
                    foregroundStatus = ForegroundPermissionStatus.DENIED,
                    isBackgroundGranted = false,
                    isNotificationGranted = false,
                    sdkInt = 34
                ),
                onRequestForegroundPermission = {},
                onRequestNotificationPermission = {},
                onOpenBackgroundLocationSettings = {},
                onOpenAppSettings = {},
                onContinue = {}
            )
        }

        composeRule.onNodeWithText("Status: Foreground location denied").assertExists()
        composeRule.onAllNodesWithText("Foreground permission blocked").assertCountEquals(0)
    }

    @Test
    fun foregroundPermanentlyDenied_showsAppSettingsGuidance() {
        var openedAppSettings = 0
        composeRule.setContent {
            PermissionExplanationScreen(
                permissionState = PermissionUiState(
                    foregroundStatus = ForegroundPermissionStatus.PERMANENTLY_DENIED,
                    isBackgroundGranted = false,
                    isNotificationGranted = false,
                    sdkInt = 34
                ),
                onRequestForegroundPermission = {},
                onRequestNotificationPermission = {},
                onOpenBackgroundLocationSettings = {},
                onOpenAppSettings = { openedAppSettings++ },
                onContinue = {}
            )
        }

        composeRule.onNodeWithText("Foreground permission blocked").assertExists()
        composeRule.onNodeWithText("Open app settings").performClick()
        composeRule.runOnIdle {
            assertEquals(1, openedAppSettings)
        }
    }

    @Test
    fun foregroundGranted_showsBackgroundGuidance_notBackgroundPromptWithForeground() {
        composeRule.setContent {
            PermissionExplanationScreen(
                permissionState = PermissionUiState(
                    foregroundStatus = ForegroundPermissionStatus.GRANTED,
                    isBackgroundGranted = false,
                    isNotificationGranted = false,
                    sdkInt = 34
                ),
                onRequestForegroundPermission = {},
                onRequestNotificationPermission = {},
                onOpenBackgroundLocationSettings = {},
                onOpenAppSettings = {},
                onContinue = {}
            )
        }

        composeRule.onNodeWithText("Status: Foreground location granted").assertExists()
        composeRule.onNodeWithText("Step 2: Background location").assertExists()
        composeRule.onNodeWithText("Open Android settings for background location").assertExists()
    }
}
