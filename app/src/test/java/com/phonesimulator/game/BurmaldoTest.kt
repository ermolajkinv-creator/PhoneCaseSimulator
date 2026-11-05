package com.phonesimulator.game
import org.junit.Assert.*
import org.junit.Test
class BurmaldoTest {
    @Test fun fiveUltrasHaveExactOddsInEveryCaseAndSeason(){
        val expected=mapOf(Catalog.NOKIA to 10,Catalog.BURMALDO to 10,Catalog.TELIFON to 10,Catalog.FOLD to 10,Catalog.PALAS to 15)
        assertEquals(expected.keys,Catalog.ultraIndices.toSet())
        val rolls=CaseKind.entries.map { kind -> { ticket:Int -> kind.roll(ticket) } } +
            (1..12).map { month -> val season=Season("test","Test",Long.MAX_VALUE,month); { ticket:Int -> season.roll(ticket) } }
        for(roll in rolls){
            val counts=(0 until Catalog.TOTAL_TICKETS).map(roll).groupingBy { it }.eachCount()
            expected.forEach { (id,count)->assertEquals(count,counts[id]);assertEquals(Rarity.ULTRA,Catalog.phones[id].rarity) }
            assertEquals(55,counts.filterKeys { Catalog.isUltra(it) }.values.sum())
        }
        assertEquals(556,Catalog.phones.size)
        assertEquals("Бурмалдофон",Catalog.phones[554].name)
        assertEquals("ТелИфон",Catalog.phones[555].name)
    }
    @Test fun promotedAndNewUltrasPreserveInstancesAndCannotBeBoughtNormally(){
        val old=Progress.restore(200000,"0,5,500,553,554").identifyPhones()
        assertEquals(listOf(0,5,500,553,554),old.owned)
        val new=old.appendPhone(Catalog.TELIFON).identifyPhones()
        assertEquals(old.devices().map { it.key },new.devices().take(5).map { it.key })
        assertEquals(new.owned,Progress.restore(new.coins,new.owned.joinToString(",")).owned)
        for(id in listOf(Catalog.NOKIA,Catalog.BURMALDO,Catalog.TELIFON)){
            assertEquals(new,new.buyNew(id))
            assertEquals(new,new.buyStock(id,StockMarket.slot(1000L),1000L))
            assertEquals(listOf("Заводская"),Workshop.options(Catalog.phones[id]))
            assertEquals(new.coins+Catalog.phones[id].price,new.sell(id).coins)
        }
    }
}
