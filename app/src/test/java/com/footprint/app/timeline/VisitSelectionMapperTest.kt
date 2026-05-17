package com.footprint.app.timeline

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VisitSelectionMapperTest {
    @Test
    fun normalizeSelection_returnsNullWhenOutOfRange() {
        assertNull(VisitSelectionMapper.normalizeSelection(selectedIndex = -1, visitCount = 3))
        assertNull(VisitSelectionMapper.normalizeSelection(selectedIndex = 3, visitCount = 3))
    }

    @Test
    fun normalizeSelection_keepsValidIndex() {
        assertEquals(1, VisitSelectionMapper.normalizeSelection(selectedIndex = 1, visitCount = 3))
    }

    @Test
    fun selectedVisit_returnsMatchingVisitOrNull() {
        val visits = listOf(
            visit(41.0, -87.0),
            visit(42.0, -88.0)
        )
        assertEquals(visits[1], VisitSelectionMapper.selectedVisit(visits, 1))
        assertNull(VisitSelectionMapper.selectedVisit(visits, 2))
    }

    private fun visit(lat: Double, lon: Double): VisitSegment {
        return VisitSegment(
            latitude = lat,
            longitude = lon,
            arrivalEpochMillis = 1_000L,
            departureEpochMillis = 2_000L,
            durationMillis = 1_000L,
            pointCount = 3
        )
    }
}
