package signal

import kotlin.random.Random

const val ticketsPerCase = 100_000
const val oldTier = 0
const val foldableTier = 6
const val firstExclusiveTier = 7
val tiers = listOf("Старые", "Обычный", "Редкий", "Эпический", "Легендарный", "Флагманский", "Раскладушки", "Эксклюзивный", "Мифический", "Ультраредкий")
val refunds = listOf(30, 30, 60, 100, 160, 200, 220, 240, 360, 500)
val phonesByTier = tiers.indices.map { tier -> phones.indices.filter { phones[it].tier == tier } }

// Each ticket has the same probability. Within a tier, all models are equally likely.
class Case(val name: String, val cost: Int, ordinaryTickets: List<Int>) {
    val tickets = ordinaryTickets + listOf(12, 10, 5)

    init {
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
    Case("Классика", 100, listOf(16_611, 48_372, 22_913, 6_074, 805, 3_237, 1_961)),
    Case("Полигон", 180, listOf(13_010, 40_540, 27_704, 10_876, 2_485, 3_357, 2_001)),
    Case("Золотой фонд", 320, listOf(10_809, 39_139, 21_312, 16_068, 7_287, 3_357, 2_001))
)

fun chanceText(tickets: Int): String {
    val fraction = (tickets % 1_000).toString().padStart(3, '0').trimEnd('0')
    val whole = tickets / 1_000
    return if (fraction.isEmpty()) "$whole%" else "$whole,$fraction%"
}
