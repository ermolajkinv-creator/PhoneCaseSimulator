package com.phonesimulator.game
import org.junit.Assert.*
import org.junit.Test
class EmpireTest {
    private fun act(p:Progress,f:EmpireFeature,c:Int=0,t:Long=1000000)=EmpireRules.act(p,f,c,t).progress
    @Test fun expensesAreAtomicAndAllFeaturesHaveDistinctRoutes(){
        assertEquals(25,EmpireFeature.entries.size)
        val poor=Progress(0)
        for(f in listOf(EmpireFeature.ROOM,EmpireFeature.TOOLS,EmpireFeature.PARCEL,EmpireFeature.PARTS,EmpireFeature.STAFF,EmpireFeature.WARRANTY))assertEquals(poor,act(poor,f))
        assertEquals(556,Catalog.phones.size)
    }
    @Test fun diagnosisRepairPartsAndRewardsAreOneTransaction(){
        var p=act(Progress(20000),EmpireFeature.PARTS)
        p=act(p,EmpireFeature.DIAGNOSIS)
        assertEquals(p,act(p,EmpireFeature.SCREEN))
        val wrong=act(p,EmpireFeature.DIAGNOSIS,2)
        assertEquals(p.coins-200,wrong.coins)
        p=act(wrong,EmpireFeature.DIAGNOSIS,1)
        assertEquals(p,act(p,EmpireFeature.DIAGNOSIS,1))
        val done=act(p,EmpireFeature.SCREEN)
        assertEquals(p.coins+1500,done.coins);assertEquals(4L,done.world.empire["parts"])
        assertEquals(1L,done.world.empire["jobs"]);assertEquals(done,act(done,EmpireFeature.SCREEN))
    }
    @Test fun paidDeliveriesSurviveFullStorageAndCannotBeClaimedTwice(){
        val start=act(Progress(30000),EmpireFeature.DELIVERY)
        assertEquals(27000,start.coins);assertEquals(start,act(start,EmpireFeature.DELIVERY))
        val full=start.copy(world=start.world.copy(empire=start.world.empire.copy(values=start.world.empire.values+mapOf("parts" to 95L))))
        assertEquals(full,act(full,EmpireFeature.DELIVERY,t=1060000))
        val done=act(start,EmpireFeature.DELIVERY,t=1060000)
        assertEquals(10L,done.world.empire["parts"]);assertEquals(0L,done.world.empire["delivery"])
        val reordered=act(done,EmpireFeature.DELIVERY,t=1060000)
        assertEquals(done.coins-3000,reordered.coins);assertEquals(10L,reordered.world.empire["parts"])
    }
    @Test fun employeesFreezePayAtStartAndPreserveUnclaimedMoney(){
        val hired=act(Progress(100000),EmpireFeature.STAFF)
        val work=act(hired,EmpireFeature.STAFF)
        val trained=act(work,EmpireFeature.TRAINING)
        val full=trained.copy(coins=Int.MAX_VALUE)
        assertEquals(full,act(full,EmpireFeature.STAFF,t=1060000))
        val done=act(trained,EmpireFeature.STAFF,t=1060000)
        assertEquals(trained.coins+1200,done.coins)
        assertEquals(0L,done.world.empire["work"])
    }
    @Test fun protectedPhonesAndDailyRewardsCannotBeDuplicated(){
        val ultra=Progress(10000,listOf(5,500));assertEquals(ultra,act(ultra,EmpireFeature.RECYCLE,0))
        val donor=Progress(10000,listOf(100));val recycled=act(donor,EmpireFeature.RECYCLE,0)
        assertTrue(recycled.owned.isEmpty());assertEquals(3L,recycled.world.empire["parts"])
        assertEquals(recycled,act(recycled,EmpireFeature.RECYCLE,0))
        val start=act(Progress(20000),EmpireFeature.DAILY)
        val ready=start.copy(world=start.world.copy(empire=start.world.empire.copy(values=start.world.empire.values+mapOf("jobs" to 3L))))
        val paid=act(ready,EmpireFeature.DAILY);assertEquals(23000,paid.coins);assertEquals(paid,act(paid,EmpireFeature.DAILY))
    }
}
