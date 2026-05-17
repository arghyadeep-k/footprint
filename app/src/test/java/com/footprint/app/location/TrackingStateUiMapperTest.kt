package com.footprint.app.location

import org.junit.Assert.assertEquals
import org.junit.Test

class TrackingStateUiMapperTest {
    @Test
    fun statusLabel_mapsStopped() {
        val label = TrackingStateUiMapper.statusLabel(TrackingState(TrackingState.Status.STOPPED))
        assertEquals("Tracking is paused", label)
    }

    @Test
    fun effectiveModeLabel_mapsRunningMode() {
        val label = TrackingStateUiMapper.effectiveModeLabel(
            TrackingState(
                status = TrackingState.Status.RUNNING,
                effectiveMode = TrackingMode.LOW_POWER
            )
        )
        assertEquals("Battery saver tracking", label)
    }
}
