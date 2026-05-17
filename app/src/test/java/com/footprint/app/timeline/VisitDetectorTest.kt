package com.footprint.app.timeline

import com.footprint.app.data.local.LocationPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VisitDetectorTest {
    @Test
    fun nearbyVisits_mergeIntoSingleVisit() {
        val start = 1_000_000L
        val points = listOf(
            point(41.87810, -87.62980, start),
            point(41.87811, -87.62981, start + 5 * 60_000L),
            point(41.87809, -87.62979, start + 11 * 60_000L),
            // brief gap, still nearby
            point(41.87812, -87.62982, start + 13 * 60_000L),
            point(41.87810, -87.62980, start + 18 * 60_000L),
            point(41.87809, -87.62979, start + 24 * 60_000L)
        )

        val visits = VisitDetector.detectVisits(
            points,
            config = VisitDetector.Config(
                stationaryRadiusMeters = 100.0,
                minimumVisitDurationMillis = 10 * 60_000L,
                minimumVisitPoints = 3,
                driftToleranceWindowMillis = 2 * 60_000L,
                mergeNearbyRadiusMeters = 120.0,
                mergeGapToleranceMillis = 3 * 60_000L
            )
        )

        assertEquals(1, visits.size)
        assertTrue(visits.first().durationMillis >= 20 * 60_000L)
    }

    @Test
    fun briefGpsDrift_doesNotSplitVisit() {
        val start = 2_000_000L
        val points = listOf(
            point(41.87810, -87.62980, start),
            point(41.87811, -87.62981, start + 4 * 60_000L),
            // one noisy drift point far away within drift tolerance window
            point(41.88500, -87.64000, start + 5 * 60_000L),
            point(41.87812, -87.62982, start + 6 * 60_000L),
            point(41.87811, -87.62980, start + 12 * 60_000L)
        )

        val visits = VisitDetector.detectVisits(points)

        assertEquals(1, visits.size)
        assertTrue(visits.first().durationMillis >= 10 * 60_000L)
    }

    @Test
    fun movingPoints_doNotBecomeVisit() {
        val start = 3_000_000L
        val points = listOf(
            point(41.87000, -87.62000, start),
            point(41.87500, -87.62500, start + 5 * 60_000L),
            point(41.88000, -87.63000, start + 10 * 60_000L),
            point(41.88500, -87.63500, start + 15 * 60_000L)
        )

        val visits = VisitDetector.detectVisits(points)

        assertTrue(visits.isEmpty())
    }

    @Test
    fun shortStopBelowThreshold_isIgnored() {
        val start = 4_000_000L
        val points = listOf(
            point(41.87810, -87.62980, start),
            point(41.87811, -87.62981, start + 2 * 60_000L),
            point(41.87809, -87.62979, start + 7 * 60_000L)
        )

        val visits = VisitDetector.detectVisits(points)

        assertTrue(visits.isEmpty())
    }

    private fun point(lat: Double, lon: Double, t: Long): LocationPoint {
        return LocationPoint(
            latitude = lat,
            longitude = lon,
            accuracyMeters = 10f,
            altitudeMeters = null,
            speedMetersPerSecond = null,
            bearingDegrees = null,
            provider = "test",
            recordedAtEpochMillis = t,
            batteryPercent = 100,
            trackingMode = "balanced",
            source = "test"
        )
    }
}
