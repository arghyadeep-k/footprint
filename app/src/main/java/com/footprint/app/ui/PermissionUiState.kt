package com.footprint.app.ui

import android.os.Build

data class PermissionUiState(
    val foregroundStatus: ForegroundPermissionStatus,
    val isBackgroundGranted: Boolean,
    val isNotificationGranted: Boolean
) {
    val isForegroundGranted: Boolean
        get() = foregroundStatus == ForegroundPermissionStatus.GRANTED

    val requiresNotificationPermission: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    val requiresBackgroundPermission: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

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
