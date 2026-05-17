package com.footprint.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.footprint.app.data.LocationRepository
import com.footprint.app.data.ActiveTripSessionStore
import com.footprint.app.data.TrackingPreferencesStore
import com.footprint.app.data.TrackingRuntimeStateStore
import com.footprint.app.data.local.FootprintDatabaseProvider
import com.footprint.app.location.TrackingController
import com.footprint.app.location.TrackingMode
import com.footprint.app.location.TrackingState
import com.footprint.app.ui.AppRoutes
import com.footprint.app.ui.HomeScreen
import com.footprint.app.ui.ForegroundPermissionStatus
import com.footprint.app.ui.PermissionUiState
import com.footprint.app.ui.PermissionExplanationScreen
import com.footprint.app.ui.PrivacyScreen
import com.footprint.app.ui.SettingsScreen
import com.footprint.app.ui.theme.FootprintTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FootprintTheme {
                FootprintApp()
            }
        }
    }
}

@Composable
fun FootprintApp() {
    val context = LocalContext.current
    val navController = rememberNavController()

    val trackingController = remember { TrackingController(context.applicationContext) }
    val activeTripSessionStore = remember { ActiveTripSessionStore(context.applicationContext) }
    val trackingPreferencesStore = remember { TrackingPreferencesStore(context.applicationContext) }
    val trackingRuntimeStateStore = remember { TrackingRuntimeStateStore(context.applicationContext) }
    val locationRepository = remember {
        val db = FootprintDatabaseProvider.getDatabase(context.applicationContext)
        LocationRepository(db.locationPointDao())
    }
    val coroutineScope = rememberCoroutineScope()

    val selectedTrackingMode by trackingPreferencesStore.trackingModeFlow.collectAsState(
        initial = TrackingMode.BALANCED
    )

    var dataRefreshKey by rememberSaveable { mutableIntStateOf(0) }

    val trackingState by trackingRuntimeStateStore.trackingStateFlow.collectAsState(
        initial = TrackingState.stopped()
    )
    val activeTripSession by activeTripSessionStore.sessionFlow.collectAsState(
        initial = com.footprint.app.data.ActiveTripSession(isActive = false, startedAtEpochMillis = null)
    )
    var foregroundPermissionRequested by rememberSaveable { mutableStateOf(false) }
    var hasForegroundPermission by rememberSaveable { mutableStateOf(context.hasForegroundLocationPermission()) }
    var shouldShowForegroundRationale by rememberSaveable {
        mutableStateOf(context.shouldShowForegroundPermissionRationale())
    }
    var hasBackgroundPermission by rememberSaveable { mutableStateOf(context.hasBackgroundLocationPermission()) }
    var hasNotificationPermission by rememberSaveable { mutableStateOf(context.hasNotificationPermission()) }

    val foregroundStatus = remember(
        hasForegroundPermission,
        foregroundPermissionRequested,
        shouldShowForegroundRationale
    ) {
        when {
            hasForegroundPermission -> ForegroundPermissionStatus.GRANTED
            !foregroundPermissionRequested -> ForegroundPermissionStatus.NOT_REQUESTED
            shouldShowForegroundRationale -> ForegroundPermissionStatus.DENIED
            else -> ForegroundPermissionStatus.PERMANENTLY_DENIED
        }
    }

    val permissionState = remember(
        foregroundStatus,
        hasBackgroundPermission,
        hasNotificationPermission
    ) {
        PermissionUiState(
            foregroundStatus = foregroundStatus,
            isBackgroundGranted = hasBackgroundPermission,
            isNotificationGranted = hasNotificationPermission
        )
    }

    val foregroundPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        foregroundPermissionRequested = true
        hasForegroundPermission = context.hasForegroundLocationPermission()
        shouldShowForegroundRationale = context.shouldShowForegroundPermissionRationale()
        hasBackgroundPermission = context.hasBackgroundLocationPermission()
        hasNotificationPermission = context.hasNotificationPermission()
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        hasNotificationPermission = context.hasNotificationPermission()
    }

    NavHost(
        navController = navController,
        startDestination = AppRoutes.PERMISSIONS
    ) {
        composable(AppRoutes.PERMISSIONS) {
            hasForegroundPermission = context.hasForegroundLocationPermission()
            shouldShowForegroundRationale = context.shouldShowForegroundPermissionRationale()
            hasBackgroundPermission = context.hasBackgroundLocationPermission()
            hasNotificationPermission = context.hasNotificationPermission()

            PermissionExplanationScreen(
                permissionState = permissionState,
                onRequestForegroundPermission = {
                    foregroundPermissionRequested = true
                    foregroundPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    )
                },
                onRequestNotificationPermission = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
                onOpenBackgroundLocationSettings = {
                    context.openAppLocationSettings()
                },
                onOpenAppSettings = {
                    context.openAppLocationSettings()
                },
                onContinue = {
                    navController.navigate(AppRoutes.HOME) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(AppRoutes.HOME) {
            HomeScreen(
                selectedTrackingMode = selectedTrackingMode,
                trackingState = trackingState,
                isActiveTripRequested = activeTripSession.isActive,
                dataRefreshKey = dataRefreshKey,
                locationRepository = locationRepository,
                onStartTracking = {
                    if (permissionState.allRequiredPermissionsReady) {
                        trackingController.startTracking()
                    } else {
                        navController.navigate(AppRoutes.PERMISSIONS) {
                            launchSingleTop = true
                        }
                    }
                },
                onStopTracking = {
                    trackingController.stopTracking()
                },
                onStartActiveTrip = {
                    if (permissionState.allRequiredPermissionsReady) {
                        trackingController.startActiveTrip()
                    } else {
                        navController.navigate(AppRoutes.PERMISSIONS) {
                            launchSingleTop = true
                        }
                    }
                },
                onStopActiveTrip = {
                    trackingController.stopActiveTrip()
                },
                onOpenSettings = { navController.navigate(AppRoutes.SETTINGS) },
                onOpenPrivacy = { navController.navigate(AppRoutes.PRIVACY) },
                onManagePermissions = {
                    navController.navigate(AppRoutes.PERMISSIONS)
                },
                canStartTracking = permissionState.allRequiredPermissionsReady
            )
        }

        composable(AppRoutes.SETTINGS) {
            SettingsScreen(
                selectedMode = selectedTrackingMode,
                onSelectMode = { mode ->
                    coroutineScope.launch {
                        trackingPreferencesStore.setTrackingMode(mode)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.PRIVACY) {
            PrivacyScreen(
                onPauseTracking = {
                    trackingController.stopTracking()
                },
                onDeleteAllHistory = {
                    coroutineScope.launch {
                        locationRepository.deleteAllPoints()
                        dataRefreshKey += 1
                    }
                },
                onDeleteHistoryOlderThan30Days = {
                    coroutineScope.launch {
                        val cutoff = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
                        locationRepository.deletePointsOlderThan(cutoff)
                        dataRefreshKey += 1
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}

private fun android.content.Context.hasForegroundLocationPermission(): Boolean {
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

private fun android.content.Context.shouldShowForegroundPermissionRationale(): Boolean {
    val activity = this as? ComponentActivity ?: return false
    val fine = activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
    val coarse = activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
    return fine || coarse
}

private fun android.content.Context.hasBackgroundLocationPermission(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return true
    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

private fun android.content.Context.hasNotificationPermission(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED
}

private fun android.content.Context.openAppLocationSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(intent)
}
