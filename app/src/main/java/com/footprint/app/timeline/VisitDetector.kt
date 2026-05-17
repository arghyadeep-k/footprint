package com.footprint.app.timeline

import com.footprint.app.data.local.LocationPoint

object VisitDetector {
    data class Config(
        val stationaryRadiusMeters: Double = 100.0,
        val minimumVisitDurationMillis: Long = 10 * 60 * 1000L,
        val minimumVisitPoints: Int = 3,
        val driftToleranceWindowMillis: Long = 2 * 60 * 1000L,
        val mergeNearbyRadiusMeters: Double = 120.0,
        val mergeGapToleranceMillis: Long = 3 * 60 * 1000L
    )

    fun detectVisits(
        points: List<LocationPoint>,
        config: Config = Config()
    ): List<VisitSegment> {
        if (points.size < 2) return emptyList()

        val sorted = points.sortedBy { it.recordedAtEpochMillis }
        val visits = mutableListOf<VisitSegment>()

        var clusterStart = 0
        var clusterEnd = 0
        var sumLat = sorted[0].latitude
        var sumLon = sorted[0].longitude
        var clusterCount = 1
        var driftStartIndex: Int? = null

        for (i in 1 until sorted.size) {
            val candidate = sorted[i]
            val centerLat = sumLat / clusterCount
            val centerLon = sumLon / clusterCount
            val distance = DistanceCalculator.haversineMeters(
                lat1 = centerLat,
                lon1 = centerLon,
                lat2 = candidate.latitude,
                lon2 = candidate.longitude
            )

            if (distance <= config.stationaryRadiusMeters) {
                driftStartIndex = null
                sumLat += candidate.latitude
                sumLon += candidate.longitude
                clusterCount += 1
                clusterEnd = i
            } else {
                if (driftStartIndex == null) {
                    driftStartIndex = i
                }
                val driftDuration = candidate.recordedAtEpochMillis -
                    sorted[driftStartIndex].recordedAtEpochMillis

                if (driftDuration > config.driftToleranceWindowMillis) {
                    finalizeCluster(
                        points = sorted,
                        startIndex = clusterStart,
                        endIndex = clusterEnd,
                        sumLat = sumLat,
                        sumLon = sumLon,
                        count = clusterCount,
                        config = config
                    )?.let(visits::add)

                    clusterStart = i
                    clusterEnd = i
                    sumLat = candidate.latitude
                    sumLon = candidate.longitude
                    clusterCount = 1
                    driftStartIndex = null
                }
            }
        }

        finalizeCluster(
            points = sorted,
            startIndex = clusterStart,
            endIndex = clusterEnd,
            sumLat = sumLat,
            sumLon = sumLon,
            count = clusterCount,
            config = config
        )?.let(visits::add)

        return mergeNearbyVisits(visits, config)
    }

    private fun finalizeCluster(
        points: List<LocationPoint>,
        startIndex: Int,
        endIndex: Int,
        sumLat: Double,
        sumLon: Double,
        count: Int,
        config: Config
    ): VisitSegment? {
        if (count < config.minimumVisitPoints) return null

        val arrival = points[startIndex].recordedAtEpochMillis
        val departure = points[endIndex].recordedAtEpochMillis
        val duration = departure - arrival
        if (duration < config.minimumVisitDurationMillis) return null

        return VisitSegment(
            latitude = sumLat / count,
            longitude = sumLon / count,
            arrivalEpochMillis = arrival,
            departureEpochMillis = departure,
            durationMillis = duration,
            pointCount = count
        )
    }

    private fun mergeNearbyVisits(
        visits: List<VisitSegment>,
        config: Config
    ): List<VisitSegment> {
        if (visits.size < 2) return visits
        val merged = mutableListOf<VisitSegment>()
        var current = visits.first()

        for (i in 1 until visits.size) {
            val next = visits[i]
            val gapMillis = next.arrivalEpochMillis - current.departureEpochMillis
            val distance = DistanceCalculator.haversineMeters(
                lat1 = current.latitude,
                lon1 = current.longitude,
                lat2 = next.latitude,
                lon2 = next.longitude
            )
            val shouldMerge = gapMillis <= config.mergeGapToleranceMillis &&
                distance <= config.mergeNearbyRadiusMeters

            current = if (shouldMerge) {
                merge(current, next)
            } else {
                merged += current
                next
            }
        }

        merged += current
        return merged
    }

    private fun merge(first: VisitSegment, second: VisitSegment): VisitSegment {
        val totalCount = first.pointCount + second.pointCount
        val mergedLat = ((first.latitude * first.pointCount) + (second.latitude * second.pointCount)) / totalCount
        val mergedLon = ((first.longitude * first.pointCount) + (second.longitude * second.pointCount)) / totalCount
        val arrival = minOf(first.arrivalEpochMillis, second.arrivalEpochMillis)
        val departure = maxOf(first.departureEpochMillis, second.departureEpochMillis)
        return VisitSegment(
            latitude = mergedLat,
            longitude = mergedLon,
            arrivalEpochMillis = arrival,
            departureEpochMillis = departure,
            durationMillis = departure - arrival,
            pointCount = totalCount
        )
    }
}
