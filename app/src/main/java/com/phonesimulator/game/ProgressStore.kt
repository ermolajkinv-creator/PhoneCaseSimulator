package com.phonesimulator.game

import android.content.Context

class ProgressStore(context: Context) {
    private val prefs = context.getSharedPreferences("game", Context.MODE_PRIVATE)
    fun load(): Progress {
        val coins = try { prefs.getInt("coins", 1000) } catch (_: ClassCastException) { 1000 }
        val owned = try { prefs.getString("owned", "").orEmpty() } catch (_: ClassCastException) { "" }
        return Progress.restore(coins, owned)
    }
    // Persist payment and reward together before starting the cosmetic animation.
    fun save(progress: Progress): Boolean = prefs.edit()
        .putInt("coins", progress.coins)
        .putString("owned", progress.owned.joinToString(","))
        .commit()
}
