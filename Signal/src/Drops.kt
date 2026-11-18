package signal

import kotlin.random.Random

// The one-million-ticket table preserves the published odds while enough tickets
// remain to make every ordinary model more likely than the three exclusive cards.
val ticketsPerCase: Int get() = if (activePhoneIds.size <= 1_000_000 / 15) 1_000_000 else 10_000_000
const val oldTier = 0
// The original card files and archive use stable catalogue codes.
const val catalogFoldableTier = 6
const val subflagshipTier = 5
const val flagshipTier = 6
const val foldableTier = 7
const val firstExclusiveTier = 8
val tiers = listOf("Старые", "Обычный", "Редкий", "Эпический", "Легендарный", "Субфлагманы", "Флагманский", "Раскладушки", "Эксклюзивный", "Мифический", "Ультраредкий")
const val repairPay = 900
val Phone.tradeIn: Int get() = (price * 45 / 1000) * 10

fun rubles(amount: Long): String =
    amount.toString().reversed().chunked(3).joinToString(" ").reversed() + " ₽"
fun rubles(amount: Int): String = rubles(amount.toLong())
val phonesByTier = tiers.indices.map { tier -> activePhoneIds.filter { phones[it].tier == tier } }

// Each ticket has the same probability. Within a tier, all models are equally likely.
class Case(val name: String, val cost: Int, weights: List<Int>) {
    val tickets: List<Int>

    init {
        require(weights.size == firstExclusiveTier && weights.all { it > 0 })
        val exclusives = if (activePhoneIds.size <= 33_000) listOf(30, 20, 10) else listOf(15, 12, 10)
        val minimum = weights.indices.map { exclusives.first() * phonesByTier[it].size + 1 }
        val remainder = ticketsPerCase - exclusives.sum() - minimum.sum()
        require(remainder >= 0)
        val weightSum = weights.sum()
        val ordinaryTickets = weights.mapIndexed { i, weight ->
            minimum[i] + (remainder.toLong() * weight / weightSum).toInt()
        }.toMutableList()
        ordinaryTickets[1] += ticketsPerCase - exclusives.sum() - ordinaryTickets.sum()
        tickets = ordinaryTickets + exclusives
        require(tickets.size == tiers.size && tickets.sum() == ticketsPerCase)
        require(phonesByTier.drop(firstExclusiveTier).all { it.size == 1 })
        // Even the least likely ordinary model must outrank the most likely exclusive.
        require(ordinaryTickets.indices.all { i ->
            phonesByTier[i].isNotEmpty() && ordinaryTickets[i] > tickets[firstExclusiveTier] * phonesByTier[i].size
        })
    }

    fun tierFor(ticket: Int): Int {
        require(ticket in 0 until ticketsPerCase)
        var remaining = ticket
        return tickets.indexOfFirst { weight ->
            remaining -= weight
            remaining < 0
        }
    }

    fun drawPhone(): Int = phonesByTier[tierFor(Random.nextInt(ticketsPerCase))].random()
}

val cases = listOf(
    Case("Классика", 19_000, listOf(18, 48, 20, 6, 2, 4, 1, 1)),
    Case("Полигон", 22_500, listOf(12, 31, 26, 12, 4, 9, 4, 2)),
    Case("Золотой фонд", 25_000, listOf(8, 23, 16, 16, 10, 12, 10, 5))
)

fun chanceText(tickets: Int): String {
    val perPercent = ticketsPerCase / 100
    val fraction = (tickets % perPercent).toString().padStart(perPercent.toString().length - 1, '0').trimEnd('0')
    val whole = tickets / perPercent
    return if (fraction.isEmpty()) "$whole%" else "$whole,$fraction%"
}
