package com.footprint.app.ui

import android.os.Build

data class PermissionUiState(
    val foregroundStatus: ForegroundPermissionStatus,
    val isBackgroundGranted: Boolean,
    val isNotificationGranted: Boolean,
    val sdkInt: Int = Build.VERSION.SDK_INT
) {
    val isForegroundGranted: Boolean
        get() = foregroundStatus == ForegroundPermissionStatus.GRANTED

    val requiresNotificationPermission: Boolean
        get() = sdkInt >= Build.VERSION_CODES.TIRAMISU

    val requiresBackgroundPermission: Boolean
        get() = sdkInt >= Build.VERSION_CODES.Q

    val allRequiredPermissionsReady: Boolean
        get() = isForegroundGranted &&
            (!requiresBackgroundPermission || isBackgroundGranted) &&
            (!requiresNotificationPermission || isNotificationGranted)
}

enum class ForegroundPermissionStatus {
    NOT_REQUESTED,
    GRANTED,
    DENIED,
    PERMANENTLY_DENIED
}

object PermissionUiTextMapper {
    fun foregroundStatusLabel(status: ForegroundPermissionStatus): String {
        return when (status) {
            ForegroundPermissionStatus.NOT_REQUESTED -> "Foreground location not requested"
            ForegroundPermissionStatus.GRANTED -> "Foreground location granted"
            ForegroundPermissionStatus.DENIED -> "Foreground location denied"
            ForegroundPermissionStatus.PERMANENTLY_DENIED -> "Foreground location permanently denied"
        }
    }
}

object PermissionStateResolver {
    fun resolve(
        sdkInt: Int,
        foregroundPermissionRequested: Boolean,
        hasForegroundPermission: Boolean,
        shouldShowForegroundRationale: Boolean,
        hasBackgroundPermission: Boolean,
        hasNotificationPermission: Boolean
    ): PermissionUiState {
        val foregroundStatus = when {
            hasForegroundPermission -> ForegroundPermissionStatus.GRANTED
            !foregroundPermissionRequested -> ForegroundPermissionStatus.NOT_REQUESTED
            shouldShowForegroundRationale -> ForegroundPermissionStatus.DENIED
            else -> ForegroundPermissionStatus.PERMANENTLY_DENIED
        }

        return PermissionUiState(
            foregroundStatus = foregroundStatus,
            isBackgroundGranted = hasBackgroundPermission,
            isNotificationGranted = hasNotificationPermission,
            sdkInt = sdkInt
        )
    }
}
