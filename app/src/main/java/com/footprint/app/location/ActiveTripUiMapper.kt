package com.footprint.app.location

object ActiveTripUiMapper {
    fun remainingMillis(
        startedAtEpochMillis: Long?,
        nowEpochMillis: Long,
        timeoutMillis: Long = LocationTrackingPolicy.DEFAULT_ACTIVE_TRIP_TIMEOUT_MILLIS
    ): Long? {
        if (startedAtEpochMillis == null) return null
        return (timeoutMillis - (nowEpochMillis - startedAtEpochMillis)).coerceAtLeast(0L)
    }

    fun remainingLabel(remainingMillis: Long?): String? {
        if (remainingMillis == null) return null
        val minutesTotal = remainingMillis / 60_000L
        val hours = minutesTotal / 60
        val minutes = minutesTotal % 60
        return if (hours > 0) "${hours}h ${minutes}m remaining" else "${minutes}m remaining"
    }

    fun timeoutPolicyLabel(
        timeoutMillis: Long = LocationTrackingPolicy.DEFAULT_ACTIVE_TRIP_TIMEOUT_MILLIS
    ): String {
        val hours = timeoutMillis / (60 * 60 * 1000L)
        return "Active trip tracking will return to balanced mode after about ${hours} hours."
    }
}
