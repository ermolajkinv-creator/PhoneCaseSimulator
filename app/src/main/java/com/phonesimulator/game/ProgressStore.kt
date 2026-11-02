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
        return Progress.restore(coins, owned).copy(used = used,
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
        .putLong("lastGiftAt", progress.lastGiftAt)
        .putString("firmware", progress.firmware.entries.joinToString(";") { "${it.key}:${it.value}" })
        .putString("used", progress.used.joinToString(";") { "${it.phone}:${it.condition}" })
        .putStringSet("purchased", progress.purchased)
        .putString("owned", progress.owned.joinToString(","))
        .commit()
}
