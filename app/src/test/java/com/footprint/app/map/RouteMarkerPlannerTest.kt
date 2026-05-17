package com.footprint.app.map

import com.footprint.app.timeline.VisitSegment
import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RouteMarkerPlannerTest {
    @Test
    fun plan_returnsNoMarkersWhenFewerThanTwoRoutePoints() {
        val markers = RouteMarkerPlanner.plan(
            routePoints = listOf(LatLng(41.0, -87.0)),
            visitSegments = emptyList()
        )

        assertNull(markers.start)
        assertNull(markers.end)
    }

    @Test
    fun plan_returnsStartAndEndWhenNotNearVisitMarkers() {
        val start = LatLng(41.0, -87.0)
        val end = LatLng(41.1, -87.1)

        val markers = RouteMarkerPlanner.plan(
            routePoints = listOf(start, end),
            visitSegments = listOf(
                visit(latitude = 42.0, longitude = -88.0)
            )
        )

        assertEquals(start, markers.start)
        assertEquals(end, markers.end)
    }

    @Test
    fun plan_suppressesMarkersWhenTooCloseToVisitMarkers() {
        val start = LatLng(41.0, -87.0)
        val end = LatLng(41.0005, -87.0005)

        val markers = RouteMarkerPlanner.plan(
            routePoints = listOf(start, end),
            visitSegments = listOf(
                visit(latitude = 41.0, longitude = -87.0),
                visit(latitude = 41.0005, longitude = -87.0005)
            ),
            suppressRadiusMeters = 80.0
        )

        assertNull(markers.start)
        assertNull(markers.end)
    }

    @Test
    fun plan_canSuppressOnlyOneMarker() {
        val start = LatLng(41.0, -87.0)
        val end = LatLng(41.1, -87.1)

        val markers = RouteMarkerPlanner.plan(
            routePoints = listOf(start, end),
            visitSegments = listOf(visit(latitude = 41.0, longitude = -87.0)),
            suppressRadiusMeters = 80.0
        )

        assertNull(markers.start)
        assertNotNull(markers.end)
    }

    private fun visit(latitude: Double, longitude: Double): VisitSegment {
        return VisitSegment(
            latitude = latitude,
            longitude = longitude,
            arrivalEpochMillis = 1_000L,
            departureEpochMillis = 2_000L,
            durationMillis = 1_000L,
            pointCount = 3
        )
    }
}
