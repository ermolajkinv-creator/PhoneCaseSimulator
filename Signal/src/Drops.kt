package signal

import kotlin.random.Random

const val ticketsPerCase = 100_000
const val oldTier = 0
const val foldableTier = 6
const val firstExclusiveTier = 7
val tiers = listOf("Старые", "Обычный", "Редкий", "Эпический", "Легендарный", "Флагманский", "Раскладушки", "Эксклюзивный", "Мифический", "Ультраредкий")
const val repairPay = 900
val Phone.tradeIn: Int get() = (price * 45 / 1000) * 10

fun rubles(amount: Long): String =
    amount.toString().reversed().chunked(3).joinToString(" ").reversed() + " ₽"
fun rubles(amount: Int): String = rubles(amount.toLong())
val phonesByTier = tiers.indices.map { tier -> phones.indices.filter { phones[it].tier == tier } }

// Each ticket has the same probability. Within a tier, all models are equally likely.
class Case(val name: String, markupPercent: Int, ordinaryTickets: List<Int>) {
    val tickets = ordinaryTickets + listOf(12, 10, 5)

    init {
        require(tickets.size == tiers.size && tickets.sum() == ticketsPerCase)
        require(phonesByTier.drop(firstExclusiveTier).all { it.size == 1 })
        // Even the least likely ordinary model must outrank the most likely exclusive.
        require(ordinaryTickets.indices.all { i ->
            phonesByTier[i].isNotEmpty() && ordinaryTickets[i] > tickets[firstExclusiveTier] * phonesByTier[i].size
        })
    }

    // One phone per box. Price the contents by the actual draw probabilities.
    val cost: Int = run {
        val expectedValue = tickets.indices.sumOf { i ->
            phonesByTier[i].map { phones[it].price }.average() * tickets[i] / ticketsPerCase
        }
        kotlin.math.ceil(expectedValue * markupPercent / 100 / 500).toInt() * 500
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
    Case("Классика", 102, listOf(16_611, 48_372, 22_913, 6_074, 805, 3_237, 1_961)),
    Case("Полигон", 110, listOf(13_010, 40_540, 27_704, 10_876, 2_485, 3_357, 2_001)),
    Case("Золотой фонд", 118, listOf(10_809, 39_139, 21_312, 16_068, 7_287, 3_357, 2_001))
)

fun chanceText(tickets: Int): String {
    val fraction = (tickets % 1_000).toString().padStart(3, '0').trimEnd('0')
    val whole = tickets / 1_000
    return if (fraction.isEmpty()) "$whole%" else "$whole,$fraction%"
}
