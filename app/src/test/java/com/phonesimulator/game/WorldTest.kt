package com.phonesimulator.game

import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class WorldTest {
    private val now=Instant.parse("2026-11-03T12:00:00Z").toEpochMilli()
    private fun phone(model: Int=1,trait: PhoneTraits=PhoneTraits()): Progress = Progress(2_000_000).appendPhone(model,trait).identifyPhones()

    @Test fun mutationWeightsAndUltraOddsAreIndependent() {
        val counts=(0..9999).map(Mutation::roll).groupingBy { it }.eachCount()
        assertEquals(listOf(8200,800,400,300,200,100),Mutation.entries.map { counts[it] })
        for(mutation in listOf(0,8200,9000,9400,9700,9900,9999)){
            val palas=Progress(2_000_000).openCase(CaseKind.CLASSIC,9975,mutationTicket=mutation)!!
            val fold=Progress(2_000_000).openCase(CaseKind.CLASSIC,9999,mutationTicket=mutation)!!
            assertEquals(500,palas.second);assertEquals(5,fold.second)
            assertEquals(Mutation.roll(mutation),palas.first.traitsAt(0).mutation)
        }
        val loot=Progress(2_000_000).openCase(CaseKind.CLASSIC,0,lootTicket=9500)!!.first
        assertEquals(1,loot.world.appTokens)
    }
    @Test fun duplicatesKeepTheirOwnTraitsAndIdentity() {
        val p=Progress(100000).appendPhone(0,PhoneTraits(Mutation.GOLD,1)).appendPhone(0,PhoneTraits(Mutation.RAINBOW,3))
            .appendPhone(1).identifyPhones()
        val ids=p.devices().map { it.key }
        assertEquals(3,ids.toSet().size)
        val removed=p.removeDevice(ids[0])
        assertEquals(listOf(0,1),removed.owned)
        assertEquals(Mutation.RAINBOW,removed.traitsAt(0).mutation)
        assertEquals(ids[1],removed.devices()[0].key)
        val sold=removed.sell(0)
        assertEquals(listOf(1),sold.owned)
        assertEquals(removed.coins+4400,sold.coins)
        assertEquals(Mutation.NORMAL,sold.traitsAt(0).mutation)
        assertEquals(sold,sold.sellDevice(ids[1]))
    }
    @Test fun repairsRaiseOneInstancesValueAndCannotRepeatAtMaximum() {
        var p=phone(1,PhoneTraits(Mutation.DIAMOND,1))
        val key=p.devices().first().key
        val before=p.device(key)!!.value
        val fee=p.repairOwnedPrice(0)
        p=p.repairDevice(key)
        assertEquals(2,p.traitsAt(0).condition)
        assertTrue(p.device(key)!!.value>before)
        assertEquals(2_000_000-fee,p.coins)
        p=p.repairDevice(key)
        assertEquals(3,p.traitsAt(0).condition)
        assertEquals(p,p.repairDevice(key))
        val poor=phone(1,PhoneTraits(Mutation.GOLD,1)).copy(coins=0)
        assertEquals(poor,poor.repairDevice(poor.devices().first().key))
    }
    @Test fun multiOpeningIsAtomicAndRetainsEachResult() {
        for(count in listOf(3,5,10)){
            val cost=CaseKind.CLASSIC.cost*count
            val tickets=List(count){if(it%2==0)9975 else 9999}
            assertNull(Progress(cost-1).openMany(CaseKind.CLASSIC,count,tickets,List(count){9900},List(count){0},1))
            val result=Progress(cost).openMany(CaseKind.CLASSIC,count,tickets,List(count){9900},List(count){0},1)!!
            assertEquals(0,result.first.coins);assertEquals(count,result.first.owned.size)
            assertEquals(count,result.first.casesOpened);assertEquals(3,result.first.questCases)
            assertEquals(count,result.second.map { it.key }.toSet().size)
            assertTrue(result.second.all { it.traits.mutation==Mutation.RAINBOW && it.traits.condition==1 })
            assertTrue(result.first.owned.all { it==5 || it==500 })
        }
    }
    @Test fun exchangeHasSpreadAndRejectsExpiredQuotesAndUltraPurchases() {
        val base=Progress(2_000_000);val slot=StockMarket.slot(now)
        assertEquals(base,base.buyStock(1,slot-1,now))
        assertEquals(base,base.buyStock(500,slot,now))
        val bought=base.buyStock(1,slot,now).identifyPhones()
        val key=bought.devices().first().key
        val sold=bought.sellStock(key,slot,now)
        assertTrue(sold.coins<base.coins);assertTrue(sold.owned.isEmpty())
        assertEquals(bought,bought.sellStock(key,slot+1,now))
        assertTrue((0L..50L).map { StockMarket.factor(1,it) }.toSet().size>1)
    }
    @Test fun appsArePerInstanceAndTokensDoNotChargeCoins() {
        val p=Progress(2_000_000).appendPhone(1).appendPhone(1).identifyPhones()
        val a=p.devices()[0].key;val b=p.devices()[1].key
        val paid=p.unlockApp(a,"snake",false)
        assertEquals(p.coins-3000,paid.coins)
        assertTrue("snake" in paid.virtualProfile(a).apps)
        assertFalse("snake" in paid.virtualProfile(b).apps)
        val flashed=paid.flashVirtualPhone(a,"LineageOS (симуляция)")
        assertEquals("LineageOS (симуляция)",flashed.virtualProfile(a).firmware)
        assertEquals("",flashed.virtualProfile(b).firmware)
        assertEquals(flashed,flashed.flashVirtualPhone(a,"unsupported"))
        assertEquals(paid,paid.unlockApp(a,"snake",false))
        val token=paid.copy(world=paid.world.copy(appTokens=1)).unlockApp(b,"gallery",true)
        assertEquals(paid.coins,token.coins);assertEquals(0,token.world.appTokens)
        assertTrue("gallery" in token.virtualProfile(b).apps)
        assertEquals(token,token.customize(a,"theme","glitch"))
        val colored=token.customize(b,"color","2")
        assertEquals(VirtualApps.colors[2],colored.virtualProfile(b).color)
        assertEquals(colored,colored.customize(b,"color","2"))
    }
    @Test fun auctionEscrowsAnItemAndPaysExactlyOnce() {
        val start=phone(1,PhoneTraits(Mutation.GOLD,3))
        val key=start.devices()[0].key
        val listed=start.listAuction(key,80,now)
        assertNull(listed.device(key));assertEquals(1,listed.world.auctions.size)
        assertTrue(listed.coins<start.coins)
        assertEquals(listed,listed.listAuction(key,80,now))
        val lot=listed.world.auctions.first()
        assertEquals(listed,listed.settleAuction(lot.id,lot.endsAt-1))
        val paid=listed.settleAuction(lot.id,lot.endsAt)
        assertEquals(listed.coins+AuctionRules.bid(lot,lot.endsAt),paid.coins)
        assertTrue(paid.world.auctions.isEmpty());assertNull(paid.device(key))
        assertEquals(paid,paid.settleAuction(lot.id,lot.endsAt))
    }
    @Test fun auctionWithoutBidsReturnsTheSamePhone() {
        val seed=phone(1,PhoneTraits(Mutation.RAINBOW,2))
        val key=seed.devices().first().key
        val start=(2L..100L).map { seed.copy(world=seed.world.copy(serial=it)) }
            .first { candidate -> candidate.listAuction(key,120,now).world.auctions.first().let { it.ceiling<it.reserve } }
        val listed=start.listAuction(key,120,now);val lot=listed.world.auctions.first()
        val returned=listed.settleAuction(lot.id,lot.endsAt)
        assertEquals(start.device(key),returned.device(key));assertEquals(listed.coins,returned.coins)
        assertEquals(returned,returned.settleAuction(lot.id,lot.endsAt))
    }
    @Test fun companyProducesIndependentVirtualDevicesAndDoesNotDuplicateProducts() {
        val created=Progress(2_000_000).createCompany("Friend Labs")
        assertEquals(1,created.world.company.level);assertEquals(1_970_000,created.coins)
        assertEquals(created,created.createCompany("Other"))
        val pending=created.startProduction(now)
        assertEquals(pending,pending.startProduction(now))
        assertEquals(pending,pending.collectProduction(now+119999))
        val made=pending.collectProduction(now+120000)
        assertEquals(1,made.world.company.phones.size);assertEquals(1,made.world.company.produced)
        val item=made.devices().first();assertTrue(item.key.startsWith("company:"));assertEquals("Friend Labs One 1",item.phone.name)
        assertEquals(made,made.collectProduction(now+120000))
        val sold=made.sellDevice(item.key)
        assertEquals(made.coins+item.value,sold.coins);assertTrue(sold.world.company.phones.isEmpty())
    }
    @Test fun minerOnlyCountsExplicitActiveTimeAndCannotBeClaimedTwice() {
        var p=phone();val key=p.devices()[0].key
        assertEquals(p,p.activateMiner(key))
        p=p.unlockApp(key,"wallet",false).activateMiner(key)
        p=p.advanceMiner(30000).advanceMiner(30000)
        assertEquals(60000L,p.world.miner.activeMs)
        val before=p.coins;val rate=p.minerRate()
        p=p.claimMining();assertEquals(before+rate,p.coins)
        assertEquals(p,p.claimMining())
        repeat(100){p=p.advanceMiner(30000)}
        assertEquals(1800000L,p.world.miner.activeMs)
        val missing=p.removeDevice(key)
        assertEquals(missing,missing.advanceMiner(1000));assertEquals(missing,missing.claimMining())
    }
    @Test fun albumsAndSecretCodesRewardOnce() {
        val start=Progress(0,listOf(0,1,6,7))
        val claimed=start.claimAlbum("start")
        assertEquals(5000,claimed.coins);assertEquals(1,claimed.world.appTokens)
        assertEquals(start.owned,claimed.owned);assertEquals(claimed,claimed.claimAlbum("start"))
        val retro=claimed.redeemCode(" prism-retro ")
        assertTrue("retro" in retro.world.skins);assertEquals(retro,retro.redeemCode("PRISM-RETRO"))
        assertEquals(retro,retro.redeemCode("MAST3R"))
        val expert=retro.copy(servicesCompleted=3).redeemCode("MAST3R")
        assertEquals(retro.coins+5000,expert.coins)
        assertEquals(expert,expert.redeemCode("MAST3R"))
    }
    @Test fun monthlyEventRetainsExactUltraOddsAndUnlocksItsSkin() {
        val season=Seasons.current(now)
        assertEquals("2026-11",season.id)
        assertEquals("2026-12",Seasons.current(season.endsAt).id)
        val results=(0..9999).map(season::roll)
        assertEquals(10,results.count { it==5 });assertEquals(15,results.count { it==500 })
        val base=Progress(2_000_000)
        assertNull(base.openSeasonal("old",0,0,9999,now))
        val opened=base.openSeasonal(season.id,9975,9900,9999,now)!!
        assertEquals(500,opened.second.model);assertEquals(Mutation.RAINBOW,opened.second.traits.mutation)
        assertTrue(season.skin in opened.first.world.seasonalSkins)
        assertEquals(base.coins-season.cost,opened.first.coins)
    }
    @Test fun virtualBattlesHaveCooldownAndNoEntryLoss() {
        val base=phone(3)
        val key=base.devices().first().key
        val (played,_)=base.fightNpc(key,0,now,7000)
        assertEquals(1,played.world.npcWins+played.world.npcLosses)
        assertTrue(played.coins>=base.coins)
        assertEquals(played,played.fightNpc(key,1,now+59999,10).first)
        val next=played.fightNpc(key,2,now+60000,10).first
        assertEquals(2,next.world.npcWins+next.world.npcLosses)
    }
    @Test fun fullWalletDoesNotConsumeItemsOrOneTimePayouts() {
        val full=phone().copy(coins=Int.MAX_VALUE)
        val key=full.devices().first().key
        assertEquals(full,full.sellDevice(key))
        assertEquals(full,full.sellStock(key,StockMarket.slot(now),now))
        val album=Progress(Int.MAX_VALUE,listOf(0,1,6,7))
        assertEquals(album,album.claimAlbum("start"))
        val listed=phone().listAuction(key,80,now)
        val lot=listed.world.auctions.first()
        val capped=listed.copy(coins=Int.MAX_VALUE)
        assertEquals(capped,capped.settleAuction(lot.id,lot.endsAt))
        assertEquals(Int.MAX_VALUE,full.money(Long.MAX_VALUE).coins)
        assertEquals(0,full.money(Long.MIN_VALUE).coins)
    }
    @Test fun snakeGrowsAndDetectsWallAndSelfCollisions() {
        val s=SnakeState(listOf(210,209,208),food=211)
        assertEquals(s,s.turn(3))
        val grown=s.tick(1)
        assertEquals(1,grown.score);assertEquals(4,grown.body.size);assertFalse(grown.food in grown.body)
        assertTrue(SnakeState(listOf(219,218,217),food=0).tick(1).over)
        assertTrue(SnakeState(listOf(210,230,231,211,212),direction=1,queued=2,food=0).tick(1).over)
        assertEquals(SnakeState.start(42),SnakeState.start(42))
    }
}
