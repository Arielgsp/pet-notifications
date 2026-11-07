package com.petnotifications

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/** El gorro navideño de regalo va por fecha: del 1/12 al 6/1, una sola vez. */
class SeasonGiftTest {

    private fun due(y: Int, m: Int, d: Int, have: Set<Accessory> = emptySet()) =
        Outfit.giftsDue(LocalDate.of(y, m, d), have)

    @Test
    fun christmasHatInsideTheSeason() {
        assertEquals(listOf(Accessory.XMAS_HAT), due(2026, 12, 1))
        assertEquals(listOf(Accessory.XMAS_HAT), due(2026, 12, 24))
        assertEquals(listOf(Accessory.XMAS_HAT), due(2027, 1, 6))
    }

    @Test
    fun nothingOutsideTheSeason() {
        assertEquals(emptyList<Accessory>(), due(2026, 11, 30))
        assertEquals(emptyList<Accessory>(), due(2027, 1, 7))
        assertEquals(emptyList<Accessory>(), due(2027, 7, 15))
    }

    @Test
    fun onlyOnce() {
        assertEquals(emptyList<Accessory>(), due(2026, 12, 10, have = setOf(Accessory.XMAS_HAT)))
    }
}
