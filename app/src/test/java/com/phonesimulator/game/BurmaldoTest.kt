package com.phonesimulator.game
import org.junit.Assert.*
import org.junit.Test
class BurmaldoTest {
    @Test fun newMemeIsBuyableReachableAndDoesNotAddAnUltra(){
        val id=Catalog.BURMALDO;val phone=Catalog.phones[id]
        assertEquals(554,id);assertEquals("Бурмалдофон",phone.name)
        assertEquals(Rarity.EPIC,phone.rarity);assertEquals(42000,phone.price)
        assertEquals(listOf("Заводская"),Workshop.options(phone))
        assertEquals(listOf(5,500),Catalog.ultraIndices)
        for(kind in CaseKind.entries){
            val outcomes=(0 until 10000).map(kind::roll)
            assertTrue(outcomes.contains(id));assertEquals(10,outcomes.count { it==5 });assertEquals(15,outcomes.count { it==500 })
        }
        val bought=Progress(200000,listOf(5,500,553)).buyNew(id)
        assertEquals(listOf(5,500,553,id),bought.owned)
        assertEquals(bought.owned,Progress.restore(bought.coins,bought.owned.joinToString(",")).owned)
        assertEquals(bought.coins+42000,bought.sell(id).coins)
    }
}
