package com.footprint.app.timeline

import com.footprint.app.data.local.LocationPoint
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object DistanceCalculator {
    fun totalDistanceMeters(points: List<LocationPoint>): Double {
        if (points.size < 2) return 0.0

        var total = 0.0
        for (i in 1 until points.size) {
            total += haversineMeters(
                lat1 = points[i - 1].latitude,
                lon1 = points[i - 1].longitude,
                lat2 = points[i].latitude,
                lon2 = points[i].longitude
            )
        }
        return total
    }

    fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6_371_000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val rLat1 = Math.toRadians(lat1)
        val rLat2 = Math.toRadians(lat2)

        val haversine = sin(dLat / 2).pow(2.0) +
            cos(rLat1) * cos(rLat2) * sin(dLon / 2).pow(2.0)
        val c = 2 * asin(sqrt(haversine))
        return earthRadius * c
    }
}
