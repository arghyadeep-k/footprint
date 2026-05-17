package com.footprint.app.ui

import android.os.Build
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PermissionStateResolverTest {

    @Test
    fun foreground_notRequested() {
        val state = PermissionStateResolver.resolve(
            sdkInt = Build.VERSION_CODES.UPSIDE_DOWN_CAKE,
            foregroundPermissionRequested = false,
            hasForegroundPermission = false,
            shouldShowForegroundRationale = false,
            hasBackgroundPermission = false,
            hasNotificationPermission = false
        )

        assertEquals(ForegroundPermissionStatus.NOT_REQUESTED, state.foregroundStatus)
    }

    @Test
    fun foreground_denied() {
        val state = PermissionStateResolver.resolve(
            sdkInt = Build.VERSION_CODES.UPSIDE_DOWN_CAKE,
            foregroundPermissionRequested = true,
            hasForegroundPermission = false,
            shouldShowForegroundRationale = true,
            hasBackgroundPermission = false,
            hasNotificationPermission = false
        )

        assertEquals(ForegroundPermissionStatus.DENIED, state.foregroundStatus)
    }

    @Test
    fun foreground_permanentlyDenied() {
        val state = PermissionStateResolver.resolve(
            sdkInt = Build.VERSION_CODES.UPSIDE_DOWN_CAKE,
            foregroundPermissionRequested = true,
            hasForegroundPermission = false,
            shouldShowForegroundRationale = false,
            hasBackgroundPermission = false,
            hasNotificationPermission = false
        )

        assertEquals(ForegroundPermissionStatus.PERMANENTLY_DENIED, state.foregroundStatus)
    }

    @Test
    fun foreground_granted() {
        val state = PermissionStateResolver.resolve(
            sdkInt = Build.VERSION_CODES.UPSIDE_DOWN_CAKE,
            foregroundPermissionRequested = true,
            hasForegroundPermission = true,
            shouldShowForegroundRationale = false,
            hasBackgroundPermission = false,
            hasNotificationPermission = false
        )

        assertEquals(ForegroundPermissionStatus.GRANTED, state.foregroundStatus)
    }

    @Test
    fun android10Plus_requiresBackgroundPermission() {
        val state = PermissionStateResolver.resolve(
            sdkInt = Build.VERSION_CODES.Q,
            foregroundPermissionRequested = true,
            hasForegroundPermission = true,
            shouldShowForegroundRationale = false,
            hasBackgroundPermission = false,
            hasNotificationPermission = true
        )

        assertTrue(state.requiresBackgroundPermission)
        assertFalse(state.allRequiredPermissionsReady)
    }

    @Test
    fun android13Plus_requiresNotificationPermission() {
        val state = PermissionStateResolver.resolve(
            sdkInt = Build.VERSION_CODES.TIRAMISU,
            foregroundPermissionRequested = true,
            hasForegroundPermission = true,
            shouldShowForegroundRationale = false,
            hasBackgroundPermission = true,
            hasNotificationPermission = false
        )

        assertTrue(state.requiresNotificationPermission)
        assertFalse(state.allRequiredPermissionsReady)
    }

    @Test
    fun allRequiredPermissionsReady_whenForegroundBackgroundAndNotificationGranted() {
        val state = PermissionStateResolver.resolve(
            sdkInt = Build.VERSION_CODES.UPSIDE_DOWN_CAKE,
            foregroundPermissionRequested = true,
            hasForegroundPermission = true,
            shouldShowForegroundRationale = false,
            hasBackgroundPermission = true,
            hasNotificationPermission = true
        )

        assertTrue(state.allRequiredPermissionsReady)
    }
}
