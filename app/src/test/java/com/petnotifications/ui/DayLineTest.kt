package com.petnotifications.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneOffset

/** La lógica de la línea del día en Avisos. */
class DayLineTest {

    private val zone = ZoneOffset.UTC
    private fun at(h: Int, m: Int) = LocalDateTime.of(2026, 11, 18, h, m).toInstant(zone).toEpochMilli()
    private fun item(h: Int, m: Int, done: Boolean = false) = LineItem("k$h$m", at(h, m), "x", done = done)

    @Test
    fun statesAroundNow() {
        val items = listOf(item(8, 0, done = true), item(9, 0), item(15, 0), item(18, 0))
        assertEquals(
            listOf(DayState.DONE, DayState.MISSED, DayState.NEXT, DayState.LATER),
            dayStates(items, at(14, 52), isToday = true),
        )
        assertEquals(2, nowIndex(items, at(14, 52)))
    }

    @Test
    fun aFinishedUpcomingItemIsNotTheNextOne() {
        val items = listOf(item(15, 0, done = true), item(16, 0))
        assertEquals(listOf(DayState.DONE, DayState.NEXT), dayStates(items, at(14, 0), isToday = true))
    }

    @Test
    fun otherDaysHaveNoMissedNorNext() {
        val items = listOf(item(8, 0), item(20, 0))
        assertEquals(listOf(DayState.LATER, DayState.LATER), dayStates(items, at(14, 0), isToday = false))
    }

    @Test
    fun nextSlotIsAnHourAtLeastHalfAnHourAway() {
        assertEquals(at(16, 0), nextHourSlot(at(14, 52), zone))
        assertEquals(at(15, 0), nextHourSlot(at(14, 30), zone))
        assertEquals(at(15, 0), nextHourSlot(at(14, 10), zone))
        assertNull(nextHourSlot(at(23, 40), zone))
    }

    @Test
    fun countdownRoundsUp() {
        assertEquals(0 to 25, countdownParts(at(15, 17), at(14, 52)))
        assertEquals(1 to 8, countdownParts(at(16, 0), at(14, 52)))
        assertEquals(3 to 0, countdownParts(at(18, 0), at(15, 0)))
        assertEquals(0 to 1, countdownParts(at(15, 0), at(15, 0) - 5_000))
    }

    @Test
    fun iconsComeFromTheTitle() {
        assertEquals(DayIcon.PHONE, dayIconFor("Llamar a mamá"))
        assertEquals(DayIcon.SCALE, dayIconFor("Mediación Ferretti"))
        assertEquals(DayIcon.CAKE, dayIconFor("Cumpleaños de Ana"))
        assertEquals(DayIcon.PILL, dayIconFor("Tomar la pastilla"))
        assertEquals(DayIcon.PLANT, dayIconFor("Regar las plantas"))
        assertEquals(DayIcon.MEETING, dayIconFor("Reuniones"))
        assertEquals(DayIcon.PAW, dayIconFor("Ir a la calle"))
        assertEquals(DayIcon.CALENDAR, dayIconFor("Llamar a mamá", calendar = true))
    }
}
