package com.footprint.app.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.footprint.app.data.local.FootprintDatabase
import com.footprint.app.data.local.LocationPoint
import com.footprint.app.timeline.TimelineRange
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.ZoneId
import java.time.ZonedDateTime

@RunWith(AndroidJUnit4::class)
class LocationRepositoryTimelineIntegrationTest {

    private lateinit var database: FootprintDatabase
    private lateinit var repository: LocationRepository

    private val testZone: ZoneId = ZoneId.of("America/Chicago")
    private val now = ZonedDateTime.of(2026, 5, 16, 12, 0, 0, 0, testZone)
    private val nowEpochMillis = now.toInstant().toEpochMilli()

    @Before
    fun setUp() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, FootprintDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = LocationRepository(database.locationPointDao())
        repository.insertLocationPoints(seedPoints())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun getPointsForToday_returnsOnlyTodayPoints() = runBlocking {
        assertRange(
            TimelineRange.Today,
            expectedLabels = listOf("today_1", "today_2")
        )
    }

    @Test
    fun getPointsForYesterday_returnsOnlyYesterdayPoints() = runBlocking {
        assertRange(
            TimelineRange.Yesterday,
            expectedLabels = listOf("yesterday_1")
        )
    }

    @Test
    fun getPointsForThisWeek_returnsOnlyThisWeekPoints() = runBlocking {
        assertRange(
            TimelineRange.ThisWeek,
            expectedLabels = listOf(
                "week_start",
                "yesterday_1",
                "today_1",
                "today_2"
            )
        )
    }

    @Test
    fun getPointsForThisMonth_returnsOnlyThisMonthPoints() = runBlocking {
        assertRange(
            TimelineRange.ThisMonth,
            expectedLabels = listOf(
                "month_start",
                "week_start",
                "yesterday_1",
                "today_1",
                "today_2"
            )
        )
    }

    @Test
    fun getPointsForThisYear_returnsOnlyThisYearPoints() = runBlocking {
        assertRange(
            TimelineRange.ThisYear,
            expectedLabels = listOf(
                "year_start",
                "month_start",
                "week_start",
                "yesterday_1",
                "today_1",
                "today_2"
            )
        )
    }

    @Test
    fun getPointsForLifetime_returnsAllPointsInOrder() = runBlocking {
        assertRange(
            TimelineRange.Lifetime,
            expectedLabels = listOf(
                "last_year",
                "year_start",
                "month_start",
                "week_start",
                "yesterday_1",
                "today_1",
                "today_2"
            )
        )
    }

    @Test
    fun getPointsForCustomRange_returnsOnlyPointsInsideCustomWindow() = runBlocking {
        val customStart = at(2026, 5, 14, 0, 0)
        val customEnd = at(2026, 5, 15, 23, 59)
        assertRange(
            TimelineRange.Custom(customStart, customEnd),
            expectedLabels = listOf("yesterday_1")
        )
    }

    private suspend fun assertRange(range: TimelineRange, expectedLabels: List<String>) {
        val labels = repository.getPointsForTimelineRange(
            range = range,
            nowEpochMillis = nowEpochMillis,
            zoneId = testZone
        ).map { it.source }
        assertEquals(expectedLabels, labels)
    }

    private fun seedPoints(): List<LocationPoint> {
        return listOf(
            point("last_year", at(2025, 12, 31, 12, 0)),
            point("year_start", at(2026, 1, 1, 0, 0)),
            point("month_start", at(2026, 5, 1, 0, 0)),
            point("week_start", at(2026, 5, 11, 0, 0)), // Monday
            point("yesterday_1", at(2026, 5, 15, 10, 0)),
            point("today_1", at(2026, 5, 16, 8, 0)),
            point("today_2", at(2026, 5, 16, 11, 30))
        )
    }

    private fun at(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int
    ): Long {
        return ZonedDateTime.of(year, month, day, hour, minute, 0, 0, testZone)
            .toInstant()
            .toEpochMilli()
    }

    private fun point(label: String, recordedAtEpochMillis: Long): LocationPoint {
        return LocationPoint(
            latitude = 41.0,
            longitude = -87.0,
            accuracyMeters = 10f,
            altitudeMeters = null,
            speedMetersPerSecond = null,
            bearingDegrees = null,
            provider = "gps",
            recordedAtEpochMillis = recordedAtEpochMillis,
            batteryPercent = 75,
            trackingMode = "BALANCED",
            source = label
        )
    }
}
