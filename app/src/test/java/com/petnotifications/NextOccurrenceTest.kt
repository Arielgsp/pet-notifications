package com.petnotifications

import com.petnotifications.data.nextOccurrence
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class NextOccurrenceTest {
    private val zone = ZoneId.of("America/Argentina/Buenos_Aires")
    private fun at(s: String) = LocalDateTime.parse(s).atZone(zone).toInstant().toEpochMilli()

    @Test
    fun oneShotIsUnchanged() {
        assertEquals(at("2026-09-20T10:00"), nextOccurrence(at("2026-09-20T10:00"), Repeat.NONE, at("2026-09-22T12:00"), zone))
    }

    @Test
    fun dailySkipsToFirstFutureDay() {
        assertEquals(at("2026-09-23T10:00"), nextOccurrence(at("2026-09-20T10:00"), Repeat.DAILY, at("2026-09-22T12:00"), zone))
    }

    @Test
    fun dailyExactlyNowMovesForward() {
        assertEquals(at("2026-09-23T10:00"), nextOccurrence(at("2026-09-22T10:00"), Repeat.DAILY, at("2026-09-22T10:00"), zone))
    }

    @Test
    fun weeklyKeepsWeekday() {
        assertEquals(at("2026-09-27T08:30"), nextOccurrence(at("2026-09-13T08:30"), Repeat.WEEKLY, at("2026-09-22T12:00"), zone))
    }
}
