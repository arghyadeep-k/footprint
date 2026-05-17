package com.footprint.app.location

import org.junit.Assert.assertEquals
import org.junit.Test

class TrackingModePolicyTest {
    @Test
    fun normalizePreferredMode_convertsActiveToBalanced() {
        assertEquals(
            TrackingMode.BALANCED,
            TrackingModePolicy.normalizePreferredMode(TrackingMode.ACTIVE)
        )
    }

    @Test
    fun normalizePreferredMode_keepsNonActiveModes() {
        assertEquals(
            TrackingMode.LOW_POWER,
            TrackingModePolicy.normalizePreferredMode(TrackingMode.LOW_POWER)
        )
        assertEquals(
            TrackingMode.BALANCED,
            TrackingModePolicy.normalizePreferredMode(TrackingMode.BALANCED)
        )
    }
}
