package com.footprint.app.location

object TrackingStateUiMapper {
    fun statusLabel(state: TrackingState): String {
        return when (state.status) {
            TrackingState.Status.STOPPED -> "Tracking is paused"
            TrackingState.Status.RUNNING -> "Tracking is active"
            TrackingState.Status.PERMISSION_MISSING -> "Tracking cannot run: permission missing"
            TrackingState.Status.ERROR -> "Tracking error"
        }
    }

    fun effectiveModeLabel(state: TrackingState): String {
        return state.effectiveMode?.notificationLabel ?: "-"
    }
}
