package com.phonesimulator.game

enum class Mutation(val title: String, val percent: Int, val accent: Int) {
    NORMAL("Стандарт",100,0xFF9CAEC9.toInt()), GOLD("Gold",120,0xFFFFCF54.toInt()),
    DIAMOND("Diamond",140,0xFF8BEAFF.toInt()), GALAXY("Galaxy",160,0xFFB094FF.toInt()),
    GLITCH("Glitch",180,0xFF5AFFC8.toInt()), RAINBOW("Rainbow",220,0xFFFF88D6.toInt());
    companion object {
        fun roll(ticket: Int): Mutation {
            require(ticket in 0..9999)
            return when { ticket < 8200 -> NORMAL; ticket < 9000 -> GOLD; ticket < 9400 -> DIAMOND
                ticket < 9700 -> GALAXY; ticket < 9900 -> GLITCH; else -> RAINBOW }
        }
    }
}
data class PhoneTraits(val mutation: Mutation = Mutation.NORMAL, val condition: Int = 3, val uid: Long = 0L)
data class VirtualProfile(val apps: Set<String> = emptySet(), val color: Int = 0, val wallpaper: Int = 0,
                          val theme: String = "default", val animation: String = "none", val bestSnake: Int = 0,
                          val firmware: String = "",val ringtone: Int = 0)
data class CompanyPhone(val id: Long, val name: String, val level: Int, val color: Int)
data class Company(val name: String = "", val level: Int = 0, val readyAt: Long = 0L,
                   val produced: Int = 0, val phones: List<CompanyPhone> = emptyList(),
                   val productionLevel: Int = 0, val series: String = "One")
data class Miner(val key: String = "", val activeMs: Long = 0L)
data class DeviceSnapshot(val model: Int, val traits: PhoneTraits = PhoneTraits(), val used: Boolean = false,
                          val company: CompanyPhone? = null) {
    val key: String get() = company?.let { "company:${it.id}" } ?: "phone:${traits.uid}"
    val phone: Phone get() = company?.let { Phone(it.name,8000+it.level*2000,Rarity.UNCOMMON,0,
        "Виртуальный чип уровня ${it.level}","Игровой OLED","Игровой Android","Смартфон вашей компании; игровые характеристики.") } ?: Catalog.phones[model]
    val value: Int get() {
        val quality = if (used) listOf(25,55,80,110)[traits.condition] else listOf(25,55,80,100)[traits.condition]
        return (phone.price.toLong()*quality*traits.mutation.percent/10_000).toInt().coerceAtLeast(1)
    }
}
data class AuctionLot(val id: Long, val device: DeviceSnapshot, val reserve: Int, val startedAt: Long,
                      val endsAt: Long, val ceiling: Int)
data class WorldData(val traits: List<PhoneTraits> = emptyList(), val serial: Long = 1L,
    val profiles: Map<String,VirtualProfile> = emptyMap(), val appTokens: Int = 0,
    val albums: Set<String> = emptySet(), val skins: Set<String> = emptySet(),
    val auctions: List<AuctionLot> = emptyList(), val settledLots: Set<Long> = emptySet(),
    val company: Company = Company(), val miner: Miner = Miner(),
    val lastBattleAt: Long = 0L, val npcWins: Int = 0, val npcLosses: Int = 0,
    val codes: Set<String> = emptySet(), val seasonalSkins: Set<String> = emptySet(), val economyRevision: Long = 0L, val empire: Empire = Empire())
fun WorldData.bumpEconomy()=copy(economyRevision=if(economyRevision==Long.MAX_VALUE) Long.MAX_VALUE else economyRevision+1)

