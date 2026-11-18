package com.petnotifications.overlay

import org.junit.Assert.assertEquals
import org.junit.Test

/** Las flechitas de los minutos del recordatorio rápido van de 5 en 5 y pasan de hora. */
class QuickTimeStepTest {

    private fun up(h: Int, m: Int) = stepTime(minutesUp(h, m))
    private fun down(h: Int, m: Int) = stepTime(minutesDown(h, m))
    private fun at(h: Int, m: Int) = h * 60 + m

    @Test
    fun goesToTheNextMultipleOfFive() {
        assertEquals(at(14, 10), up(14, 7))
        assertEquals(at(14, 5), up(14, 0))
        assertEquals(at(14, 5), down(14, 7))
        assertEquals(at(14, 5), down(14, 10))
    }

    @Test
    fun crossesTheHour() {
        assertEquals(at(15, 0), up(14, 55))
        assertEquals(at(13, 55), down(14, 0))
    }

    @Test
    fun staysInsideTheDay() {
        assertEquals(at(0, 0), up(23, 55))
        assertEquals(at(23, 55), down(0, 0))
    }
}
