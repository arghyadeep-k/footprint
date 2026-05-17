package com.footprint.app.ui.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.footprint.app.ui.ForegroundPermissionStatus
import com.footprint.app.ui.PermissionUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PermissionViewModel : ViewModel() {
    private val _permissionState = MutableStateFlow(
        PermissionUiState(
            foregroundStatus = ForegroundPermissionStatus.NOT_REQUESTED,
            isBackgroundGranted = false,
            isNotificationGranted = false
        )
    )
    val permissionState: StateFlow<PermissionUiState> = _permissionState.asStateFlow()

    private var foregroundPermissionRequested = false

    fun refreshFromSystem(context: Context, activity: ComponentActivity?) {
        val hasForeground = context.hasForegroundLocationPermission()
        val showRationale = activity?.let {
            it.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
                it.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
        } ?: false

        val foregroundStatus = when {
            hasForeground -> ForegroundPermissionStatus.GRANTED
            !foregroundPermissionRequested -> ForegroundPermissionStatus.NOT_REQUESTED
            showRationale -> ForegroundPermissionStatus.DENIED
            else -> ForegroundPermissionStatus.PERMANENTLY_DENIED
        }

        _permissionState.update {
            it.copy(
                foregroundStatus = foregroundStatus,
                isBackgroundGranted = context.hasBackgroundLocationPermission(),
                isNotificationGranted = context.hasNotificationPermission()
            )
        }
    }

    fun onForegroundPermissionRequested() {
        foregroundPermissionRequested = true
    }

    private fun Context.hasForegroundLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineGranted || coarseGranted
    }

    private fun Context.hasBackgroundLocationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return true
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun Context.hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }
}
