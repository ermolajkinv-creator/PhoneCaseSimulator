package com.phonesimulator.game

import java.time.Instant
import java.time.YearMonth
import java.time.ZoneOffset
import kotlin.math.sqrt

object StockMarket {
    const val INTERVAL = 30*60*1000L
    fun slot(now: Long): Long { require(now >= 0); return now/INTERVAL }
    fun factor(model: Int, slot: Long): Int = 80 + Math.floorMod(model.toLong()*73471L + slot*7919L + (slot/3)*173L,41L).toInt()
    fun quote(model: Int, now: Long): Int = (Catalog.phones[model].price.toLong()*factor(model,slot(now))/100).toInt()
    fun buyPrice(model: Int, now: Long): Int = (quote(model,now).toLong()*110/100).toInt()
    fun sellPrice(item: DeviceSnapshot, now: Long): Int =
        (item.value.toLong()*factor(item.model,slot(now))*90/10000).toInt().coerceAtLeast(1)
}
data class Album(val id: String, val title: String, val models: Set<Int>, val reward: Int)
object Albums {
    val all: List<Album> by lazy {
        fun family(id: String,title: String,test: (String)->Boolean): Album {
            val models=Catalog.phones.indices.filter { test(Catalog.phones[it].name) }.toSet()
            val reward=(models.sumOf { Catalog.phones[it].price.toLong() }/10).coerceIn(5000L,150000L).toInt()
            return Album(id,title,models,reward)
        }
        listOf(Album("start","Первые знакомые",setOf(0,1,6,7),5000),
            Album("flagships","Флагманская четвёрка",setOf(3,4,24,25),20000),
            Album("ultras","Два Ultra",setOf(5,500),20000),
            family("galaxys","Все Galaxy S") { it.startsWith("Samsung Galaxy S") },
            family("iphone","Все iPhone") { it.startsWith("iPhone") },
            family("pixel","Все Pixel") { it.startsWith("Google Pixel") },
            family("oneplus","Клуб OnePlus") { it.startsWith("OnePlus") },
            family("nokia","История Nokia") { it.startsWith("Nokia") })
    }
}
object VirtualApps {
    val prices = linkedMapOf("snake" to 3000,"clicker" to 2000,"wallet" to 10000,"messages" to 1000,"gallery" to 2000)
    val titles = mapOf("snake" to "Змейка","clicker" to "Кликер","wallet" to "Крипто-кошелёк","messages" to "Сообщения","gallery" to "Галерея")
    val colors = listOf(0xFFC4C7D0.toInt(),0xFF67B7A4.toInt(),0xFFBB8DF3.toInt(),0xFFEB9B50.toInt(),0xFF5188D8.toInt(),0xFFF18FB0.toInt())
    val themes = listOf("default","pda","holo","ios","galaxy")
    val animations = listOf("none","pulse","sparkle")
}
object AuctionRules {
    const val DURATION = 120_000L
    fun bid(lot: AuctionLot, now: Long): Int {
        if (now < lot.startedAt+15_000L || lot.ceiling < lot.reserve) return 0
        val steps=((minOf(now,lot.endsAt)-lot.startedAt)/15_000L).coerceIn(1,8)
        return (lot.reserve + (lot.ceiling-lot.reserve).toLong()*steps/8).toInt()
    }
    fun bidder(lot: AuctionLot, now: Long): String = listOf("Виртуальный Илья","Виртуальная Аня","Виртуальный Max","Виртуальный коллекционер").get(
        Math.floorMod(lot.id + now/15000L,4L).toInt())
}
data class Season(val id: String,val title: String,val endsAt: Long,val month: Int) {
    val skin: String get() = "season:$id"
    private val pool: List<Int> by lazy { Catalog.phones.indices.filter {
        !Catalog.isUltra(it) && Catalog.phones[it].rarity.ordinal >= Rarity.UNCOMMON.ordinal && it%4==month%4 } }
    fun roll(ticket: Int): Int {
        require(ticket in 0 until Catalog.TOTAL_TICKETS)
        return Catalog.ultraDrop(ticket) ?: pool[ticket%pool.size]
    }
    val cost: Int by lazy {
        val total=(0 until Catalog.TOTAL_TICKETS).sumOf { Catalog.phones[roll(it)].price.toLong() }
        ((total*115+999999999L)/1000000000L*1000).toInt()
    }
}
object Seasons {
    fun current(now: Long): Season {
        require(now>=0)
        val month=YearMonth.from(Instant.ofEpochMilli(now).atZone(ZoneOffset.UTC))
        val title=when(month.monthValue) { 11->"Неоновая осень";12->"Северное сияние";1->"Ретро-сезон";else->"Фестиваль цвета" }
        return Season(month.toString(),title,month.plusMonths(1).atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),month.monthValue)
    }
}
data class BattleStats(val power: Int,val camera: Int,val battery: Int) {
    fun values()=listOf(power,camera,battery)
}
object Battles {
    const val COOLDOWN=60_000L
    val tactics=listOf("Разгон","Стабильность","Энергосбережение")
    fun stats(item: DeviceSnapshot): BattleStats {
        val phone=item.phone
        val quality=listOf(25,55,80,100)[item.traits.condition]
        val mutation=100+item.traits.mutation.ordinal*5
        fun adjust(value: Int)=(value.toLong()*quality*mutation/10000).toInt().coerceIn(1,999)
        val power=(sqrt(phone.price.toDouble())*2+phone.rarity.ordinal*35).toInt()
        return BattleStats(adjust(power),adjust(power*9/10+Math.floorMod(phone.name.hashCode(),60)),
            adjust(if(phone.name=="Nokia 3310") 950 else 450+Math.floorMod(phone.name.hashCode(),300)))
    }
    fun score(item: DeviceSnapshot,tactic: Int,opponentTactic: Int): Int {
        require(tactic in 0..2 && opponentTactic in 0..2)
        val base=stats(item).values()[tactic]
        val boost=if((tactic-opponentTactic+3)%3==1) 125 else 100
        return base*boost/100
    }
    fun opponent(now: Long): DeviceSnapshot = DeviceSnapshot(Math.floorMod(now/COOLDOWN*17L+29L,Catalog.phones.size.toLong()).toInt())
}