fun Progress.traitsAt(position: Int): PhoneTraits = world.traits.getOrNull(position) ?: PhoneTraits()
private fun compactTraits(traits: List<PhoneTraits>) = if (traits.all { it == PhoneTraits() }) emptyList() else traits
fun Progress.replaceOwned(models: List<Int>, traits: List<PhoneTraits>): Progress {
    require(models.size == traits.size)
    return copy(owned = models, world = world.copy(traits = compactTraits(traits)))
}
fun Progress.appendPhone(model: Int, traits: PhoneTraits = PhoneTraits()): Progress {
    require(model in Catalog.phones.indices && traits.condition in 0..3 && traits.uid >= 0)
    val needsId = traits != PhoneTraits() && traits.uid == 0L
    if (needsId && world.serial == Long.MAX_VALUE) return this
    val assigned = if (needsId) traits.copy(uid = world.serial) else traits
    val next = replaceOwned(owned + model, owned.indices.map(::traitsAt) + assigned)
    return if (needsId) next.copy(world = next.world.copy(serial = world.serial+1).bumpEconomy())
        else if(traits!=PhoneTraits()) next.copy(world=next.world.bumpEconomy()) else next
}
fun Progress.removePhoneAt(position: Int): Progress {
    if (position !in owned.indices) return this
    val next=replaceOwned(owned.filterIndexed { i,_ -> i != position }, owned.indices.filter { it != position }.map(::traitsAt))
    return if(traitsAt(position).uid>0L) next.copy(world=next.world.bumpEconomy()) else next
}
fun Progress.identifyPhones(): Progress {
    var serial = world.serial
    if (serial >= Long.MAX_VALUE - owned.size - used.size) return this
    val traits = owned.indices.map { traitsAt(it).let { t -> if(t.uid == 0L) t.copy(uid = serial++) else t } }
    val secondhand = used.map { if(it.uid == 0L) it.copy(uid = serial++) else it }
    return copy(used = secondhand, world = world.copy(traits = compactTraits(traits), serial = serial))
}
fun Progress.devices(): List<DeviceSnapshot> = owned.mapIndexed { i,model -> DeviceSnapshot(model, traitsAt(i)) } +
    used.map { DeviceSnapshot(it.phone,PhoneTraits(it.mutation,it.condition,it.uid),true) } +
    world.company.phones.map { DeviceSnapshot(-1,PhoneTraits(uid=it.id),company=it) }
fun Progress.device(key: String): DeviceSnapshot? = devices().firstOrNull { it.key == key && it.traits.uid > 0L }
fun Progress.removeDevice(key: String): Progress {
    val item = device(key) ?: return this
    item.company?.let { companyPhone -> return copy(world=world.copy(company=world.company.copy(
        phones=world.company.phones.filter { it.id != companyPhone.id })).bumpEconomy()) }
    return if(item.used) copy(used=used.filter { it.uid != item.traits.uid },world=world.bumpEconomy())
        else removePhoneAt(owned.indices.first { traitsAt(it).uid == item.traits.uid })
}
fun Progress.returnDevice(item: DeviceSnapshot): Progress = when {
    item.company != null -> copy(world=world.copy(company=world.company.copy(phones=world.company.phones+item.company)))
    item.used -> copy(used=used+UsedPhone(item.model,item.traits.condition,item.traits.mutation,item.traits.uid))
    else -> appendPhone(item.model,item.traits)
}
fun Progress.money(delta: Long): Progress = copy(coins=when {
    delta>Int.MAX_VALUE.toLong()->Int.MAX_VALUE;delta< -Int.MAX_VALUE.toLong()->0
    else->(coins.toLong()+delta).coerceIn(0L,Int.MAX_VALUE.toLong()).toInt()
},
    world=if(delta!=0L)world.bumpEconomy() else world)
fun Progress.virtualProfile(key: String) = world.profiles[key] ?: VirtualProfile()
fun Progress.canCredit(amount: Long)=amount>=0L && amount<=Int.MAX_VALUE.toLong()-coins
fun Progress.updateVirtualProfile(key: String, value: VirtualProfile): Progress =
    copy(world=world.copy(profiles=world.profiles+(key to value)).bumpEconomy())
