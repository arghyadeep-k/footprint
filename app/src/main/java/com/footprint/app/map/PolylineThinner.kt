package com.footprint.app.map

import com.google.android.gms.maps.model.LatLng
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object PolylineThinner {
    fun thin(
        points: List<LatLng>,
        minDistanceMeters: Double,
        maxPoints: Int = 1200
    ): List<LatLng> {
        if (points.size <= 2) return points

        val thinned = ArrayList<LatLng>(points.size)
        var lastKept = points.first()
        thinned += lastKept

        for (index in 1 until points.lastIndex) {
            val point = points[index]
            val distance = distanceMeters(lastKept, point)
            if (distance >= minDistanceMeters) {
                thinned += point
                lastKept = point
            }
        }

        val last = points.last()
        if (thinned.last() != last) {
            thinned += last
        }

        if (thinned.size <= maxPoints) {
            return thinned
        }

        val sampled = ArrayList<LatLng>(maxPoints)
        sampled += thinned.first()
        val interiorTarget = (maxPoints - 2).coerceAtLeast(0)
        val interiorCount = (thinned.size - 2).coerceAtLeast(0)

        if (interiorTarget > 0 && interiorCount > 0) {
            val step = interiorCount.toDouble() / interiorTarget.toDouble()
            var cursor = 1.0
            repeat(interiorTarget) {
                sampled += thinned[cursor.toInt().coerceIn(1, thinned.lastIndex - 1)]
                cursor += step
            }
        }

        sampled += thinned.last()
        return sampled
    }

    private fun distanceMeters(a: LatLng, b: LatLng): Double {
        val earthRadius = 6_371_000.0
        val lat1 = Math.toRadians(a.latitude)
        val lat2 = Math.toRadians(b.latitude)
        val dLat = lat2 - lat1
        val dLon = Math.toRadians(b.longitude - a.longitude)

        val haversine = sin(dLat / 2).pow(2.0) +
            cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2.0)
        val c = 2 * asin(sqrt(haversine))
        return earthRadius * c
    }
}
