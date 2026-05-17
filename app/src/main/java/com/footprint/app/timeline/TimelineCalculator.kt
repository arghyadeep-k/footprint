package com.footprint.app.timeline

import java.time.Clock
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters

data class TimelineWindow(
    val startEpochMillis: Long,
    val endEpochMillis: Long
)

class TimelineCalculator(
    private val clock: Clock = Clock.systemDefaultZone(),
    private val zoneId: ZoneId = clock.zone
) {
    fun windowFor(range: TimelineRange): TimelineWindow {
        val now = ZonedDateTime.ofInstant(clock.instant(), zoneId)
        return when (range) {
            TimelineRange.Today -> dayWindow(now)
            TimelineRange.Yesterday -> dayWindow(now.minusDays(1))
            TimelineRange.ThisWeek -> thisWeekWindow(now)
            TimelineRange.ThisMonth -> thisMonthWindow(now)
            TimelineRange.ThisYear -> thisYearWindow(now)
            TimelineRange.Last24Hours -> rollingWindow(now, 24L * 60 * 60 * 1000)
            TimelineRange.Last7Days -> rollingWindow(now, 7L * 24 * 60 * 60 * 1000)
            TimelineRange.Last30Days -> rollingWindow(now, 30L * 24 * 60 * 60 * 1000)
            TimelineRange.Lifetime -> TimelineWindow(
                startEpochMillis = Long.MIN_VALUE,
                endEpochMillis = now.toInstant().toEpochMilli()
            )
            is TimelineRange.Custom -> customWindow(range.startEpochMillis, range.endEpochMillis)
        }
    }

    fun dayWindow(dayInZone: ZonedDateTime): TimelineWindow {
        val start = dayInZone.toLocalDate().atStartOfDay(zoneId)
        val end = start.plusDays(1).minusNanos(1)
        return TimelineWindow(start.toInstant().toEpochMilli(), end.toInstant().toEpochMilli())
    }

    fun thisWeekWindow(nowInZone: ZonedDateTime): TimelineWindow {
        val start = nowInZone
            .toLocalDate()
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .atStartOfDay(zoneId)
        val end = start.plusWeeks(1).minusNanos(1)
        return TimelineWindow(start.toInstant().toEpochMilli(), end.toInstant().toEpochMilli())
    }

    fun thisMonthWindow(nowInZone: ZonedDateTime): TimelineWindow {
        val start = nowInZone
            .toLocalDate()
            .withDayOfMonth(1)
            .atStartOfDay(zoneId)
        val end = start.plusMonths(1).minusNanos(1)
        return TimelineWindow(start.toInstant().toEpochMilli(), end.toInstant().toEpochMilli())
    }

    fun thisYearWindow(nowInZone: ZonedDateTime): TimelineWindow {
        val start = nowInZone
            .toLocalDate()
            .withDayOfYear(1)
            .atStartOfDay(zoneId)
        val end = start.plusYears(1).minusNanos(1)
        return TimelineWindow(start.toInstant().toEpochMilli(), end.toInstant().toEpochMilli())
    }

    fun customWindow(startEpochMillis: Long, endEpochMillis: Long): TimelineWindow {
        return if (startEpochMillis <= endEpochMillis) {
            TimelineWindow(startEpochMillis, endEpochMillis)
        } else {
            TimelineWindow(endEpochMillis, startEpochMillis)
        }
    }

    private fun rollingWindow(nowInZone: ZonedDateTime, durationMillis: Long): TimelineWindow {
        val end = nowInZone.toInstant().toEpochMilli()
        return TimelineWindow(
            startEpochMillis = end - durationMillis,
            endEpochMillis = end
        )
    }

    companion object {
        fun windowFor(
            range: TimelineRange,
            nowEpochMillis: Long,
            zoneId: ZoneId
        ): TimelineWindow {
            val clock = Clock.fixed(Instant.ofEpochMilli(nowEpochMillis), zoneId)
            return TimelineCalculator(clock = clock, zoneId = zoneId).windowFor(range)
        }
    }
}
