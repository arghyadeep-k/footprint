package com.footprint.app.timeline

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class TimelineCalculatorTest {
    private val zone = ZoneId.of("America/Chicago")

    @Test
    fun todayWindow_usesLocalMidnightBounds() {
        val now = LocalDateTime.of(2026, 5, 16, 18, 45)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()

        val window = TimelineCalculator.windowFor(TimelineRange.Today, now, zone)

        val expectedStart = LocalDateTime.of(2026, 5, 16, 0, 0).atZone(zone).toInstant().toEpochMilli()
        val expectedEnd = LocalDateTime.of(2026, 5, 16, 23, 59, 59, 999_000_000)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()

        assertEquals(expectedStart, window.startEpochMillis)
        assertEquals(expectedEnd, window.endEpochMillis)
    }

    @Test
    fun thisWeekWindow_startsMondayInDeviceZone() {
        val now = LocalDateTime.of(2026, 5, 16, 18, 45)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()

        val window = TimelineCalculator.windowFor(TimelineRange.ThisWeek, now, zone)

        val expectedStart = LocalDateTime.of(2026, 5, 11, 0, 0).atZone(zone).toInstant().toEpochMilli()
        val expectedEnd = LocalDateTime.of(2026, 5, 17, 23, 59, 59, 999_000_000)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()

        assertEquals(expectedStart, window.startEpochMillis)
        assertEquals(expectedEnd, window.endEpochMillis)
    }

    @Test
    fun thisWeekWindow_exactStartAndEndEdges_matchMondayToSundayBounds() {
        val now = LocalDateTime.of(2026, 5, 13, 9, 0)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()

        val window = TimelineCalculator.windowFor(TimelineRange.ThisWeek, now, zone)

        val expectedStart = LocalDateTime.of(2026, 5, 11, 0, 0).atZone(zone).toInstant().toEpochMilli()
        val expectedEnd = LocalDateTime.of(2026, 5, 17, 23, 59, 59, 999_000_000)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()

        assertEquals(expectedStart, window.startEpochMillis)
        assertEquals(expectedEnd, window.endEpochMillis)
    }

    @Test
    fun thisMonthWindow_startsFirstDayOfMonth() {
        val now = LocalDateTime.of(2026, 5, 16, 18, 45)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()

        val window = TimelineCalculator.windowFor(TimelineRange.ThisMonth, now, zone)

        val expectedStart = LocalDateTime.of(2026, 5, 1, 0, 0).atZone(zone).toInstant().toEpochMilli()
        val expectedEnd = LocalDateTime.of(2026, 5, 31, 23, 59, 59, 999_000_000)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()

        assertEquals(expectedStart, window.startEpochMillis)
        assertEquals(expectedEnd, window.endEpochMillis)
    }

    @Test
    fun thisMonthWindow_exactStartAndEndEdges_matchCalendarMonthBounds() {
        val now = LocalDateTime.of(2026, 2, 10, 7, 15)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()

        val window = TimelineCalculator.windowFor(TimelineRange.ThisMonth, now, zone)

        val expectedStart = LocalDateTime.of(2026, 2, 1, 0, 0).atZone(zone).toInstant().toEpochMilli()
        val expectedEnd = LocalDateTime.of(2026, 2, 28, 23, 59, 59, 999_000_000)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()

        assertEquals(expectedStart, window.startEpochMillis)
        assertEquals(expectedEnd, window.endEpochMillis)
    }

    @Test
    fun thisYearWindow_exactStartAndEndEdges_matchCalendarYearBounds() {
        val now = LocalDateTime.of(2026, 8, 20, 13, 30)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()

        val window = TimelineCalculator.windowFor(TimelineRange.ThisYear, now, zone)

        val expectedStart = LocalDateTime.of(2026, 1, 1, 0, 0).atZone(zone).toInstant().toEpochMilli()
        val expectedEnd = LocalDateTime.of(2026, 12, 31, 23, 59, 59, 999_000_000)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()

        assertEquals(expectedStart, window.startEpochMillis)
        assertEquals(expectedEnd, window.endEpochMillis)
    }

    @Test
    fun customWindow_normalizesReversedInputs() {
        val end = Instant.parse("2026-05-20T00:00:00Z").toEpochMilli()
        val start = Instant.parse("2026-05-10T00:00:00Z").toEpochMilli()

        // Intended behavior: reversed custom ranges are normalized instead of failing.
        val window = TimelineCalculator.windowFor(
            TimelineRange.Custom(startEpochMillis = end, endEpochMillis = start),
            nowEpochMillis = end,
            zoneId = zone
        )

        assertEquals(start, window.startEpochMillis)
        assertEquals(end, window.endEpochMillis)
    }

    @Test
    fun customWindow_keepsExactStartEndBoundaries() {
        val start = Instant.parse("2026-05-10T12:34:00Z").toEpochMilli()
        val end = Instant.parse("2026-05-10T13:34:00Z").toEpochMilli()

        val window = TimelineCalculator.windowFor(
            TimelineRange.Custom(startEpochMillis = start, endEpochMillis = end),
            nowEpochMillis = end,
            zoneId = zone
        )

        assertEquals(start, window.startEpochMillis)
        assertEquals(end, window.endEpochMillis)
    }

    @Test
    fun last24HoursWindow_usesRollingDurationFromNow() {
        val now = Instant.parse("2026-05-16T18:45:00Z").toEpochMilli()

        val window = TimelineCalculator.windowFor(TimelineRange.Last24Hours, now, zone)

        assertEquals(now - 24L * 60 * 60 * 1000, window.startEpochMillis)
        assertEquals(now, window.endEpochMillis)
    }

    @Test
    fun last7DaysWindow_usesRollingDurationFromNow() {
        val now = Instant.parse("2026-05-16T18:45:00Z").toEpochMilli()

        val window = TimelineCalculator.windowFor(TimelineRange.Last7Days, now, zone)

        assertEquals(now - 7L * 24 * 60 * 60 * 1000, window.startEpochMillis)
        assertEquals(now, window.endEpochMillis)
    }

    @Test
    fun last30DaysWindow_usesRollingDurationFromNow() {
        val now = Instant.parse("2026-05-16T18:45:00Z").toEpochMilli()

        val window = TimelineCalculator.windowFor(TimelineRange.Last30Days, now, zone)

        assertEquals(now - 30L * 24 * 60 * 60 * 1000, window.startEpochMillis)
        assertEquals(now, window.endEpochMillis)
    }

    @Test
    fun todayWindow_exactStartAndEndEdges_matchDayBounds() {
        val now = LocalDateTime.of(2026, 1, 9, 12, 0)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()

        val window = TimelineCalculator.windowFor(TimelineRange.Today, now, zone)

        val expectedStart = LocalDateTime.of(2026, 1, 9, 0, 0).atZone(zone).toInstant().toEpochMilli()
        val expectedEnd = LocalDateTime.of(2026, 1, 9, 23, 59, 59, 999_000_000)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()

        assertEquals(expectedStart, window.startEpochMillis)
        assertEquals(expectedEnd, window.endEpochMillis)
    }

    @Test
    fun dstSpringForward_todayWindow_isShorterDayButStillLocalMidnightBounded() {
        val dstZone = ZoneId.of("America/New_York")
        val now = LocalDateTime.of(2026, 3, 8, 12, 0)
            .atZone(dstZone)
            .toInstant()
            .toEpochMilli()

        val window = TimelineCalculator.windowFor(TimelineRange.Today, now, dstZone)

        val expectedStart = LocalDateTime.of(2026, 3, 8, 0, 0).atZone(dstZone).toInstant().toEpochMilli()
        val expectedEnd = LocalDateTime.of(2026, 3, 8, 23, 59, 59, 999_000_000)
            .atZone(dstZone)
            .toInstant()
            .toEpochMilli()

        assertEquals(expectedStart, window.startEpochMillis)
        assertEquals(expectedEnd, window.endEpochMillis)
        // Spring-forward day has 23 wall-clock hours.
        assertEquals((23L * 60 * 60 * 1000) - 1L, window.endEpochMillis - window.startEpochMillis)
    }

    @Test
    fun dstFallBack_todayWindow_isLongerDayButStillLocalMidnightBounded() {
        val dstZone = ZoneId.of("America/New_York")
        val now = LocalDateTime.of(2026, 11, 1, 12, 0)
            .atZone(dstZone)
            .toInstant()
            .toEpochMilli()

        val window = TimelineCalculator.windowFor(TimelineRange.Today, now, dstZone)

        val expectedStart = LocalDateTime.of(2026, 11, 1, 0, 0).atZone(dstZone).toInstant().toEpochMilli()
        val expectedEnd = LocalDateTime.of(2026, 11, 1, 23, 59, 59, 999_000_000)
            .atZone(dstZone)
            .toInstant()
            .toEpochMilli()

        assertEquals(expectedStart, window.startEpochMillis)
        assertEquals(expectedEnd, window.endEpochMillis)
        // Fall-back day has 25 wall-clock hours.
        assertEquals((25L * 60 * 60 * 1000) - 1L, window.endEpochMillis - window.startEpochMillis)
    }
}
