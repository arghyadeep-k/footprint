package com.footprint.app.location

object TrackingModePolicy {
    fun normalizePreferredMode(mode: TrackingMode): TrackingMode {
        return if (mode == TrackingMode.ACTIVE) TrackingMode.BALANCED else mode
    }
}
