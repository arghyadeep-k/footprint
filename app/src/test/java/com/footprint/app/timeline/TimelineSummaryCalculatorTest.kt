package com.footprint.app.timeline

import com.footprint.app.data.local.LocationPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class TimelineSummaryCalculatorTest {
    private val zone: ZoneId = ZoneId.of("America/Chicago")

    @Test
    fun calculate_returnsZeroMetricsForEmptyPoints() {
        val metrics = TimelineSummaryCalculator.calculate(
            points = emptyList(),
            visitCount = 3,
            zoneId = zone
        )

        assertEquals(0.0, metrics.distancePerDayKm, 0.0001)
        assertEquals(0.0, metrics.pointsPerDay, 0.0001)
        assertEquals(3, metrics.visitCount)
        assertNull(metrics.averageDailyDistanceKm)
    }

    @Test
    fun calculate_computesPointsAndDistancePerDayAcrossMultipleDays() {
        val day1 = at(2026, 5, 10, 8, 0)
        val day2 = at(2026, 5, 11, 8, 0)
        val day3 = at(2026, 5, 12, 8, 0)
        val points = listOf(
            point(41.0000, -87.0000, day1),
            point(41.0009, -87.0000, day2), // ~100m north
            point(41.0018, -87.0000, day3)  // ~100m north
        )

        val metrics = TimelineSummaryCalculator.calculate(
            points = points,
            visitCount = 2,
            zoneId = zone
        )

        assertEquals(2, metrics.visitCount)
        assertEquals(1.0, metrics.pointsPerDay, 0.0001)
        assertTrue(metrics.distancePerDayKm > 0.0)
        assertEquals(metrics.distancePerDayKm, metrics.averageDailyDistanceKm ?: 0.0, 0.0001)
    }

    @Test
    fun calculate_averageDailyDistance_requiresAtLeastTwoDays() {
        val day = at(2026, 6, 1, 9, 0)
        val points = listOf(
            point(41.0, -87.0, day),
            point(41.0005, -87.0005, day + 10 * 60_000L)
        )

        val metrics = TimelineSummaryCalculator.calculate(
            points = points,
            visitCount = 1,
            zoneId = zone
        )

        assertNull(metrics.averageDailyDistanceKm)
    }

    private fun at(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int
    ): Long {
        return ZonedDateTime.of(year, month, day, hour, minute, 0, 0, zone)
            .toInstant()
            .toEpochMilli()
    }

    private fun point(
        lat: Double,
        lon: Double,
        time: Long
    ): LocationPoint {
        return LocationPoint(
            latitude = lat,
            longitude = lon,
            accuracyMeters = 10f,
            altitudeMeters = null,
            speedMetersPerSecond = null,
            bearingDegrees = null,
            provider = "test",
            recordedAtEpochMillis = time,
            batteryPercent = 90,
            trackingMode = "balanced",
            source = "test"
        )
    }
}
