package com.footprint.app.timeline

import com.footprint.app.data.local.LocationPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VisitDetectorTest {
    @Test
    fun detectsVisitWhenPointsStayWithinRadiusLongEnough() {
        val start = 1_000_000L
        val points = listOf(
            point(41.87810, -87.62980, start),
            point(41.87811, -87.62981, start + 5 * 60_000L),
            point(41.87809, -87.62979, start + 11 * 60_000L)
        )

        val visits = VisitDetector.detectVisits(points)

        assertEquals(1, visits.size)
        assertTrue(visits.first().durationMillis >= 10 * 60_000L)
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
