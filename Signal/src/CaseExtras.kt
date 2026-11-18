package signal

val wishes = mutableSetOf<Int>()
private data class Drop(val phone: Int, val case: Int, val isNew: Boolean)
private val history = mutableListOf<Drop>()
private const val historyLimit = 20

fun recordDrop(id: Int, case: Int, isNew: Boolean) {
    history.add(0, Drop(id, case, isNew))
    if (history.size > historyLimit) history.removeAt(history.lastIndex)
}

fun encodeHistory() = history.joinToString(",") { "${it.phone}:${it.case}:${if (it.isNew) 1 else 0}" }

fun restoreExtras(savedWishes: String, savedHistory: String) {
    wishes.addAll(savedWishes.split(',').mapNotNull { it.toIntOrNull() }.filter { it in phones.indices })
    for (entry in savedHistory.split(',').take(historyLimit)) {
        val f = entry.split(':')
        if (f.size != 3) continue
        val id = f[0].toIntOrNull() ?: continue
        val case = f[1].toIntOrNull() ?: continue
        if (id in phones.indices && case in cases.indices && f[2] in listOf("0", "1"))
            history.add(Drop(id, case, f[2] == "1"))
    }
}

fun wishLabel(id: Int) = if (id in wishes) "♥ В списке желаний" else "♡ Хочу в коллекцию"
fun wishButton(id: Int) = """<button class="wish" data-act="wish-$id" aria-pressed="${id in wishes}">${wishLabel(id)}</button>"""

fun historyPanel(): String {
    val rows = history.joinToString("") { drop ->
        val p = phones[drop.phone]
        """<li><button class="history-row r${p.tier}" data-act="phone-${drop.phone}"><i class="swatch"></i><span><b>${p.name}</b><small>${cases[drop.case].name}</small></span><em>${if (drop.isNew) "Новая" else "Повтор"}</em></button></li>"""
    }
    val content = if (history.isEmpty()) "<p>Здесь появятся следующие 20 выпадений.</p>" else "<ol>$rows</ol>"
    return """<details class="drop-history"><summary>История выпадений <span>${history.size}</span></summary>$content</details>"""
}

private data class Reward(val id: Int, val message: String)
private var spinning: Reward? = null
private var spinTimer = 0
private const val spinDuration = 2800
private const val winnerSlot = 15

fun spinCase(case: Case, winner: Int, message: String) {
    if (window.matchMedia("(prefers-reduced-motion: reduce)").matches == true) {
        showPhone(winner, message)
        return
    }
    val tiles = (0..winnerSlot + 3).joinToString("") { slot ->
        val id = if (slot == winnerSlot) winner else case.drawPhone()
        val phone = phones[id]
        """<div class="reel-tile r${phone.tier}"><img src="${art("phone$id")}" alt=""><b>${phone.name}</b></div>"""
    }
    spinning = Reward(winner, message)
    openModal("""<div class="modalbg"><section class="modal spin-modal" role="dialog" aria-modal="true" aria-labelledby="spin-title">
        <div class="spin-heading"><span>Открытие кейса</span><h2 id="spin-title">${case.name}</h2></div>
        <div class="reel-window" aria-hidden="true"><div class="reel-track" style="--stop:${-(winnerSlot * 120 + 56)}px;--duration:${spinDuration}ms">$tiles</div></div>
        <div class="spin-footer">${btn("skip-spin", "Показать результат")}</div>
    </section></div>""")
    spinTimer = window.setTimeout({ revealSpin() }, spinDuration + 450) as Int
}

fun revealSpin(): Boolean {
    val reward = spinning ?: return false
    spinning = null
    window.clearTimeout(spinTimer)
    showPhone(reward.id, reward.message)
    return true
}
