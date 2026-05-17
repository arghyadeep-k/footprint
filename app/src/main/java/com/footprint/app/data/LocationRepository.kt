package com.footprint.app.data

import com.footprint.app.data.local.LocationPoint
import com.footprint.app.data.local.LocationPointDao
import com.footprint.app.timeline.TimelineCalculator
import com.footprint.app.timeline.TimelineRange
import java.time.ZoneId

class LocationRepository(
    private val locationPointDao: LocationPointDao
) {
    suspend fun insertLocationPoint(point: LocationPoint): Long {
        return locationPointDao.insertLocationPoint(point.sanitized())
    }

    suspend fun insertLocationPoints(points: List<LocationPoint>): List<Long> {
        if (points.isEmpty()) return emptyList()
        return locationPointDao.insertLocationPoints(points.map { it.sanitized() })
    }

    suspend fun getPointsBetween(startEpochMillis: Long, endEpochMillis: Long): List<LocationPoint> {
        val (start, end) = LocationPointHelpers.normalizeTimeWindow(startEpochMillis, endEpochMillis)
        return locationPointDao.getPointsBetween(start, end)
    }

    suspend fun getPointsForTimelineRange(
        range: TimelineRange,
        nowEpochMillis: Long = System.currentTimeMillis(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): List<LocationPoint> {
        return when (range) {
            TimelineRange.Lifetime -> locationPointDao.getAllPointsOrderedByTime()
            else -> {
                val window = TimelineCalculator.windowFor(range, nowEpochMillis, zoneId)
                locationPointDao.getPointsBetween(window.startEpochMillis, window.endEpochMillis)
            }
        }
    }

    suspend fun getAllPointsOrderedByTime(): List<LocationPoint> {
        return locationPointDao.getAllPointsOrderedByTime()
    }

    suspend fun deletePointsOlderThan(olderThanEpochMillis: Long): Int {
        return locationPointDao.deletePointsOlderThan(olderThanEpochMillis)
    }

    suspend fun deletePointsBetween(startEpochMillis: Long, endEpochMillis: Long): Int {
        val (start, end) = LocationPointHelpers.normalizeTimeWindow(startEpochMillis, endEpochMillis)
        return locationPointDao.deletePointsBetween(start, end)
    }

    suspend fun deleteAllPoints(): Int {
        return locationPointDao.deleteAllPoints()
    }

    private fun LocationPoint.sanitized(): LocationPoint {
        return copy(
            provider = LocationPointHelpers.normalizeOptionalText(provider),
            batteryPercent = LocationPointHelpers.clampBatteryPercent(batteryPercent),
            trackingMode = LocationPointHelpers.normalizeRequiredText(trackingMode),
            source = LocationPointHelpers.normalizeRequiredText(source)
        )
    }
}
