package com.phonesimulator.game

import android.content.Context

class ProgressStore(context: Context) {
    private val prefs = context.getSharedPreferences("game", Context.MODE_PRIVATE)
    fun load(): Progress {
        val coins = try { prefs.getInt("coins", Economy.START_BALANCE) } catch (_: ClassCastException) { Economy.START_BALANCE }
        val owned = try { prefs.getString("owned", "").orEmpty() } catch (_: ClassCastException) { "" }
        val used = prefs.getString("used", "").orEmpty().split(';').mapNotNull { entry ->
            val parts = entry.split(':')
            val phone = parts.getOrNull(0)?.toIntOrNull()
            val condition = parts.getOrNull(1)?.toIntOrNull()
            if (phone != null && phone in Catalog.phones.indices && condition != null && condition in 0..3)
                UsedPhone(phone, condition) else null
        }
        val jobParts = prefs.getString("clientJob", "").orEmpty().split(',')
        val job = try {
            val slot = jobParts[0].toLong()
            val phone = jobParts[1].toInt()
            val kind = ServiceKind.valueOf(jobParts[2])
            val cost = jobParts[3].toInt()
            val reward = jobParts[4].toInt()
            val seconds = jobParts[5].toInt()
            val at = jobParts[6].toLong()
            if (slot >= 0 && phone in Catalog.phones.indices && cost >= 0 && reward >= cost && seconds in 10..60 && at >= 0)
                ClientJob(ClientOffer(slot, phone, kind, cost, reward, seconds), at) else null
        } catch (_: RuntimeException) { null }
        return Progress.restore(coins, owned).copy(used = used,
            workshopLevel = prefs.getInt("workshopLevel", 1).coerceIn(1,5),
            reputation = prefs.getInt("reputation", 0).coerceAtLeast(0),
            servicesCompleted = prefs.getInt("servicesCompleted", 0).coerceAtLeast(0),
            serviceSlot = prefs.getLong("serviceSlot", -1L),
            servicedSlots = prefs.getStringSet("servicedSlots", emptySet()).orEmpty().mapNotNull { it.toLongOrNull() }.toSet(),
            clientJob = job,
            favorites = prefs.getStringSet("favorites", emptySet()).orEmpty().mapNotNull { it.toIntOrNull() }.filter { it in Catalog.phones.indices }.toSet(),
            nickname = prefs.getString("nickname", "Коллекционер").orEmpty().take(24).ifBlank { "Коллекционер" },
            clicks = prefs.getInt("clicks", 0).coerceAtLeast(0),
            casesOpened = prefs.getInt("casesOpened", 0).coerceAtLeast(0),
            questDay = prefs.getLong("questDay", -1L),
            questClicks = prefs.getInt("questClicks", 0).coerceIn(0,100),
            questCases = prefs.getInt("questCases", 0).coerceIn(0,3),
            claimedQuests = prefs.getStringSet("claimedQuests", emptySet()).orEmpty().toSet(),
            lastDailyDay = prefs.getLong("lastDailyDay", -1L), dailyStreak = prefs.getInt("dailyStreak", 0).coerceIn(0,7),
            badges = prefs.getStringSet("badges", emptySet()).orEmpty().toSet(),
            lastGiftAt = prefs.getLong("lastGiftAt", 0L).coerceAtLeast(0L),
            purchased = prefs.getStringSet("purchased", emptySet()).orEmpty().toSet(),
            firmware = prefs.getString("firmware", "").orEmpty().split(';').mapNotNull {
                val parts = it.split(':', limit = 2)
                val model = parts.firstOrNull()?.toIntOrNull()
                val rom = parts.getOrNull(1)
                if (model != null && model in Catalog.phones.indices && rom in Workshop.options(Catalog.phones[model])) model to rom!! else null
            }.toMap())
    }
    // Persist payment and reward together before starting the cosmetic animation.
    fun save(progress: Progress): Boolean = prefs.edit()
        .putInt("coins", progress.coins)
        .putInt("workshopLevel", progress.workshopLevel)
        .putInt("reputation", progress.reputation)
        .putInt("servicesCompleted", progress.servicesCompleted)
        .putLong("serviceSlot", progress.serviceSlot)
        .putStringSet("servicedSlots", progress.servicedSlots.map { it.toString() }.toSet())
        .putString("clientJob", progress.clientJob?.let { "${it.offer.slot},${it.offer.phone},${it.offer.kind.name},${it.offer.cost},${it.offer.reward},${it.offer.seconds},${it.readyAt}" }.orEmpty())
        .putLong("questDay", progress.questDay)
        .putInt("questClicks", progress.questClicks)
        .putInt("questCases", progress.questCases)
        .putStringSet("claimedQuests", progress.claimedQuests)
        .putStringSet("favorites", progress.favorites.map { it.toString() }.toSet())
        .putString("nickname", progress.nickname.take(24))
        .putInt("clicks", progress.clicks)
        .putInt("casesOpened", progress.casesOpened)
        .putLong("lastDailyDay", progress.lastDailyDay)
        .putInt("dailyStreak", progress.dailyStreak)
        .putStringSet("badges", progress.badges)
        .putLong("lastGiftAt", progress.lastGiftAt)
        .putString("firmware", progress.firmware.entries.joinToString(";") { "${it.key}:${it.value}" })
        .putString("used", progress.used.joinToString(";") { "${it.phone}:${it.condition}" })
        .putStringSet("purchased", progress.purchased)
        .putString("owned", progress.owned.joinToString(","))
        .commit()
}
