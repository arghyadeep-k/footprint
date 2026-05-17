package com.footprint.app.location

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class TrackingController(
    private val context: Context
) {
    fun startTracking() {
        val intent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_START_TRACKING
        }
        ContextCompat.startForegroundService(context, intent)
    }

    fun stopTracking() {
        val intent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_STOP_TRACKING
        }
        context.startService(intent)
    }

    fun startActiveTrip() {
        val intent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_START_ACTIVE_TRIP
        }
        ContextCompat.startForegroundService(context, intent)
    }

    fun stopActiveTrip() {
        val intent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_STOP_ACTIVE_TRIP
        }
        context.startService(intent)
    }
}
