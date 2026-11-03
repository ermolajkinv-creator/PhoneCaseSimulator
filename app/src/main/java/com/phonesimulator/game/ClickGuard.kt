package com.phonesimulator.game

/** Local rate limit, using the same persisted wall-clock time across activity/process restarts. */
data class ClickGuard(val recent: List<Long> = emptyList(), val blockedUntil: Long = 0L) {
    fun remaining(now: Long): Long = (blockedUntil - now).coerceAtLeast(0L)

    fun tap(now: Long): Pair<ClickGuard, Boolean> {
        require(now >= 0L)
        if (remaining(now) > 0L) return this to false
        val window = recent.filter { it <= now && now - it < WINDOW_MS }
        if (window.size >= MAX_TAPS || recent.any { it > now }) {
            val deadline = now.coerceAtMost(Long.MAX_VALUE - BLOCK_MS) + BLOCK_MS
            return ClickGuard(blockedUntil = deadline) to false
        }
        return ClickGuard(window + now) to true
    }

    companion object {
        const val MAX_TAPS = 12
        const val WINDOW_MS = 1000L
        const val BLOCK_MS = 30_000L
    }
}
