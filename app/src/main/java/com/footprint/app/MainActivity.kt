package com.footprint.app

import android.Manifest
import android.content.Intent
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.footprint.app.data.ActiveTripSessionStore
import com.footprint.app.data.LocationRepository
import com.footprint.app.data.TrackingPreferencesStore
import com.footprint.app.data.TrackingRuntimeStateStore
import com.footprint.app.data.local.FootprintDatabaseProvider
import com.footprint.app.location.TrackingController
import com.footprint.app.location.TrackingState
import com.footprint.app.location.TrackingStateReconciler
import com.footprint.app.ui.AppRoutes
import com.footprint.app.ui.HomeScreen
import com.footprint.app.ui.PermissionExplanationScreen
import com.footprint.app.ui.PrivacyScreen
import com.footprint.app.ui.SettingsScreen
import com.footprint.app.ui.theme.FootprintTheme
import com.footprint.app.ui.viewmodel.HomeViewModel
import com.footprint.app.ui.viewmodel.HomeViewModelFactory
import com.footprint.app.ui.viewmodel.PermissionViewModel
import com.footprint.app.ui.viewmodel.PrivacyActionState
import com.footprint.app.ui.viewmodel.PrivacyViewModel
import com.footprint.app.ui.viewmodel.PrivacyViewModelFactory
import com.footprint.app.ui.viewmodel.SettingsViewModel
import com.footprint.app.ui.viewmodel.SettingsViewModelFactory
import kotlinx.coroutines.flow.first

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

    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(
            locationRepository = locationRepository,
            trackingController = trackingController,
            trackingPreferencesStore = trackingPreferencesStore,
            trackingRuntimeStateStore = trackingRuntimeStateStore,
            activeTripSessionStore = activeTripSessionStore
        )
    )
    val permissionViewModel: PermissionViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(trackingPreferencesStore)
    )
    val privacyViewModel: PrivacyViewModel = viewModel(
        factory = PrivacyViewModelFactory(locationRepository, trackingController)
    )

    val homeUiState by homeViewModel.uiState.collectAsState()
    val permissionState by permissionViewModel.permissionState.collectAsState()
    val selectedMode by settingsViewModel.selectedMode.collectAsState()
    val privacyActionState by privacyViewModel.actionState.collectAsState()

    LaunchedEffect(Unit) {
        val persisted = trackingRuntimeStateStore.trackingStateFlow.first()
        val serviceRunning = trackingController.isTrackingServiceRunning()
        val hasRequiredPermissions = trackingController.hasRequiredPermissionsForTracking()
        val reconciled = TrackingStateReconciler.resolveStartupState(
            persistedState = persisted,
            isServiceRunning = serviceRunning,
            hasRequiredPermissions = hasRequiredPermissions
        )
        if (reconciled != persisted) {
            trackingRuntimeStateStore.setState(reconciled)
        }
    }

    LaunchedEffect(permissionState.allRequiredPermissionsReady) {
        homeViewModel.updatePermissionReadiness(permissionState.allRequiredPermissionsReady)
    }

    val foregroundPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        permissionViewModel.refreshFromSystem(context, context as? ComponentActivity)
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        permissionViewModel.refreshFromSystem(context, context as? ComponentActivity)
    }

    NavHost(
        navController = navController,
        startDestination = AppRoutes.PERMISSIONS
    ) {
        composable(AppRoutes.PERMISSIONS) {
            LaunchedEffect(Unit) {
                permissionViewModel.refreshFromSystem(context, context as? ComponentActivity)
            }

            PermissionExplanationScreen(
                permissionState = permissionState,
                onRequestForegroundPermission = {
                    permissionViewModel.onForegroundPermissionRequested()
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
                uiState = homeUiState,
                onTimelineOptionSelected = homeViewModel::onTimelineOptionSelected,
                onCustomStartChanged = homeViewModel::onCustomStartChanged,
                onCustomEndChanged = homeViewModel::onCustomEndChanged,
                onRetryLoad = homeViewModel::reloadTimelineData,
                onStartTracking = {
                    homeViewModel.onStartTracking {
                        navController.navigate(AppRoutes.PERMISSIONS) { launchSingleTop = true }
                    }
                },
                onStopTracking = homeViewModel::onStopTracking,
                onStartActiveTrip = {
                    homeViewModel.onStartActiveTrip {
                        navController.navigate(AppRoutes.PERMISSIONS) { launchSingleTop = true }
                    }
                },
                onStopActiveTrip = homeViewModel::onStopActiveTrip,
                onOpenSettings = { navController.navigate(AppRoutes.SETTINGS) },
                onOpenPrivacy = { navController.navigate(AppRoutes.PRIVACY) },
                onManagePermissions = { navController.navigate(AppRoutes.PERMISSIONS) },
                onMarkExportResult = homeViewModel::markExportResult
            )
        }

        composable(AppRoutes.SETTINGS) {
            SettingsScreen(
                selectedMode = selectedMode,
                onSelectMode = settingsViewModel::selectMode,
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.PRIVACY) {
            val isWorking = privacyActionState is PrivacyActionState.Working
            val actionMessage = when (privacyActionState) {
                is PrivacyActionState.Success -> (privacyActionState as PrivacyActionState.Success).message
                is PrivacyActionState.Error -> (privacyActionState as PrivacyActionState.Error).message
                else -> null
            }

            PrivacyScreen(
                actionMessage = actionMessage,
                isWorking = isWorking,
                onPauseTracking = {
                    privacyViewModel.pauseTracking {
                        homeViewModel.refreshAfterDataChange()
                    }
                },
                onDeleteAllHistory = {
                    privacyViewModel.deleteAllHistory {
                        homeViewModel.refreshAfterDataChange()
                    }
                },
                onDeleteHistoryOlderThan30Days = {
                    privacyViewModel.deleteHistoryOlderThan30Days {
                        homeViewModel.refreshAfterDataChange()
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}

private fun android.content.Context.openAppLocationSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(intent)
}
