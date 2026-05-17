package com.footprint.app.map

import com.footprint.app.timeline.VisitSegment
import com.google.android.gms.maps.model.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class RouteMarkerState(
    val start: LatLng?,
    val end: LatLng?
)

object RouteMarkerPlanner {
    private const val EARTH_RADIUS_METERS = 6_371_000.0

    fun plan(
        routePoints: List<LatLng>,
        visitSegments: List<VisitSegment>,
        suppressRadiusMeters: Double = 60.0
    ): RouteMarkerState {
        if (routePoints.size < 2) return RouteMarkerState(start = null, end = null)

        val start = routePoints.first()
        val end = routePoints.last()

        val startSuppressed = isNearVisit(start, visitSegments, suppressRadiusMeters)
        val endSuppressed = isNearVisit(end, visitSegments, suppressRadiusMeters)

        return RouteMarkerState(
            start = if (startSuppressed) null else start,
            end = if (endSuppressed) null else end
        )
    }

    private fun isNearVisit(
        point: LatLng,
        visits: List<VisitSegment>,
        suppressRadiusMeters: Double
    ): Boolean {
        return visits.any { visit ->
            distanceMeters(
                point.latitude,
                point.longitude,
                visit.latitude,
                visit.longitude
            ) <= suppressRadiusMeters
        }
    }

    private fun distanceMeters(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val lat1Rad = Math.toRadians(lat1)
        val lon1Rad = Math.toRadians(lon1)
        val lat2Rad = Math.toRadians(lat2)
        val lon2Rad = Math.toRadians(lon2)

        val dLat = lat2Rad - lat1Rad
        val dLon = lon2Rad - lon1Rad
        val sinLat = sin(dLat / 2.0)
        val sinLon = sin(dLon / 2.0)
        val h = sinLat * sinLat + cos(lat1Rad) * cos(lat2Rad) * sinLon * sinLon
        val c = 2.0 * atan2(sqrt(h), sqrt(1.0 - h))
        return EARTH_RADIUS_METERS * c
    }
}
