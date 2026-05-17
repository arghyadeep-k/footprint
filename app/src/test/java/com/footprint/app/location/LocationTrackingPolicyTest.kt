package com.footprint.app.location

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationTrackingPolicyTest {
    @Test
    fun poorAccuracy_isIgnoredAfterFirstSavedPoint() {
        val candidate = sample(lat = 41.0, lon = -87.0, timeMillis = 1_000L, accuracy = 300f)

        val shouldPersist = LocationTrackingPolicy.shouldPersist(
            candidate = candidate,
            previousSaved = sample(lat = 41.0, lon = -87.0, timeMillis = 0L, accuracy = 20f),
            hasAnySavedPoint = true
        )

        assertFalse(shouldPersist)
    }

    @Test
    fun poorAccuracy_isAllowedWhenNoSavedPointsExist() {
        val candidate = sample(lat = 41.0, lon = -87.0, timeMillis = 1_000L, accuracy = 300f)

        val shouldPersist = LocationTrackingPolicy.shouldPersist(
            candidate = candidate,
            previousSaved = null,
            hasAnySavedPoint = false
        )

        assertTrue(shouldPersist)
    }

    @Test
    fun duplicateNearbyPoint_isIgnored() {
        val previous = sample(lat = 41.0, lon = -87.0, timeMillis = 60_000L, accuracy = 10f)
        val candidate = sample(lat = 41.00001, lon = -87.00001, timeMillis = 90_000L, accuracy = 10f)

        val shouldPersist = LocationTrackingPolicy.shouldPersist(
            candidate = candidate,
            previousSaved = previous,
            hasAnySavedPoint = true
        )

        assertFalse(shouldPersist)
    }

    @Test
    fun balancedMode_downgradesToLowPowerWhenNotMoving() {
        val mode = LocationTrackingPolicy.resolveAdaptiveMode(
            preferredMode = TrackingMode.BALANCED,
            isMovingMeaningfully = false
        )
        assertEquals(TrackingMode.LOW_POWER, mode)
    }

    @Test
    fun lowPowerMode_promotesToBalancedWhenMoving() {
        val mode = LocationTrackingPolicy.resolveAdaptiveMode(
            preferredMode = TrackingMode.LOW_POWER,
            isMovingMeaningfully = true
        )
        assertEquals(TrackingMode.BALANCED, mode)
    }

    @Test
    fun activeMode_staysActive() {
        val mode = LocationTrackingPolicy.resolveAdaptiveMode(
            preferredMode = TrackingMode.ACTIVE,
            isMovingMeaningfully = false
        )
        assertEquals(TrackingMode.LOW_POWER, mode)
    }

    @Test
    fun resolveEffectiveMode_usesActiveDuringValidTripSession() {
        val mode = LocationTrackingPolicy.resolveEffectiveMode(
            preferredMode = TrackingMode.BALANCED,
            isMovingMeaningfully = false,
            activeTripRequested = true,
            activeTripStartedAtEpochMillis = 1_000L,
            nowEpochMillis = 1_000L + (30 * 60 * 1000L)
        )

        assertEquals(TrackingMode.ACTIVE, mode)
    }

    @Test
    fun resolveEffectiveMode_doesNotUseActiveWithoutExplicitTripStart() {
        val mode = LocationTrackingPolicy.resolveEffectiveMode(
            preferredMode = TrackingMode.BALANCED,
            isMovingMeaningfully = true,
            activeTripRequested = false,
            activeTripStartedAtEpochMillis = null,
            nowEpochMillis = 1_000L
        )

        assertEquals(TrackingMode.BALANCED, mode)
    }

    @Test
    fun resolveEffectiveMode_fallsBackWhenActiveTripStoppedExplicitly() {
        val mode = LocationTrackingPolicy.resolveEffectiveMode(
            preferredMode = TrackingMode.LOW_POWER,
            isMovingMeaningfully = false,
            activeTripRequested = false,
            activeTripStartedAtEpochMillis = 1_000L,
            nowEpochMillis = 1_000L + 10_000L
        )

        assertEquals(TrackingMode.LOW_POWER, mode)
    }

    @Test
    fun resolveEffectiveMode_fallsBackAfterActiveTimeout() {
        val mode = LocationTrackingPolicy.resolveEffectiveMode(
            preferredMode = TrackingMode.BALANCED,
            isMovingMeaningfully = false,
            activeTripRequested = true,
            activeTripStartedAtEpochMillis = 1_000L,
            nowEpochMillis = 1_000L + LocationTrackingPolicy.DEFAULT_ACTIVE_TRIP_TIMEOUT_MILLIS
        )

        assertEquals(TrackingMode.LOW_POWER, mode)
    }

    @Test
    fun resolveEffectiveMode_afterActiveTimeout_usesNonActiveAdaptiveFallbackWhenMoving() {
        val mode = LocationTrackingPolicy.resolveEffectiveMode(
            preferredMode = TrackingMode.BALANCED,
            isMovingMeaningfully = true,
            activeTripRequested = true,
            activeTripStartedAtEpochMillis = 1_000L,
            nowEpochMillis = 1_000L + LocationTrackingPolicy.DEFAULT_ACTIVE_TRIP_TIMEOUT_MILLIS
        )

        assertEquals(TrackingMode.BALANCED, mode)
    }

    @Test
    fun resolveEffectiveMode_afterActiveTimeout_neverReturnsActive() {
        val mode = LocationTrackingPolicy.resolveEffectiveMode(
            preferredMode = TrackingMode.LOW_POWER,
            isMovingMeaningfully = true,
            activeTripRequested = true,
            activeTripStartedAtEpochMillis = 1_000L,
            nowEpochMillis = 1_000L + LocationTrackingPolicy.DEFAULT_ACTIVE_TRIP_TIMEOUT_MILLIS + 1L
        )

        assertTrue(mode != TrackingMode.ACTIVE)
    }

    @Test
    fun isActiveTripExpired_returnsTrueAtOrBeyondTimeout() {
        assertTrue(
            LocationTrackingPolicy.isActiveTripExpired(
                startedAtEpochMillis = 10L,
                nowEpochMillis = 10L + LocationTrackingPolicy.DEFAULT_ACTIVE_TRIP_TIMEOUT_MILLIS
            )
        )
    }

    @Test
    fun resolveEffectiveMode_keepsPreferredAndActiveOverrideSeparate() {
        val withActiveOverride = LocationTrackingPolicy.resolveEffectiveMode(
            preferredMode = TrackingMode.LOW_POWER,
            isMovingMeaningfully = false,
            activeTripRequested = true,
            activeTripStartedAtEpochMillis = 10L,
            nowEpochMillis = 10L + 60_000L
        )
        val withoutActiveOverride = LocationTrackingPolicy.resolveEffectiveMode(
            preferredMode = TrackingMode.LOW_POWER,
            isMovingMeaningfully = false,
            activeTripRequested = false,
            activeTripStartedAtEpochMillis = null,
            nowEpochMillis = 10L + 60_000L
        )

        assertEquals(TrackingMode.ACTIVE, withActiveOverride)
        assertEquals(TrackingMode.LOW_POWER, withoutActiveOverride)
    }

    private fun sample(
        lat: Double,
        lon: Double,
        timeMillis: Long,
        accuracy: Float?
    ): LocationTrackingPolicy.PointSample {
        return LocationTrackingPolicy.PointSample(
            latitude = lat,
            longitude = lon,
            timeMillis = timeMillis,
            accuracyMeters = accuracy
        )
    }
}
