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
    fun customWindow_normalizesReversedInputs() {
        val end = Instant.parse("2026-05-20T00:00:00Z").toEpochMilli()
        val start = Instant.parse("2026-05-10T00:00:00Z").toEpochMilli()

        val window = TimelineCalculator.windowFor(
            TimelineRange.Custom(startEpochMillis = end, endEpochMillis = start),
            nowEpochMillis = end,
            zoneId = zone
        )

        assertEquals(start, window.startEpochMillis)
        assertEquals(end, window.endEpochMillis)
    }
}
