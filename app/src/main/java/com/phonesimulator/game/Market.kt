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
            Listing("$day-$slot", phone, condition, (quote.toLong() * random.nextInt(85, 121) / 100).toInt().coerceAtLeast(1))
        }
    }
}
enum class CaseKind(val title: String, val cost: Int, val minimum: Rarity) {
    CLASSIC("Классический", 100, Rarity.COMMON),
    STREET("Уличный", 250, Rarity.UNCOMMON),
    FLAGSHIP("Флагманский", 900, Rarity.RARE),
    PREMIUM("Премиум", 2500, Rarity.EPIC);
    fun roll(ticket: Int): Int {
        require(ticket in 0..999)
        if (this == CLASSIC) return Catalog.roll(ticket)
        // Ultra stays exactly one ticket out of 1000 in every case.
        if (ticket == 999) return 5
        val pool = Catalog.phones.indices.filter { it != 5 && Catalog.phones[it].rarity.ordinal >= minimum.ordinal }
        return pool[ticket % pool.size]
    }
}
