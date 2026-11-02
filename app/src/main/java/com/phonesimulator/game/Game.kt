package com.phonesimulator.game

/** Stable list positions preserve collections saved by version 1.0. */
enum class Rarity(val title: String, val accent: Int) {
    COMMON("Обычный", 0xFF9CAEC9.toInt()),
    UNCOMMON("Необычный", 0xFF59F3DC.toInt()),
    RARE("Редкий", 0xFF63ADFF.toInt()),
    EPIC("Эпический", 0xFFBA8CFF.toInt()),
    LEGENDARY("Легендарный", 0xFFFFCA71.toInt()),
    ULTRA("Ультралегендарный", 0xFFFF78C6.toInt())
}

data class Phone(val name: String, val price: Int, val rarity: Rarity, val tickets: Int)
object Catalog {
    const val CASE_PRICE = 100
    val phones = listOf(
        Phone("Nokia 3310", 50, Rarity.COMMON, 500),
        Phone("Redmi Note", 150, Rarity.UNCOMMON, 280),
        Phone("Google Pixel", 500, Rarity.RARE, 150),
        Phone("Samsung Galaxy S", 1200, Rarity.EPIC, 58),
        Phone("iPhone Pro", 2500, Rarity.LEGENDARY, 11),
        Phone("Samsung Z Fold 8 Ultra", 25000, Rarity.ULTRA, 1)
    )
    // One of exactly 1000 equally likely tickets is Ultra: 0.1%, in every case.
    fun roll(ticket: Int): Int {
        require(ticket in 0..999)
        var edge = 0
        phones.forEachIndexed { index, phone ->
            edge += phone.tickets
            if (ticket < edge) return index
        }
        error("Invalid drop table")
    }
}

data class Progress(val coins: Int = 1000, val owned: List<Int> = emptyList()) {
    fun open(ticket: Int): Pair<Progress, Int>? {
        if (coins < Catalog.CASE_PRICE) return null
        val phone = Catalog.roll(ticket)
        return copy(coins = coins - Catalog.CASE_PRICE, owned = owned + phone) to phone
    }
    fun sell(index: Int): Progress {
        if (index !in Catalog.phones.indices || index !in owned) return this
        val next = owned.toMutableList().apply { remove(index) }
        return copy(coins = credit(Catalog.phones[index].price), owned = next)
    }
    fun bonus() = copy(coins = credit(500))
    private fun credit(amount: Int) = (coins.toLong() + amount).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    companion object {
        fun restore(coins: Int, saved: String) = Progress(
            coins.coerceAtLeast(0),
            saved.split(',').mapNotNull { it.toIntOrNull() }.filter { it in Catalog.phones.indices }
        )
    }
}
