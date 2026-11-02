package com.phonesimulator.game

object Progression {
    const val CLICK_REWARD = 10
    const val DAY_MS = 86_400_000L
    val goalNames = linkedMapOf(
        "first" to "Первый телефон", "ten" to "10 моделей", "quarter" to "25 моделей",
        "ultra" to "Ультра в коллекции", "million" to "Миллионер", "clicks" to "100 нажатий"
    )
    fun goals(p: Progress): Set<String> {
        val models = (p.owned + p.used.map { it.phone }).toSet()
        return buildSet {
            if (models.isNotEmpty()) add("first")
            if (models.size >= 10) add("ten")
            if (models.size >= 25) add("quarter")
            if (5 in models) add("ultra")
            if (p.coins >= 1_000_000) add("million")
            if (p.clicks >= 100) add("clicks")
        }
    }
    fun dailyReward(streak: Int) = Economy.BONUS + (streak.coerceIn(1,7)-1)*5000
}
