package com.phonesimulator.game

import android.content.Context

class ProgressStore(context: Context) {
    private val prefs = context.getSharedPreferences("game", Context.MODE_PRIVATE)
    fun load(): Progress {
        val coins = try { prefs.getInt("coins", 1000) } catch (_: ClassCastException) { 1000 }
        val owned = try { prefs.getString("owned", "").orEmpty() } catch (_: ClassCastException) { "" }
        val used = prefs.getString("used", "").orEmpty().split(';').mapNotNull { entry ->
            val parts = entry.split(':')
            val phone = parts.getOrNull(0)?.toIntOrNull()
            val condition = parts.getOrNull(1)?.toIntOrNull()
            if (phone != null && phone in Catalog.phones.indices && condition != null && condition in 0..3)
                UsedPhone(phone, condition) else null
        }
        return Progress.restore(coins, owned).copy(used = used,
            purchased = prefs.getStringSet("purchased", emptySet()).orEmpty().toSet())
    }
    // Persist payment and reward together before starting the cosmetic animation.
    fun save(progress: Progress): Boolean = prefs.edit()
        .putInt("coins", progress.coins)
        .putString("used", progress.used.joinToString(";") { "${it.phone}:${it.condition}" })
        .putStringSet("purchased", progress.purchased)
        .putString("owned", progress.owned.joinToString(","))
        .commit()
}