fun Progress.buyStock(model: Int, expectedSlot: Long, now: Long): Progress {
    require(model in Catalog.phones.indices)
    if(Catalog.isUltra(model) || StockMarket.slot(now)!=expectedSlot) return this
    val price=StockMarket.buyPrice(model,now)
    return if(coins<price) this else money(-price.toLong()).appendPhone(model)
}
fun Progress.sellStock(key: String,expectedSlot: Long,now: Long): Progress {
    val item=device(key) ?: return this
    if(StockMarket.slot(now)!=expectedSlot) return this
    if(!canCredit(StockMarket.sellPrice(item,now).toLong())) return this
    return removeDevice(key).money(StockMarket.sellPrice(item,now).toLong())
}
fun Progress.sellDevice(key: String): Progress {
    val item=device(key) ?: return this
    if(!canCredit(item.value.toLong())) return this
    return removeDevice(key).money(item.value.toLong())
}
fun Progress.repairOwnedPrice(position: Int): Int {
    if(position !in owned.indices) return 0
    val traits=traitsAt(position)
    if(traits.condition==3) return 0
    val before=DeviceSnapshot(owned[position],traits).value
    val after=DeviceSnapshot(owned[position],traits.copy(condition=traits.condition+1)).value
    return ((after-before).toLong()*70/100).toInt().coerceAtLeast(1)
}
fun Progress.repairDevice(key: String): Progress {
    val item=device(key) ?: return this
    if(item.traits.condition>=3 || item.company!=null) return this
    if(item.used) return repairUsed(used.indexOfFirst { it.uid==item.traits.uid })
    val position=owned.indices.first { traitsAt(it).uid==item.traits.uid }
    val cost=repairOwnedPrice(position)
    if(coins<cost) return this
    val nextTraits=owned.indices.map { if(it==position) traitsAt(it).copy(condition=traitsAt(it).condition+1) else traitsAt(it) }
    return money(-cost.toLong()).replaceOwned(owned,nextTraits)
}
fun Progress.claimAlbum(id: String): Progress {
    val album=Albums.all.firstOrNull { it.id==id } ?: return this
    val collection=(owned+used.map { it.phone }).toSet()
    if(id in world.albums || !collection.containsAll(album.models) || !canCredit(album.reward.toLong())) return this
    return money(album.reward.toLong()).copy(world=world.copy(albums=world.albums+id,skins=world.skins+"album:$id",
        appTokens=(world.appTokens.toLong()+1).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()).bumpEconomy())
}
fun Progress.unlockApp(key: String,app: String,useToken: Boolean): Progress {
    if(device(key)==null) return this
    val price=VirtualApps.prices[app] ?: return this
    val profile=virtualProfile(key)
    if(app in profile.apps || (useToken && world.appTokens==0) || (!useToken && coins<price)) return this
    val paid=if(useToken) copy(world=world.copy(appTokens=world.appTokens-1)) else money(-price.toLong())
    return paid.updateVirtualProfile(key,profile.copy(apps=profile.apps+app))
}
fun Progress.customize(key: String,kind: String,value: String): Progress {
    if(device(key)==null) return this
    val profile=virtualProfile(key)
    val next=when(kind) {
        "color" -> { val i=value.toIntOrNull() ?: return this; if(i !in VirtualApps.colors.indices) return this;profile.copy(color=VirtualApps.colors[i]) }
        "wallpaper" -> { val i=value.toIntOrNull() ?: return this;if(i !in 0..5) return this;profile.copy(wallpaper=i) }
        "theme" -> { if(value !in VirtualApps.themes && value !in world.skins && value !in world.seasonalSkins) return this;profile.copy(theme=value) }
        "animation" -> { if(value !in VirtualApps.animations) return this;profile.copy(animation=value) }
        else -> return this
    }
    if(next==profile || coins<1000) return this
    return money(-1000L).updateVirtualProfile(key,next)
}
fun Progress.flashVirtualPhone(key: String,firmware: String): Progress {
    val item=device(key) ?: return this
    val profile=virtualProfile(key)
    if(firmware !in Workshop.options(item.phone) || profile.firmware==firmware) return this
    return updateVirtualProfile(key,profile.copy(firmware=firmware))
}
fun Progress.listAuction(key: String,reservePercent: Int,now: Long): Progress {
    require(reservePercent in listOf(80,100,120) && now>=0)
    val item=device(key) ?: return this
    if(world.auctions.size>=5 || world.serial==Long.MAX_VALUE) return this
    val reserve=(item.value.toLong()*reservePercent/100).toInt().coerceAtLeast(1)
    val fee=(reserve/50).coerceAtLeast(100)
    if(coins<fee) return this
    val ceiling=(item.value.toLong()*(85+Math.floorMod(world.serial*31L+StockMarket.slot(now),46L))/100).toInt()
    val lot=AuctionLot(world.serial,item,reserve,now,now+AuctionRules.DURATION,ceiling)
    val removed=removeDevice(key).money(-fee.toLong())
    return removed.copy(world=removed.world.copy(auctions=removed.world.auctions+lot,serial=world.serial+1))
}
fun Progress.settleAuction(id: Long,now: Long): Progress {
    val lot=world.auctions.firstOrNull { it.id==id } ?: return this
    if(now<lot.endsAt || id in world.settledLots) return this
    val bid=AuctionRules.bid(lot,now)
    if(bid>0 && !canCredit(bid.toLong())) return this
    val next=copy(world=world.copy(auctions=world.auctions.filter { it.id!=id },settledLots=(world.settledLots+id).sorted().takeLast(200).toSet()).bumpEconomy())
    return if(bid==0) next.returnDevice(lot.device) else next.money(bid.toLong())
}
fun Progress.createCompany(name: String): Progress {
    val clean=name.trim().take(24)
    if(clean.length<2 || world.company.level>0 || coins<30000) return this
    return money(-30000L).copy(world=world.copy(company=Company(name=clean,level=1)).bumpEconomy())
}
fun Progress.upgradeCompany(): Progress {
    val company=world.company
    val cost=company.level*25000
    if(company.level !in 1..4 || coins<cost) return this
    return money(-cost.toLong()).copy(world=world.copy(company=company.copy(level=company.level+1)).bumpEconomy())
}
fun Progress.startProduction(now: Long): Progress {
    require(now>=0)
    val c=world.company
    val cost=5000+c.level*1000
    if(c.level==0 || c.readyAt>0 || c.phones.size>=20 || c.produced==Int.MAX_VALUE || coins<cost) return this
    return money(-cost.toLong()).copy(world=world.copy(company=c.copy(readyAt=now+120000L,productionLevel=c.level)).bumpEconomy())
}
fun Progress.collectProduction(now: Long): Progress {
    val c=world.company
    if(c.readyAt==0L || now<c.readyAt || world.serial==Long.MAX_VALUE) return this
    if(c.produced==Int.MAX_VALUE) return this
    val product=CompanyPhone(world.serial,"${c.name} ${c.series} ${c.produced+1}",c.productionLevel.coerceIn(1,5),VirtualApps.colors[(c.produced)%VirtualApps.colors.size])
    return copy(world=world.copy(serial=world.serial+1,company=c.copy(readyAt=0,productionLevel=0,
        produced=(c.produced+1).coerceAtMost(Int.MAX_VALUE),phones=c.phones+product)).bumpEconomy())
}
fun Progress.renameSeries(name: String): Progress {
    val clean=name.trim().take(20)
    if(world.company.level==0 || clean.length<2 || clean==world.company.series || world.company.readyAt>0) return this
    return copy(world=world.copy(company=world.company.copy(series=clean)))
}
fun Progress.activateMiner(key: String): Progress {
    if(device(key)==null || "wallet" !in virtualProfile(key).apps || world.miner.key==key) return this
    // Switching devices must not multiply a second source of passive income.
    return copy(world=world.copy(miner=world.miner.copy(key=key)))
}
fun Progress.advanceMiner(activeMs: Long): Progress {
    require(activeMs>=0)
    val key=world.miner.key
    if(key.isBlank()) return this
    if(device(key)==null || "wallet" !in virtualProfile(key).apps) return this
    return copy(world=world.copy(miner=world.miner.copy(activeMs=(world.miner.activeMs+activeMs.coerceAtMost(30000L)).coerceAtMost(1800000L))))
}
fun Progress.minerRate(): Int = world.miner.key.let { device(it)?.phone?.price?.let { value -> 25+(value/10000).coerceAtMost(25) } ?: 0 }
fun Progress.claimMining(): Progress {
    if(device(world.miner.key)==null) return this
    val minutes=world.miner.activeMs/60000
    if(minutes==0L) return this
    if(!canCredit(minutes*minerRate())) return this
    return money(minutes*minerRate()).copy(world=world.copy(miner=world.miner.copy(activeMs=world.miner.activeMs%60000)).bumpEconomy())
}
fun Progress.redeemCode(raw: String): Progress {
    val code=raw.trim().uppercase(java.util.Locale.ROOT)
    if(code in world.codes) return this
    if(code=="MAST3R" && !canCredit(5000L)) return this
    val models=(owned+used.map { it.phone }).toSet()
    val skin=when {
        code=="PRISM-RETRO" && 0 in models -> "retro"
        code=="SMILE-404" && 500 in models -> "glitch"
        code=="MAST3R" && servicesCompleted>=3 -> "engineer"
        else -> return this
    }
    return money(if(code=="MAST3R") 5000 else 0).copy(world=world.copy(codes=world.codes+code,skins=world.skins+skin).bumpEconomy())
}
fun Progress.fightNpc(key: String,tactic: Int,now: Long,ticket: Int): Pair<Progress,String> {
    require(tactic in 0..2 && ticket in 0..9999 && now>=0)
    val own=device(key) ?: return this to "Телефон недоступен"
    if(world.lastBattleAt>0 && now-world.lastBattleAt<Battles.COOLDOWN) return this to "Бой доступен раз в минуту"
    val npc=Battles.opponent(now)
    val enemyTactic=(ticket%3)
    val a=Battles.score(own,tactic,enemyTactic)*(90+ticket/1000)
    val b=Battles.score(npc,enemyTactic,tactic)*95
    val won=a>=b
    val changed=money(if(won) 1000 else 0).copy(world=world.copy(lastBattleAt=now.coerceAtLeast(1),
        npcWins=(world.npcWins.toLong()+if(won) 1 else 0).coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
        npcLosses=(world.npcLosses.toLong()+if(won) 0 else 1).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()).bumpEconomy())
    return changed to "${if(won) "Победа! +1000 ₽" else "Победил виртуальный соперник"}\n${own.phone.name} против ${npc.phone.name}\nИгровые очки: $a / $b"
}
fun Progress.openMany(kind: CaseKind,count: Int,tickets: List<Int>,mutations: List<Int>,conditions: List<Int>,day: Long,
                      loots: List<Int> = List(count){0}): Pair<Progress,List<DeviceSnapshot>>? {
    require(count in listOf(3,5,10) && tickets.size==count && mutations.size==count && conditions.size==count && loots.size==count)
    require(tickets.all { it in 0 until Catalog.TOTAL_TICKETS } && mutations.all { it in 0..9999 } && conditions.all { it in 0..9999 } && loots.all { it in 0..9999 })
    if(coins.toLong()<kind.cost.toLong()*count || world.serial>Long.MAX_VALUE-count-1) return null
    var next=this
    val drops=mutableListOf<DeviceSnapshot>()
    for(i in 0 until count) {
        next=next.openCase(kind,tickets[i],day,mutations[i],conditions[i],loots[i])!!.first
        drops+=DeviceSnapshot(next.owned.last(),next.traitsAt(next.owned.lastIndex))
    }
    val identified=next.identifyPhones()
    return identified.copy(world=identified.world.bumpEconomy()) to
        (identified.owned.size-count until identified.owned.size).map { DeviceSnapshot(identified.owned[it],identified.traitsAt(it)) }
}
fun Progress.openSeasonal(expectedId: String,ticket: Int,mutation: Int,condition: Int,now: Long): Pair<Progress,DeviceSnapshot>? {
    require(ticket in 0..9999 && mutation in 0..9999 && condition in 0..9999)
    val season=Seasons.current(now)
    if(season.id!=expectedId || coins<season.cost || world.serial==Long.MAX_VALUE) return null
    val model=season.roll(ticket)
    val trait=PhoneTraits(Mutation.roll(mutation),when { condition<1000->1;condition<3000->2;else->3 })
    val ready=forQuestDay(now/Progression.DAY_MS)
    val next=ready.money(-season.cost.toLong()).copy(casesOpened=(casesOpened.toLong()+1).coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
        questCases=(ready.questCases+1).coerceAtMost(3),world=world.copy(seasonalSkins=world.seasonalSkins+season.skin).bumpEconomy()).appendPhone(model,trait)
    val identified=next.identifyPhones()
    return identified to DeviceSnapshot(model,identified.traitsAt(identified.owned.lastIndex))
}
