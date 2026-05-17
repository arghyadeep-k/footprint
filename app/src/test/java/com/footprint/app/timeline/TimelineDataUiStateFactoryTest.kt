package com.footprint.app.timeline

import com.footprint.app.data.local.LocationPoint
import org.junit.Assert.assertTrue
import org.junit.Test

class TimelineDataUiStateFactoryTest {

    @Test
    fun fromPoints_returnsEmpty_forNoPoints() {
        val state = TimelineDataUiStateFactory.fromPoints(emptyList())
        assertTrue(state is TimelineDataUiState.Empty)
    }

    @Test
    fun fromPoints_returnsSuccess_forPoints() {
        val point = LocationPoint(
            id = 1L,
            latitude = 40.0,
            longitude = -74.0,
            accuracyMeters = 10f,
            altitudeMeters = null,
            speedMetersPerSecond = null,
            bearingDegrees = null,
            provider = "gps",
            recordedAtEpochMillis = 1234L,
            batteryPercent = 50,
            trackingMode = "BALANCED",
            source = "test"
        )
        val state = TimelineDataUiStateFactory.fromPoints(listOf(point))
        assertTrue(state is TimelineDataUiState.Success)
    }

    @Test
    fun fromError_returnsErrorState() {
        val state = TimelineDataUiStateFactory.fromError(IllegalStateException("db failed"))
        assertTrue(state is TimelineDataUiState.Error)
    }
}
