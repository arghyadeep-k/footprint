package com.footprint.app.location

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ActiveTripUiMapperTest {
    @Test
    fun remainingMillis_returnsNullWhenNoStartTime() {
        assertNull(ActiveTripUiMapper.remainingMillis(startedAtEpochMillis = null, nowEpochMillis = 1_000L))
    }

    @Test
    fun remainingMillis_clampsToZeroAfterTimeout() {
        val remaining = ActiveTripUiMapper.remainingMillis(
            startedAtEpochMillis = 0L,
            nowEpochMillis = LocationTrackingPolicy.DEFAULT_ACTIVE_TRIP_TIMEOUT_MILLIS + 1L
        )
        assertEquals(0L, remaining)
    }

    @Test
    fun remainingLabel_formatsHoursAndMinutes() {
        val label = ActiveTripUiMapper.remainingLabel((65L * 60 * 1000))
        assertEquals("1h 5m remaining", label)
    }

    @Test
    fun timeoutPolicyLabel_mentionsBalancedModeAndHours() {
        val label = ActiveTripUiMapper.timeoutPolicyLabel()
        assertTrue(label.contains("balanced mode"))
        assertTrue(label.contains("2 hours"))
    }
}
