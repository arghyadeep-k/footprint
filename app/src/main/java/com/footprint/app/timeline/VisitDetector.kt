package com.footprint.app.timeline

import com.footprint.app.data.local.LocationPoint

object VisitDetector {
    private const val STATIONARY_RADIUS_METERS = 100.0
    private const val MIN_VISIT_DURATION_MILLIS = 10 * 60 * 1000L

    fun detectVisits(points: List<LocationPoint>): List<VisitSegment> {
        if (points.size < 2) return emptyList()

        val sorted = points.sortedBy { it.recordedAtEpochMillis }
        val visits = mutableListOf<VisitSegment>()

        var clusterStart = 0
        var sumLat = sorted[0].latitude
        var sumLon = sorted[0].longitude
        var clusterCount = 1

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

            if (distance <= STATIONARY_RADIUS_METERS) {
                sumLat += candidate.latitude
                sumLon += candidate.longitude
                clusterCount += 1
            } else {
                finalizeCluster(
                    points = sorted,
                    startIndex = clusterStart,
                    endIndex = i - 1,
                    sumLat = sumLat,
                    sumLon = sumLon,
                    count = clusterCount
                )?.let(visits::add)

                clusterStart = i
                sumLat = candidate.latitude
                sumLon = candidate.longitude
                clusterCount = 1
            }
        }

        finalizeCluster(
            points = sorted,
            startIndex = clusterStart,
            endIndex = sorted.lastIndex,
            sumLat = sumLat,
            sumLon = sumLon,
            count = clusterCount
        )?.let(visits::add)

        return visits
    }

    private fun finalizeCluster(
        points: List<LocationPoint>,
        startIndex: Int,
        endIndex: Int,
        sumLat: Double,
        sumLon: Double,
        count: Int
    ): VisitSegment? {
        if (count <= 1) return null

        val arrival = points[startIndex].recordedAtEpochMillis
        val departure = points[endIndex].recordedAtEpochMillis
        val duration = departure - arrival
        if (duration < MIN_VISIT_DURATION_MILLIS) return null

        return VisitSegment(
            latitude = sumLat / count,
            longitude = sumLon / count,
            arrivalEpochMillis = arrival,
            departureEpochMillis = departure,
            durationMillis = duration,
            pointCount = count
        )
    }
}
