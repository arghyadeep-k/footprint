package com.footprint.app.map

import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PolylineThinnerTest {
    @Test
    fun thinning_keepsFirstAndLastAndSkipsClosePoints() {
        val points = listOf(
            LatLng(41.0, -87.0),
            LatLng(41.000001, -87.000001),
            LatLng(41.000002, -87.000002),
            LatLng(41.01, -87.01)
        )

        val thinned = PolylineThinner.thin(points, minDistanceMeters = 50.0)

        assertEquals(points.first(), thinned.first())
        assertEquals(points.last(), thinned.last())
        assertTrue(thinned.size < points.size)
    }
}
