package com.footprint.app.timeline

object VisitSelectionMapper {
    fun normalizeSelection(
        selectedIndex: Int?,
        visitCount: Int
    ): Int? {
        if (visitCount <= 0) return null
        if (selectedIndex == null) return null
        return selectedIndex.takeIf { it in 0 until visitCount }
    }

    fun selectedVisit(
        visits: List<VisitSegment>,
        selectedIndex: Int?
    ): VisitSegment? {
        val normalized = normalizeSelection(selectedIndex, visits.size) ?: return null
        return visits[normalized]
    }
}
