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
class Case(val name: String, val cost: Int, ordinaryTickets: List<Int>) {
    val tickets = ordinaryTickets + listOf(3, 2, 1)

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
    Case("Классика", 19_000, listOf(25_213, 51_671, 16_019, 3_517, 344, 2_028, 1_202)),
    Case("Полигон", 22_500, listOf(23_772, 48_538, 17_936, 5_438, 1_016, 2_076, 1_218)),
    Case("Золотой фонд", 25_000, listOf(22_892, 47_977, 15_379, 7_515, 2_937, 2_076, 1_218))
)

fun chanceText(tickets: Int): String {
    val fraction = (tickets % 1_000).toString().padStart(3, '0').trimEnd('0')
    val whole = tickets / 1_000
    return if (fraction.isEmpty()) "$whole%" else "$whole,$fraction%"
}
