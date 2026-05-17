package com.footprint.app.location

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackingStateReconcilerTest {

    @Test
    fun persistedRunning_andServiceNotRunning_resolvesStopped() {
        val persisted = TrackingState(
            status = TrackingState.Status.RUNNING,
            effectiveMode = TrackingMode.BALANCED,
            isActiveTripRunning = true
        )

        val reconciled = TrackingStateReconciler.resolveStartupState(
            persistedState = persisted,
            isServiceRunning = false,
            hasRequiredPermissions = true
        )

        assertEquals(TrackingState.Status.STOPPED, reconciled.status)
        assertEquals(null, reconciled.effectiveMode)
        assertTrue(reconciled.errorMessage?.contains("reconciled", ignoreCase = true) == true)
    }

    @Test
    fun persistedRunning_andPermissionMissing_resolvesPermissionMissing() {
        val persisted = TrackingState(
            status = TrackingState.Status.RUNNING,
            effectiveMode = TrackingMode.BALANCED
        )

        val reconciled = TrackingStateReconciler.resolveStartupState(
            persistedState = persisted,
            isServiceRunning = true,
            hasRequiredPermissions = false
        )

        assertEquals(TrackingState.Status.PERMISSION_MISSING, reconciled.status)
        assertEquals(null, reconciled.effectiveMode)
    }

    @Test
    fun persistedRunning_andServiceRunning_resolvesRunning() {
        val persisted = TrackingState(
            status = TrackingState.Status.RUNNING,
            effectiveMode = TrackingMode.BALANCED
        )

        val reconciled = TrackingStateReconciler.resolveStartupState(
            persistedState = persisted,
            isServiceRunning = true,
            hasRequiredPermissions = true
        )

        assertEquals(TrackingState.Status.RUNNING, reconciled.status)
        assertEquals(TrackingMode.BALANCED, reconciled.effectiveMode)
    }

    @Test
    fun persistedStopped_resolvesStopped() {
        val persisted = TrackingState(
            status = TrackingState.Status.STOPPED
        )

        val reconciled = TrackingStateReconciler.resolveStartupState(
            persistedState = persisted,
            isServiceRunning = false,
            hasRequiredPermissions = true
        )

        assertEquals(TrackingState.Status.STOPPED, reconciled.status)
    }
}
