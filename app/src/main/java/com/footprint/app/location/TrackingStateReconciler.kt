package com.footprint.app.location

object TrackingStateReconciler {
    const val STALE_RUNNING_REASON = "Tracking was marked running, but service was not active. State was reconciled."
    const val PERMISSION_MISSING_REASON = "Tracking permissions are missing. State was reconciled."

    fun resolveStartupState(
        persistedState: TrackingState,
        isServiceRunning: Boolean,
        hasRequiredPermissions: Boolean
    ): TrackingState {
        return when {
            !hasRequiredPermissions -> {
                TrackingState(
                    status = TrackingState.Status.PERMISSION_MISSING,
                    effectiveMode = null,
                    isActiveTripRunning = false,
                    errorMessage = PERMISSION_MISSING_REASON
                )
            }

            persistedState.status == TrackingState.Status.RUNNING && !isServiceRunning -> {
                TrackingState(
                    status = TrackingState.Status.STOPPED,
                    effectiveMode = null,
                    isActiveTripRunning = false,
                    errorMessage = STALE_RUNNING_REASON
                )
            }

            isServiceRunning && persistedState.status != TrackingState.Status.RUNNING -> {
                TrackingState(
                    status = TrackingState.Status.RUNNING,
                    effectiveMode = persistedState.effectiveMode,
                    isActiveTripRunning = persistedState.isActiveTripRunning,
                    errorMessage = null
                )
            }

            else -> persistedState
        }
    }

    fun reconcile(
        persistedState: TrackingState,
        isServiceRunning: Boolean
    ): TrackingState {
        return resolveStartupState(
            persistedState = persistedState,
            isServiceRunning = isServiceRunning,
            hasRequiredPermissions = true
        )
    }
}
