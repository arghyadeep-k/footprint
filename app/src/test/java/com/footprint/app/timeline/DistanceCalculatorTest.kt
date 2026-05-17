package com.footprint.app.timeline

import com.footprint.app.data.local.LocationPoint
import org.junit.Assert.assertTrue
import org.junit.Test

class DistanceCalculatorTest {
    @Test
    fun totalDistanceMeters_isPositiveForDifferentPoints() {
        val points = listOf(
            point(41.8781, -87.6298, 1_000L),
            point(41.8810, -87.6270, 2_000L)
        )

        val meters = DistanceCalculator.totalDistanceMeters(points)
        assertTrue(meters > 0.0)
    }

    private fun point(lat: Double, lon: Double, time: Long): LocationPoint {
        return LocationPoint(
            latitude = lat,
            longitude = lon,
            accuracyMeters = 10f,
            altitudeMeters = null,
            speedMetersPerSecond = null,
            bearingDegrees = null,
            provider = "test",
            recordedAtEpochMillis = time,
            batteryPercent = 50,
            trackingMode = "balanced",
            source = "test"
        )
    }
}
