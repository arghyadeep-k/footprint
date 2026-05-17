package com.footprint.app.location

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.footprint.app.MainActivity
import com.footprint.app.R
import com.footprint.app.data.ActiveTripSessionStore
import com.footprint.app.data.LocationRepository
import com.footprint.app.data.TrackingPreferencesStore
import com.footprint.app.data.TrackingRuntimeStateStore
import com.footprint.app.data.local.FootprintDatabaseProvider
import com.footprint.app.data.local.LocationPoint
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LocationTrackingService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRepository: LocationRepository
    private lateinit var trackingPreferencesStore: TrackingPreferencesStore
    private lateinit var trackingRuntimeStateStore: TrackingRuntimeStateStore
    private lateinit var activeTripSessionStore: ActiveTripSessionStore
    private lateinit var locationCallback: LocationCallback

    private var lastSavedLocation: Location? = null
    private var hasSavedAnyPoint = false
    private var isRequestingUpdates = false

    private var preferredMode: TrackingMode = TrackingMode.BALANCED
    private var currentRequestMode: TrackingMode = TrackingMode.BALANCED
    private var activeTripRequested = false
    private var activeTripStartedAtEpochMillis: Long? = null
    private var stateOnDestroy: TrackingState = TrackingState.stopped()

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val database = FootprintDatabaseProvider.getDatabase(this)
        locationRepository = LocationRepository(database.locationPointDao())
        trackingPreferencesStore = TrackingPreferencesStore(this)
        trackingRuntimeStateStore = TrackingRuntimeStateStore(this)
        activeTripSessionStore = ActiveTripSessionStore(this)
        locationCallback = buildLocationCallback()

        updateTrackingState(TrackingState.stopped())

        serviceScope.launch {
            trackingPreferencesStore.trackingModeFlow.collectLatest { newMode ->
                val modeChanged = preferredMode != newMode
                preferredMode = newMode
                if (modeChanged && isRequestingUpdates) {
                    syncActiveTripTimeoutIfNeeded()
                    val effectiveMode = resolveEffectiveMode(isMovingMeaningfully = false)
                    recreateLocationUpdates(effectiveMode)
                }
            }
        }

        serviceScope.launch {
            activeTripSessionStore.sessionFlow.collectLatest { session ->
                activeTripRequested = session.isActive
                activeTripStartedAtEpochMillis = session.startedAtEpochMillis

                if (isRequestingUpdates) {
                    syncActiveTripTimeoutIfNeeded()
                    val effectiveMode = resolveEffectiveMode(isMovingMeaningfully = false)
                    if (effectiveMode != currentRequestMode) {
                        recreateLocationUpdates(effectiveMode)
                    } else {
                        updateTrackingState(
                            TrackingState(
                                status = TrackingState.Status.RUNNING,
                                effectiveMode = currentRequestMode,
                                isActiveTripRunning = activeTripRequested && currentRequestMode == TrackingMode.ACTIVE
                            )
                        )
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            when (intent?.action) {
                ACTION_STOP_TRACKING -> stopTrackingAndSelf()
                ACTION_START_ACTIVE_TRIP -> startActiveTrip()
                ACTION_STOP_ACTIVE_TRIP -> stopActiveTrip()
                ACTION_START_TRACKING, null -> startTracking()
            }
        } catch (t: Throwable) {
            reportUnrecoverableError("Service command failure: ${t.javaClass.simpleName}")
            stopTrackingAndSelf()
        }
        return START_STICKY
    }

    private fun startTracking() {
        if (!hasForegroundLocationPermission()) {
            updateTrackingState(TrackingState(status = TrackingState.Status.PERMISSION_MISSING))
            stopTrackingAndSelf()
            return
        }

        startForeground(NOTIFICATION_ID, buildNotification())

        if (isRequestingUpdates) return

        syncActiveTripTimeoutIfNeeded()
        val initialMode = resolveEffectiveMode(isMovingMeaningfully = false)
        requestLocationUpdates(initialMode)
    }

    private fun startActiveTrip() {
        val now = System.currentTimeMillis()
        activeTripRequested = true
        activeTripStartedAtEpochMillis = now
        serviceScope.launch {
            activeTripSessionStore.startActiveTrip(now)
        }
        startTracking()
        if (isRequestingUpdates && currentRequestMode != TrackingMode.ACTIVE) {
            recreateLocationUpdates(TrackingMode.ACTIVE)
        }
    }

    private fun stopActiveTrip() {
        activeTripRequested = false
        activeTripStartedAtEpochMillis = null
        serviceScope.launch {
            activeTripSessionStore.stopActiveTrip()
        }
        if (isRequestingUpdates) {
            val fallbackMode = resolveEffectiveMode(isMovingMeaningfully = false)
            recreateLocationUpdates(fallbackMode)
        }
    }

    private fun requestLocationUpdates(mode: TrackingMode) {
        val request = LocationRequest.Builder(mode.priority, mode.intervalMillis)
            .setMinUpdateIntervalMillis(mode.minUpdateIntervalMillis)
            .setMinUpdateDistanceMeters(mode.minDistanceMeters)
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(request, locationCallback, mainLooper)
            currentRequestMode = mode
            isRequestingUpdates = true
            updateTrackingState(
                TrackingState(
                    status = TrackingState.Status.RUNNING,
                    effectiveMode = mode,
                    isActiveTripRunning = activeTripRequested && mode == TrackingMode.ACTIVE
                )
            )
        } catch (_: SecurityException) {
            reportUnrecoverableError("Location permission unavailable")
            stopTrackingAndSelf()
        } catch (_: Throwable) {
            reportUnrecoverableError("Failed to request location updates")
            stopTrackingAndSelf()
        }
    }

    private fun recreateLocationUpdates(mode: TrackingMode) {
        removeLocationUpdates()
        requestLocationUpdates(mode)
    }

    private fun buildLocationCallback(): LocationCallback {
        return object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                result.locations.forEach { location ->
                    maybePersistLocation(location)
                }
            }
        }
    }

    private fun maybePersistLocation(location: Location) {
        syncActiveTripTimeoutIfNeeded()
        val moving = LocationTrackingPolicy.isMovingMeaningfully(location, lastSavedLocation)
        val effectiveMode = resolveEffectiveMode(isMovingMeaningfully = moving)
        if (effectiveMode != currentRequestMode && isRequestingUpdates) {
            recreateLocationUpdates(effectiveMode)
        }

        if (!LocationTrackingPolicy.shouldPersist(location, lastSavedLocation, hasSavedAnyPoint)) {
            return
        }

        val point = LocationPoint(
            latitude = location.latitude,
            longitude = location.longitude,
            accuracyMeters = if (location.hasAccuracy()) location.accuracy else null,
            altitudeMeters = if (location.hasAltitude()) location.altitude else null,
            speedMetersPerSecond = if (location.hasSpeed()) location.speed else null,
            bearingDegrees = if (location.hasBearing()) location.bearing else null,
            provider = location.provider,
            recordedAtEpochMillis = location.time,
            batteryPercent = currentBatteryPercent(),
            trackingMode = effectiveMode.storageValue,
            source = "fused"
        )

        serviceScope.launch {
            try {
                val insertedId = locationRepository.insertLocationPoint(point)
                if (insertedId > 0L) {
                    lastSavedLocation = location
                    hasSavedAnyPoint = true
                }
            } catch (_: Throwable) {
                reportUnrecoverableError("Failed to persist location point")
                stopTrackingAndSelf()
            }
        }
    }

    private fun currentBatteryPercent(): Int? {
        val batteryManager = getSystemService(BATTERY_SERVICE) as? BatteryManager ?: return null
        val level = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        return if (level in 0..100) level else null
    }

    private fun stopTrackingAndSelf() {
        activeTripRequested = false
        activeTripStartedAtEpochMillis = null
        serviceScope.launch {
            activeTripSessionStore.stopActiveTrip()
        }
        removeLocationUpdates()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun removeLocationUpdates() {
        if (!isRequestingUpdates) return

        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (_: SecurityException) {
            // Ignore; service is stopping and permission may have changed.
        }
        isRequestingUpdates = false
        updateTrackingState(
            TrackingState(
                status = TrackingState.Status.STOPPED,
                isActiveTripRunning = false
            )
        )
    }

    private fun resolveEffectiveMode(isMovingMeaningfully: Boolean): TrackingMode {
        return LocationTrackingPolicy.resolveEffectiveMode(
            preferredMode = preferredMode,
            isMovingMeaningfully = isMovingMeaningfully,
            activeTripRequested = activeTripRequested,
            activeTripStartedAtEpochMillis = activeTripStartedAtEpochMillis,
            nowEpochMillis = System.currentTimeMillis()
        )
    }

    private fun syncActiveTripTimeoutIfNeeded() {
        if (!activeTripRequested) return
        val timedOut = LocationTrackingPolicy.isActiveTripExpired(
            startedAtEpochMillis = activeTripStartedAtEpochMillis,
            nowEpochMillis = System.currentTimeMillis()
        )
        if (timedOut) {
            activeTripRequested = false
            activeTripStartedAtEpochMillis = null
            serviceScope.launch {
                activeTripSessionStore.stopActiveTrip()
            }
        }
    }

    private fun buildNotification(): Notification {
        createNotificationChannelIfNeeded()

        val openAppIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Footprint is recording")
            .setContentText("Footprint is recording travel history.")
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = getSystemService(NotificationManager::class.java)
        val existing = manager.getNotificationChannel(NOTIFICATION_CHANNEL_ID)
        if (existing != null) return

        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows when Footprint is recording travel history"
        }

        manager.createNotificationChannel(channel)
    }

    private fun hasForegroundLocationPermission(): Boolean {
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

    private fun updateTrackingState(state: TrackingState) {
        stateOnDestroy = state
        serviceScope.launch {
            trackingRuntimeStateStore.setState(state)
        }
    }

    private fun reportUnrecoverableError(message: String) {
        updateTrackingState(
            TrackingState(
                status = TrackingState.Status.ERROR,
                errorMessage = message,
                effectiveMode = currentRequestMode.takeIf { isRequestingUpdates },
                isActiveTripRunning = activeTripRequested && currentRequestMode == TrackingMode.ACTIVE
            )
        )
    }

    override fun onDestroy() {
        removeLocationUpdates()
        updateTrackingState(stateOnDestroy)
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START_TRACKING = "com.footprint.app.action.START_TRACKING"
        const val ACTION_STOP_TRACKING = "com.footprint.app.action.STOP_TRACKING"
        const val ACTION_START_ACTIVE_TRIP = "com.footprint.app.action.START_ACTIVE_TRIP"
        const val ACTION_STOP_ACTIVE_TRIP = "com.footprint.app.action.STOP_ACTIVE_TRIP"

        private const val NOTIFICATION_CHANNEL_ID = "footprint_tracking"
        private const val NOTIFICATION_CHANNEL_NAME = "Travel tracking"
        private const val NOTIFICATION_ID = 1001
    }
}
