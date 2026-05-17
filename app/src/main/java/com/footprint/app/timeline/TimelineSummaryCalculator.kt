package com.footprint.app.timeline

import com.footprint.app.data.local.LocationPoint
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Locale

data class TimelineSummaryMetrics(
    val distancePerDayKm: Double,
    val pointsPerDay: Double,
    val visitCount: Int,
    val averageDailyDistanceKm: Double?
) {
    val distancePerDayLabel: String
        get() = String.format(Locale.getDefault(), "%.2f km/day", distancePerDayKm)
    val pointsPerDayLabel: String
        get() = String.format(Locale.getDefault(), "%.1f points/day", pointsPerDay)
    val averageDailyDistanceLabel: String
        get() = averageDailyDistanceKm?.let {
            String.format(Locale.getDefault(), "%.2f km/day avg", it)
        } ?: "Need at least 2 days of data"
}

object TimelineSummaryCalculator {
    fun calculate(
        points: List<LocationPoint>,
        visitCount: Int,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): TimelineSummaryMetrics {
        if (points.isEmpty()) {
            return TimelineSummaryMetrics(
                distancePerDayKm = 0.0,
                pointsPerDay = 0.0,
                visitCount = visitCount,
                averageDailyDistanceKm = null
            )
        }

        val sorted = points.sortedBy { it.recordedAtEpochMillis }
        val firstDay = sorted.first().recordedAtEpochMillis.toLocalDate(zoneId)
        val lastDay = sorted.last().recordedAtEpochMillis.toLocalDate(zoneId)
        val daySpan = ChronoUnit.DAYS.between(firstDay, lastDay).toInt() + 1
        val dayDivisor = daySpan.coerceAtLeast(1)

        val totalDistanceKm = DistanceCalculator.totalDistanceMeters(sorted) / 1000.0
        val distancePerDay = totalDistanceKm / dayDivisor
        val pointsPerDay = sorted.size.toDouble() / dayDivisor

        return TimelineSummaryMetrics(
            distancePerDayKm = distancePerDay,
            pointsPerDay = pointsPerDay,
            visitCount = visitCount,
            averageDailyDistanceKm = distancePerDay.takeIf { daySpan >= 2 }
        )
    }

    private fun Long.toLocalDate(zoneId: ZoneId): LocalDate {
        return Instant.ofEpochMilli(this).atZone(zoneId).toLocalDate()
    }
}
