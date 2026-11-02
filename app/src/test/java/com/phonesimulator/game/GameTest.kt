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
        assertEquals(Catalog.phones[24].tickets, counts[24])
        assertEquals(Rarity.ULTRA, Catalog.phones[5].rarity)
        assertEquals("Samsung Z Fold 8 Ultra", Catalog.phones[5].name)
    }
    @Test fun paymentAndRewardAreOneTransaction() {
        val (next, phone) = Progress(CaseKind.CLASSIC.cost).open(999)!!
        assertEquals(0, next.coins)
        assertEquals(listOf(phone), next.owned)
        assertEquals(5, phone)
        assertNull(next.open(0))
        assertEquals(Catalog.phones[5].price, next.sell(5).coins)
        assertEquals(next, next.sell(0))
    }
    @Test fun legacySaveAndMalformedIndices() {
        assertEquals(Progress(700, listOf(0, 5, 5)), Progress.restore(700, "0,5,5"))
        assertEquals(Progress(0, listOf(2)), Progress.restore(-1, "bad,-1,999,2,"))
        assertEquals(Progress(1000), Progress.restore(1000, ""))
    }
    @Test fun coinsNeverOverflow() {
        assertEquals(Int.MAX_VALUE, Progress(Int.MAX_VALUE).bonus().coins)
        assertEquals(Int.MAX_VALUE, Progress(Int.MAX_VALUE, listOf(5)).sell(5).coins)
    }
    @Test fun expandedCatalogAndAdminGrantPreserveProgress() {
        assertEquals(100, Catalog.phones.size)
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
    @Test fun allCasesAndMarketTransactions() {
        CaseKind.entries.forEach { kind ->
            val results = (0..999).map(kind::roll)
            assertEquals(1, results.count { it == 5 })
            assertTrue(results.all { Catalog.phones[it].rarity.ordinal >= kind.minimum.ordinal })
            assertNull(Progress(0).openCase(kind, 0))
            assertEquals(0, Progress(kind.cost).openCase(kind, 999)!!.first.coins)
        }
        val listing = Listing("test", 1, 1, 80)
        val bought = Progress(100).buy(listing)!!
        assertEquals(20, bought.coins)
        assertEquals(listOf(UsedPhone(1, 1)), bought.used)
        assertNull(bought.buy(listing))
        assertEquals(20 + Market.price(1, 1), bought.sellUsed(0).coins)
        assertTrue(bought.sellUsed(0).used.isEmpty())
        assertEquals(Market.listings(10), Market.listings(10))
        assertEquals(12, Market.listings(10).map { it.id }.toSet().size)
        assertTrue(Market.price(1, 0) < Market.price(1, 3))
    }
    @Test fun firmwareAndConcreteCatalog() {
        assertEquals(100, Catalog.phones.map { it.name }.toSet().size)
        assertEquals("iPhone 17 Pro", Catalog.phones[4].name)
        assertEquals("Google Pixel 10 Pro", Catalog.phones[2].name)
        assertTrue(Catalog.phones.all { it.price in 1000..300000 })
        assertTrue("Джейлбрейк (симуляция)" in Workshop.options(Catalog.phones[4]))
        assertFalse("LineageOS (симуляция)" in Workshop.options(Catalog.phones[4]))
        assertEquals(listOf("Заводская"), Workshop.options(Catalog.phones[0]))
        val state = Progress(777, listOf(1, 4)).tune(1, "LineageOS (симуляция)")
        assertEquals(777, state.coins)
        assertEquals("LineageOS (симуляция)", state.firmware[1])
    }
    @Test fun economyTracksExactRewards() {
        assertEquals(listOf(31000, 45000, 55000, 97000), CaseKind.entries.map { it.cost })
        CaseKind.entries.forEach { kind ->
            val expected = (0..999).sumOf { Catalog.phones[kind.roll(it)].price.toLong() } / 1000.0
            assertTrue(kind.cost >= expected * 1.15)
            assertTrue(kind.cost < expected * 1.15 + 1000)
            assertNull(Progress(kind.cost - 1).openCase(kind, 999))
        }
        assertTrue(Economy.START_BALANCE >= 2 * CaseKind.CLASSIC.cost)
        assertTrue(Economy.BONUS >= CaseKind.CLASSIC.cost)
        assertEquals(777, Progress.restore(777, "0,5").coins)
    }
    @Test fun povertyGiftChecksAllPurchasesAndCannotRepeatWhileAffordable() {
        val listing = Listing("cheap", 0, 0, 500)
        val poor = Progress(0, listOf(5))
        assertTrue(poor.canReceiveGift(listOf(listing)))
        val rescued = poor.receiveGift(listOf(listing))
        assertEquals(Economy.BONUS, rescued.coins)
        assertEquals(poor.owned, rescued.owned)
        assertFalse(rescued.canReceiveGift(listOf(listing)))
        assertEquals(rescued, rescued.receiveGift(listOf(listing)))
        assertFalse(Progress(500).canReceiveGift(listOf(listing)))
        assertFalse(Progress(CaseKind.CLASSIC.cost).canReceiveGift(emptyList()))
        assertTrue(Progress(500, purchased = setOf("cheap")).canReceiveGift(listOf(listing)))
        assertTrue(Progress(499).canReceiveGift(listOf(listing)))
        assertEquals(Progress(500), Progress(500).receiveGift(listOf(listing)))
    }
    @Test fun sharedGiftCooldownHasExactBoundaryAndCannotBeBypassed() {
        val time = 1_000_000L
        val first = Progress(0).receiveGift(emptyList(), time)
        assertEquals(time, first.lastGiftAt)
        val spent = first.copy(coins = 0)
        assertEquals(Economy.GIFT_COOLDOWN, spent.giftCooldownRemaining(time))
        assertEquals(1L, spent.giftCooldownRemaining(time + Economy.GIFT_COOLDOWN - 1))
        assertFalse(spent.canReceiveGift(emptyList(), time + 1))
        assertEquals(spent, spent.receiveGift(emptyList(), time + 1))
        assertEquals(spent, spent.bonus(time + 1))
        assertTrue(spent.canReceiveGift(emptyList(), time + Economy.GIFT_COOLDOWN))
        assertEquals(Economy.BONUS, spent.receiveGift(emptyList(), time + Economy.GIFT_COOLDOWN).coins)
        assertEquals(Economy.GIFT_COOLDOWN, spent.giftCooldownRemaining(time - 1000))
        assertEquals(0L, Progress.restore(1000, "").giftCooldownRemaining(time))
        val shop = Progress(0).bonus(time).copy(coins = 0)
        assertFalse(shop.canReceiveGift(emptyList(), time + 1))
    }
    @Test fun dailyClickerFavoritesAndUpgradeRules() {
        val start = Progress(1000, listOf(0,0))
        val day1 = start.claimDaily(100)
        assertEquals(1000 + Economy.BONUS, day1.coins)
        assertEquals(day1, day1.claimDaily(100))
        assertEquals(day1, day1.claimDaily(99))
        val day2 = day1.claimDaily(101)
        assertEquals(2, day2.dailyStreak)
        assertEquals(day2.coins + Economy.BONUS, day2.claimDaily(103).coins)
        assertEquals(1, day2.claimDaily(103).dailyStreak)
        assertEquals(Progress(1010, listOf(0,0), clicks = 1, questDay = 100, questClicks = 1), start.clickCoin(100))
        assertTrue(0 in start.toggleFavorite(0).favorites)
        assertEquals(start, start.toggleFavorite(0).toggleFavorite(0))
        val chance = start.upgradeChance(0, 1)
        val outcomes = (0..999).map { start.upgrade(0,1,it) }
        assertEquals(chance, outcomes.count { it.second })
        assertEquals(listOf(0,1), outcomes.first().first.owned)
        assertEquals(listOf(0), outcomes.last().first.owned)
        assertTrue(outcomes.all { it.first.coins == start.coins })
        assertTrue("clicks" in Progression.goals(start.copy(clicks = 100)))
        assertTrue("ultra" in Progression.goals(Progress(owned = listOf(5))))
    }
    @Test fun adminMoneyAndDailyQuests() {
        assertEquals(Int.MAX_VALUE, Progress(777).adminCredit(Int.MAX_VALUE.toLong()).coins)
        assertEquals(877, Progress(777).adminCredit(100).coins)
        var state = Progress(0)
        repeat(100) { state = state.clickCoin(100) }
        val claimed = state.claimQuest("clicks", 100)
        assertEquals(6000, claimed.coins)
        assertEquals(claimed, claimed.claimQuest("clicks", 100))
        assertEquals(claimed, claimed.claimQuest("cases", 100))
        val tomorrow = claimed.forQuestDay(101)
        assertEquals(0, tomorrow.questClicks)
        assertTrue(tomorrow.claimedQuests.isEmpty())
        assertEquals(tomorrow, tomorrow.claimQuest("clicks", 100))
        var cases = Progress(3 * CaseKind.CLASSIC.cost)
        repeat(3) { cases = cases.openCase(CaseKind.CLASSIC, 0, 100)!!.first }
        assertEquals(10000, cases.claimQuest("cases",100).coins)
    }
    @Test fun clientsAreTimedIdempotentAndSeparateFromInventory() {
        val start = Progress(50000, listOf(5))
        val offer = Clients.offer(0, 1)
        val accepted = start.acceptClient(0, 0)
        val job = accepted.clientJob!!
        assertEquals(start.coins-offer.cost, accepted.coins)
        assertEquals(accepted, accepted.acceptClient(0, 1))
        assertEquals(accepted.coins, accepted.finishClient(job.readyAt-1).coins)
        val done = accepted.finishClient(job.readyAt)
        assertEquals(accepted.coins+offer.reward, done.coins)
        assertEquals(start.owned, done.owned)
        assertEquals(start.firmware, done.firmware)
        assertEquals(1, done.reputation)
        assertEquals(1, done.servicesCompleted)
        assertNull(done.clientJob)
        assertEquals(done, done.finishClient(job.readyAt))
        assertNull(done.acceptClient(0, job.readyAt).clientJob)
        assertNull(Progress(0).acceptClient(0,0).clientJob)
        assertNull(start.acceptClient(2,2*Clients.INTERVAL).clientJob)
        assertNull(start.acceptClient(3,3*Clients.INTERVAL).clientJob)
        assertEquals(3, Progress().clientOffers(10*Clients.INTERVAL).size)
        val future = Progress().syncClients(100*Clients.INTERVAL)
        assertTrue(future.clientOffers(0).isEmpty())
        assertEquals(100L, future.syncClients(0).serviceSlot)
        assertEquals(2, Progress(50000, reputation = 3).improveWorkshop().workshopLevel)
        assertEquals(40000, Progress(50000, reputation = 3).improveWorkshop().coins)
        assertEquals(1, Progress(50000).improveWorkshop().workshopLevel)
        assertEquals(5, Progress(50000, workshopLevel = 5, reputation = 100).improveWorkshop().workshopLevel)
        assertTrue(Catalog.phones[Clients.offer(3,3).phone].name.startsWith("iPhone"))
        assertTrue("LineageOS (симуляция)" in Workshop.options(Catalog.phones[Clients.offer(2,2).phone]))
    }
    @Test fun versionThreeTradingAwardsContractsAndSafe() {
        val rich=Progress(1_000_000)
        val bought=rich.buyNew(0)
        assertEquals(listOf(0),bought.owned)
        assertEquals(rich.coins-ExpansionRules.newPrice(0),bought.coins)
        assertEquals(rich,rich.buyNew(5))
        val lot=Listing("x",0,0,500)
        val discounted=rich.bargain(lot,0)
        assertEquals(425,discounted.askPrice(lot))
        assertEquals(discounted,discounted.bargain(lot,99))
        assertEquals(rich.coins,discounted.coins)
        assertFalse(discounted.copy(coins=450).canReceiveGift(listOf(lot),1000))
        val broken=Progress(10000,used=listOf(UsedPhone(0,0)))
        val repaired=broken.repairUsed(0)
        assertEquals(1,repaired.used.single().condition)
        assertEquals(broken.coins-broken.repairPrice(0),repaired.coins)
        val award=Progress(0,listOf(0)).claimAward("first")
        assertEquals(1000,award.coins)
        assertEquals(award,award.claimAward("first"))
        val inputs=Progress(100000,listOf(0,0,0))
        val exchanged=inputs.contract(Rarity.COMMON,0)
        assertEquals(1,exchanged.owned.size)
        assertEquals(Rarity.UNCOMMON,Catalog.phones[exchanged.owned.single()].rarity)
        assertEquals(inputs.coins-inputs.contractFee(Rarity.COMMON),exchanged.coins)
        val vault=rich.moveToVault(1000)
        assertEquals(rich,vault.withdrawVault(1000))
        assertEquals(0L,vault.expansion.record(rich,vault,100,"Сейф").income)
        val ledger=bought.expansion.record(rich,bought,100,"Покупка")
        assertEquals(ExpansionRules.newPrice(0).toLong(),ledger.expenses)
        val day=100L;val wanted=ExpansionRules.deliveryModel(day)
        val delivered=Progress(0,listOf(wanted)).deliver(day)
        assertEquals(ExpansionRules.deliveryPrice(day),delivered.coins)
        assertTrue(delivered.owned.isEmpty())
        assertEquals(delivered,delivered.deliver(day))
        val idle=Progress(0).claimIdle(1000)
        assertEquals(idle,idle.claimIdle(60999))
        val income=idle.claimIdle(61000)
        assertEquals(100,income.coins)
        assertEquals(income,income.claimIdle(61000))
        assertEquals(12000,idle.claimIdle(1000+999*60000L).coins)
        assertEquals(Progress(0),Progress(0).buyNew(0))
    }
    @Test(expected = IllegalArgumentException::class)
    fun cannotUpgradeToUltra() { Progress(owned = listOf(0)).upgrade(0,5,0) }
    @Test(expected = IllegalArgumentException::class)
    fun rejectsInvalidTicket() { Catalog.roll(1000) }
}
