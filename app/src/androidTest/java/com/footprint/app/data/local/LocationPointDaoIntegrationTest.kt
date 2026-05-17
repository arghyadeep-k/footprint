package com.footprint.app.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocationPointDaoIntegrationTest {

    private lateinit var database: FootprintDatabase
    private lateinit var dao: LocationPointDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, FootprintDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.locationPointDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertOnePoint_persistsPoint() = runBlocking {
        dao.insertLocationPoint(pointAt(1_000L, 41.0, -87.0))

        val points = dao.getAllPointsOrderedByTime()
        assertEquals(1, points.size)
        assertEquals(1_000L, points.first().recordedAtEpochMillis)
    }

    @Test
    fun insertMultiplePoints_persistsAllPoints() = runBlocking {
        dao.insertLocationPoints(
            listOf(
                pointAt(3_000L, 41.3, -87.3),
                pointAt(1_000L, 41.1, -87.1),
                pointAt(2_000L, 41.2, -87.2)
            )
        )

        val points = dao.getAllPointsOrderedByTime()
        assertEquals(listOf(1_000L, 2_000L, 3_000L), points.map { it.recordedAtEpochMillis })
    }

    @Test
    fun getPointsBetween_returnsOnlyWindowPointsInOrder() = runBlocking {
        dao.insertLocationPoints(
            listOf(
                pointAt(500L, 41.0, -87.0),
                pointAt(1_000L, 41.1, -87.1),
                pointAt(1_500L, 41.2, -87.2),
                pointAt(2_000L, 41.3, -87.3),
                pointAt(2_500L, 41.4, -87.4)
            )
        )

        val points = dao.getPointsBetween(startEpochMillis = 1_000L, endEpochMillis = 2_000L)
        assertEquals(listOf(1_000L, 1_500L, 2_000L), points.map { it.recordedAtEpochMillis })
    }

    @Test
    fun deletePointsOlderThan_removesOnlyOlderPoints() = runBlocking {
        dao.insertLocationPoints(
            listOf(
                pointAt(1_000L, 41.0, -87.0),
                pointAt(2_000L, 41.1, -87.1),
                pointAt(3_000L, 41.2, -87.2)
            )
        )

        val deletedCount = dao.deletePointsOlderThan(2_000L)
        assertEquals(1, deletedCount)

        val remaining = dao.getAllPointsOrderedByTime()
        assertEquals(listOf(2_000L, 3_000L), remaining.map { it.recordedAtEpochMillis })
    }

    @Test
    fun deleteAllPoints_removesEverything() = runBlocking {
        dao.insertLocationPoints(
            listOf(
                pointAt(1_000L, 41.0, -87.0),
                pointAt(2_000L, 41.1, -87.1)
            )
        )

        val deletedCount = dao.deleteAllPoints()
        assertEquals(2, deletedCount)
        assertTrue(dao.getAllPointsOrderedByTime().isEmpty())
    }

    private fun pointAt(
        recordedAt: Long,
        latitude: Double,
        longitude: Double
    ): LocationPoint {
        return LocationPoint(
            latitude = latitude,
            longitude = longitude,
            accuracyMeters = 10f,
            altitudeMeters = null,
            speedMetersPerSecond = null,
            bearingDegrees = null,
            provider = "gps",
            recordedAtEpochMillis = recordedAt,
            batteryPercent = 80,
            trackingMode = "BALANCED",
            source = "integration_test"
        )
    }
}
