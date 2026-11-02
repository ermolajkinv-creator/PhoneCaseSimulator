package com.phonesimulator.game

enum class ServiceKind(val title: String, val level: Int, val seconds: Int) {
    REPAIR("Ремонт экрана", 1, 30), SETUP("Настройка телефона", 1, 20),
    ROM("Кастомная прошивка Android", 2, 45), JAILBREAK("Джейлбрейк iPhone", 3, 60)
}
data class ClientOffer(val slot: Long, val phone: Int, val kind: ServiceKind, val cost: Int, val reward: Int, val seconds: Int) {
    val customer: String get() = listOf("Алексей", "Мария", "Денис", "Анна", "Тимур", "Елена")[(slot % 6).toInt()]
}
data class ClientJob(val offer: ClientOffer, val readyAt: Long)
object Clients {
    const val INTERVAL = 120_000L
    val upgradeCosts = listOf(10000,30000,60000,120000)
    val reputationRequired = listOf(3,10,25,50)
    fun offer(slot: Long, workshopLevel: Int): ClientOffer {
        require(slot >= 0 && workshopLevel in 1..5)
        val kind = ServiceKind.entries[(slot % ServiceKind.entries.size).toInt()]
        val candidates = Catalog.phones.indices.filter {
            when (kind) {
                ServiceKind.ROM -> "LineageOS (симуляция)" in Workshop.options(Catalog.phones[it])
                ServiceKind.JAILBREAK -> Catalog.phones[it].name.startsWith("iPhone")
                else -> true
            }
        }
        val phone = candidates[((slot / 4) % candidates.size).toInt()]
        val cost = if (kind == ServiceKind.REPAIR) 300 + Catalog.phones[phone].price / 100 else 0
        val profit = 1000 + Catalog.phones[phone].price / 50 + workshopLevel*250 + kind.ordinal*500
        return ClientOffer(slot, phone, kind, cost, cost+profit, (kind.seconds-(workshopLevel-1)*5).coerceAtLeast(10))
    }
}
