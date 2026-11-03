package com.phonesimulator.game

data class CashEvent(val at: Long, val amount: Long, val title: String)
data class Expansion(
    val vault: Int = 0, val income: Long = 0, val expenses: Long = 0,
    val ledger: List<CashEvent> = emptyList(), val negotiated: Set<String> = emptySet(),
    val discounts: Map<String, Int> = emptyMap(), val awardsClaimed: Set<String> = emptySet(),
    val deliveryDay: Long = -1L, val idleAt: Long = 0L
) {
    fun record(old: Progress, next: Progress, at: Long, title: String): Expansion {
        val delta = next.coins.toLong()-old.coins + vault.toLong()-old.expansion.vault
        if (delta == 0L) return this
        fun add(a: Long, b: Long) = if (Long.MAX_VALUE-a < b) Long.MAX_VALUE else a+b
        return copy(income = if(delta>0) add(income,delta) else income,
            expenses = if(delta<0) add(expenses,-delta) else expenses,
            ledger = (ledger + CashEvent(at,delta,title.take(60))).takeLast(50))
    }
}
object ExpansionRules {
    val awardPrices = mapOf("first" to 1000, "ten" to 5000, "quarter" to 10000,
        "ultra" to 20000, "million" to 10000, "clicks" to 3000)
    fun newPrice(model: Int) = ((Catalog.phones[model].price.toLong()*110+99)/100).toInt()
    fun deliveryModel(day: Long): Int {
        require(day >= 0)
        val models = Catalog.phones.indices.filter { !Catalog.isUltra(it) }
        return models[(day % models.size).toInt()]
    }
    fun deliveryPrice(day: Long) = Catalog.phones[deliveryModel(day)].price*125/100+2500
}
private fun Progress.addCash(amount: Int) = copy(coins=(coins.toLong()+amount).coerceAtMost(Int.MAX_VALUE.toLong()).toInt())
fun Progress.buyNew(model: Int): Progress {
    require(model in Catalog.phones.indices)
    if (Catalog.isUltra(model) || coins < ExpansionRules.newPrice(model)) return this
    return copy(coins=coins-ExpansionRules.newPrice(model), owned=owned+model)
}
fun Progress.bargain(listing: Listing, ticket: Int): Progress {
    require(ticket in 0..99)
    if (listing.id in purchased || listing.id in expansion.negotiated) return this
    val attempts = (expansion.negotiated.toList()+listing.id).takeLast(200).toSet()
    val discounts = expansion.discounts.filterKeys { it in attempts }.toMutableMap()
    if (ticket < 60) discounts[listing.id] = (listing.price.toLong()*85/100).toInt().coerceAtLeast(1)
    return copy(expansion=expansion.copy(negotiated=attempts, discounts=discounts))
}
fun Progress.askPrice(listing: Listing) = expansion.discounts[listing.id] ?: listing.price
fun Progress.repairPrice(position: Int): Int {
    require(position in used.indices)
    val item=used[position]
    if (item.condition>=3) return 0
    val difference=Market.price(item.phone,item.condition+1)-Market.price(item.phone,item.condition)
    return ((difference.toLong()*70+99)/100).toInt().coerceAtLeast(1)
}
fun Progress.repairUsed(position: Int): Progress {
    require(position in used.indices)
    val price=repairPrice(position)
    if(price==0 || coins<price) return this
    val items=used.toMutableList();items[position]=items[position].copy(condition=items[position].condition+1)
    return copy(coins=coins-price,used=items)
}
fun Progress.claimAward(id: String): Progress {
    val reward=ExpansionRules.awardPrices[id] ?: return this
    if(id in expansion.awardsClaimed || id !in (badges+Progression.goals(this))) return this
    return addCash(reward).copy(expansion=expansion.copy(awardsClaimed=expansion.awardsClaimed+id))
}
fun Progress.contractInputs(rarity: Rarity) = owned.withIndex().filter { Catalog.phones[it.value].rarity==rarity }.take(3)
fun Progress.contractFee(rarity: Rarity): Int {
    if(rarity.ordinal>=Rarity.LEGENDARY.ordinal || contractInputs(rarity).size<3) return 0
    val targets=Catalog.phones.filter { it.rarity.ordinal==rarity.ordinal+1 }
    val expected=targets.sumOf { it.price.toLong() }.toDouble()/targets.size
    val source=contractInputs(rarity).sumOf { Catalog.phones[it.value].price.toLong() }
    return (kotlin.math.ceil((expected*1.10-source).coerceAtLeast(0.0)/1000)*1000).toInt()
}
fun Progress.contract(rarity: Rarity, ticket: Int): Progress {
    require(ticket in 0..999)
    if(rarity.ordinal>=Rarity.LEGENDARY.ordinal) return this
    val inputs=contractInputs(rarity)
    if(inputs.size<3 || coins<contractFee(rarity)) return this
    val targets=Catalog.phones.indices.filter { Catalog.phones[it].rarity.ordinal==rarity.ordinal+1 }
    val inventory=owned.toMutableList()
    inputs.map { it.index }.sortedDescending().forEach { inventory.removeAt(it) }
    inventory.add(targets[ticket%targets.size])
    return copy(coins=coins-contractFee(rarity),owned=inventory)
}
fun Progress.deliver(day: Long): Progress {
    val model=ExpansionRules.deliveryModel(day)
    if(day<=expansion.deliveryDay || model !in owned) return this
    val inventory=owned.toMutableList().apply { remove(model) }
    return addCash(ExpansionRules.deliveryPrice(day)).copy(owned=inventory,expansion=expansion.copy(deliveryDay=day))
}
fun Progress.claimIdle(now: Long): Progress {
    require(now>0)
    if(expansion.idleAt==0L) return copy(expansion=expansion.copy(idleAt=now))
    val minutes=((now-expansion.idleAt)/60000L).coerceIn(0L,120L)
    if(minutes==0L) return this
    return addCash((minutes*workshopLevel*100).toInt()).copy(expansion=expansion.copy(idleAt=now))
}
fun Progress.moveToVault(amount: Int): Progress {
    require(amount>0)
    if(amount>coins || amount.toLong()+expansion.vault>Int.MAX_VALUE) return this
    return copy(coins=coins-amount,expansion=expansion.copy(vault=expansion.vault+amount))
}
fun Progress.withdrawVault(amount: Int): Progress {
    require(amount>0)
    if(amount>expansion.vault || amount.toLong()+coins>Int.MAX_VALUE) return this
    return copy(coins=coins+amount,expansion=expansion.copy(vault=expansion.vault-amount))
}
