package com.phonesimulator.game

import kotlin.random.Random

data class UsedPhone(val phone: Int, val condition: Int)
data class Listing(val id: String, val phone: Int, val condition: Int, val price: Int)
object Market {
    val conditions = listOf("На запчасти", "С потёртостями", "Хорошее", "Как новый")
    fun price(phone: Int, condition: Int): Int {
        require(phone in Catalog.phones.indices && condition in conditions.indices)
        return (Catalog.phones[phone].price.toLong() * listOf(25, 55, 80, 110)[condition] / 100).toInt().coerceAtLeast(1)
    }
    fun listings(day: Long): List<Listing> {
        val random = Random(day.hashCode())
        return (0 until 12).map { slot ->
            val phone = random.nextInt(Catalog.phones.size)
            val condition = random.nextInt(conditions.size)
            val quote = price(phone, condition)
            // A catalog expansion may change the seeded model. Never reuse another model's negotiated quote.
            Listing("$day-$slot-$phone-$condition", phone, condition, (quote.toLong() * random.nextInt(85, 121) / 100).toInt().coerceAtLeast(1))
        }
    }
}
enum class CaseKind(val title: String, val minimum: Rarity) {
    CLASSIC("Классический", Rarity.COMMON),
    STREET("Уличный", Rarity.UNCOMMON),
    FLAGSHIP("Флагманский", Rarity.RARE),
    PREMIUM("Премиум", Rarity.EPIC);
    private val pool: List<Int> by lazy {
        Catalog.phones.indices.filter { !Catalog.isUltra(it) && Catalog.phones[it].rarity.ordinal >= minimum.ordinal }
    }
    // Price is 115% of the exact expected sale value, rounded up to 1,000 ₽.
    val cost: Int by lazy {
        val total = (0 until Catalog.TOTAL_TICKETS).sumOf { Catalog.phones[roll(it)].price.toLong() }
        val denominator = Catalog.TOTAL_TICKETS * 100L * 1000L
        ((total * 115 + denominator - 1) / denominator * 1000L).toInt()
    }
    fun roll(ticket: Int): Int {
        require(ticket in 0 until Catalog.TOTAL_TICKETS)
        if (this == CLASSIC) return Catalog.roll(ticket)
        Catalog.ultraDrop(ticket)?.let { return it }
        return pool[ticket % pool.size]
    }
}

object Economy {
    const val GIFT_COOLDOWN = 10 * 60 * 1000L
    const val START_BALANCE = 62000
    const val BONUS = 31000
}
