package com.footprint.app.timeline

sealed interface TimelineRange {
    data object Today : TimelineRange
    data object Yesterday : TimelineRange
    data object ThisWeek : TimelineRange
    data object ThisMonth : TimelineRange
    data object ThisYear : TimelineRange
    data object Last24Hours : TimelineRange
    data object Last7Days : TimelineRange
    data object Last30Days : TimelineRange
    data object Lifetime : TimelineRange
    data class Custom(
        val startEpochMillis: Long,
        val endEpochMillis: Long
    ) : TimelineRange
}
