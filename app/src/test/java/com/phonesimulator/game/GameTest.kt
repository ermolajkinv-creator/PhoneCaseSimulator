package com.phonesimulator.game

import org.junit.Assert.*
import org.junit.Test

class GameTest {
    @Test fun exactDropDistribution() {
        val counts = (0..999).map(Catalog::roll).groupingBy { it }.eachCount()
        Catalog.phones.forEachIndexed { index, phone -> assertEquals(phone.tickets, counts[index]) }
        assertEquals(1, counts[5])
        assertTrue(Catalog.phones[5].description.contains("2026"))
        assertFalse(Catalog.phones[5].description.contains("Вымышленная"))
        assertEquals("OnePlus 15", Catalog.phones[24].name)
        assertEquals(Rarity.LEGENDARY, Catalog.phones[24].rarity)
        assertEquals(2, counts[24])
        assertEquals(Rarity.ULTRA, Catalog.phones[5].rarity)
        assertEquals("Samsung Z Fold 8 Ultra", Catalog.phones[5].name)
    }
    @Test fun paymentAndRewardAreOneTransaction() {
        val (next, phone) = Progress(100).open(999)!!
        assertEquals(0, next.coins)
        assertEquals(listOf(phone), next.owned)
        assertEquals(5, phone)
        assertNull(next.open(0))
        assertEquals(Progress(25000), next.sell(5))
        assertEquals(next, next.sell(0))
    }
    @Test fun legacySaveAndMalformedIndices() {
        assertEquals(Progress(700, listOf(0, 5, 5)), Progress.restore(700, "0,5,5"))
        assertEquals(Progress(0, listOf(2)), Progress.restore(-1, "bad,-1,99,2,"))
        assertEquals(Progress(1000), Progress.restore(1000, ""))
    }
    @Test fun coinsNeverOverflow() {
        assertEquals(Int.MAX_VALUE, Progress(Int.MAX_VALUE).bonus().coins)
        assertEquals(Int.MAX_VALUE, Progress(Int.MAX_VALUE, listOf(5)).sell(5).coins)
    }
    @Test fun expandedCatalogAndAdminGrantPreserveProgress() {
        assertEquals(40, Catalog.phones.size)
        assertEquals(1000, Catalog.phones.sumOf { it.tickets })
        val original = Progress(777, listOf(0, 5, 5))
        var granted = original
        Catalog.phones.indices.forEach { granted = granted.grant(it) }
        assertEquals(777, granted.coins)
        assertEquals(original.owned + Catalog.phones.indices.toList(), granted.owned)
        assertEquals(granted, Progress.restore(granted.coins, granted.owned.joinToString(",")))
        Catalog.phones.forEach {
            assertTrue(it.chip.isNotBlank() && it.display.isNotBlank() && it.system.isNotBlank())
            assertTrue(it.description.isNotBlank())
        }
    }
    @Test fun adminPasswordCheck() {
        assertFalse(AdminAccess.accepts(""))
        assertFalse(AdminAccess.accepts("wrong-password"))
        assertFalse(AdminAccess.accepts("prism-8426"))
        assertTrue(AdminAccess.accepts("Prism-8426"))
    }
    @Test(expected = IllegalArgumentException::class)
    fun rejectsInvalidTicket() { Catalog.roll(1000) }
}
