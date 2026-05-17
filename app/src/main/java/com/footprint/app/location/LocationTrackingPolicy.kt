package com.footprint.app.location

import android.location.Location
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object LocationTrackingPolicy {
    private const val POOR_ACCURACY_METERS = 200f
    private const val DUPLICATE_DISTANCE_METERS = 10f
    private const val DUPLICATE_TIME_WINDOW_MILLIS = 60_000L
    private const val MOVING_DISTANCE_METERS = 80f
    private const val EARTH_RADIUS_METERS = 6_371_000.0
    const val DEFAULT_ACTIVE_TRIP_TIMEOUT_MILLIS = 2 * 60 * 60 * 1000L

    data class PointSample(
        val latitude: Double,
        val longitude: Double,
        val timeMillis: Long,
        val accuracyMeters: Float?
    )

    fun shouldPersist(
        candidate: Location,
        previousSaved: Location?,
        hasAnySavedPoint: Boolean
    ): Boolean {
        return shouldPersist(
            candidate = candidate.toPointSample(),
            previousSaved = previousSaved?.toPointSample(),
            hasAnySavedPoint = hasAnySavedPoint
        )
    }

    fun shouldPersist(
        candidate: PointSample,
        previousSaved: PointSample?,
        hasAnySavedPoint: Boolean
    ): Boolean {
        if (candidate.accuracyMeters != null &&
            candidate.accuracyMeters > POOR_ACCURACY_METERS &&
            hasAnySavedPoint
        ) {
            return false
        }

        if (previousSaved == null) return true

        val closeInSpace = distanceMeters(candidate, previousSaved) < DUPLICATE_DISTANCE_METERS
        val closeInTime = abs(candidate.timeMillis - previousSaved.timeMillis) < DUPLICATE_TIME_WINDOW_MILLIS
        return !(closeInSpace && closeInTime)
    }

    fun isMovingMeaningfully(candidate: Location, previousSaved: Location?): Boolean {
        return isMovingMeaningfully(
            candidate = candidate.toPointSample(),
            previousSaved = previousSaved?.toPointSample()
        )
    }

    fun isMovingMeaningfully(candidate: PointSample, previousSaved: PointSample?): Boolean {
        if (previousSaved == null) return false
        return distanceMeters(candidate, previousSaved) >= MOVING_DISTANCE_METERS
    }

    fun resolveAdaptiveMode(preferredMode: TrackingMode, isMovingMeaningfully: Boolean): TrackingMode {
        return when (preferredMode) {
            TrackingMode.ACTIVE -> if (isMovingMeaningfully) TrackingMode.BALANCED else TrackingMode.LOW_POWER
            TrackingMode.BALANCED -> if (isMovingMeaningfully) TrackingMode.BALANCED else TrackingMode.LOW_POWER
            TrackingMode.LOW_POWER -> if (isMovingMeaningfully) TrackingMode.BALANCED else TrackingMode.LOW_POWER
        }
    }

    fun isActiveTripExpired(
        startedAtEpochMillis: Long?,
        nowEpochMillis: Long,
        timeoutMillis: Long = DEFAULT_ACTIVE_TRIP_TIMEOUT_MILLIS
    ): Boolean {
        if (startedAtEpochMillis == null) return true
        return nowEpochMillis - startedAtEpochMillis >= timeoutMillis
    }

    fun resolveEffectiveMode(
        preferredMode: TrackingMode,
        isMovingMeaningfully: Boolean,
        activeTripRequested: Boolean,
        activeTripStartedAtEpochMillis: Long?,
        nowEpochMillis: Long,
        activeTripTimeoutMillis: Long = DEFAULT_ACTIVE_TRIP_TIMEOUT_MILLIS
    ): TrackingMode {
        val activeTripExpired = isActiveTripExpired(
            startedAtEpochMillis = activeTripStartedAtEpochMillis,
            nowEpochMillis = nowEpochMillis,
            timeoutMillis = activeTripTimeoutMillis
        )
        return if (activeTripRequested && !activeTripExpired) {
            TrackingMode.ACTIVE
        } else {
            resolveAdaptiveMode(preferredMode, isMovingMeaningfully)
        }
    }

    private fun Location.toPointSample(): PointSample {
        return PointSample(
            latitude = latitude,
            longitude = longitude,
            timeMillis = time,
            accuracyMeters = if (hasAccuracy()) accuracy else null
        )
    }

    private fun distanceMeters(a: PointSample, b: PointSample): Double {
        val lat1 = Math.toRadians(a.latitude)
        val lon1 = Math.toRadians(a.longitude)
        val lat2 = Math.toRadians(b.latitude)
        val lon2 = Math.toRadians(b.longitude)

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1
        val sinLat = sin(dLat / 2.0)
        val sinLon = sin(dLon / 2.0)
        val h = sinLat * sinLat + cos(lat1) * cos(lat2) * sinLon * sinLon
        val c = 2.0 * atan2(sqrt(h), sqrt(1.0 - h))
        return EARTH_RADIUS_METERS * c
    }
}
