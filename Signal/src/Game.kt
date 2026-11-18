package signal

import kotlin.random.Random

external val document: dynamic
external val window: dynamic

val labels = listOf("Кейсы", "Альбом", "Мастерская", "Настройки")
val icons = listOf(
    "<path d='m3 8 9-5 9 5v10l-9 4-9-4zM3 8l9 5 9-5M12 13v9'/>",
    "<path d='M3 4h7l2 2 2-2h7v15h-7l-2 2-2-2H3zM12 6v15'/>",
    "<path d='M14 4a6 6 0 0 0-6 8L3 17a3 3 0 0 0 4 4l6-6a6 6 0 0 0 7-8l-4 4-3-3 4-4z'/>",
    "<path d='M3 7h5m4 0h9M3 17h10m4 0h4'/><circle cx='10' cy='7' r='2'/><circle cx='15' cy='17' r='2'/>"
)
val ordered = activePhoneIds.sortedWith(compareByDescending<Int> { phones[it].tier }.thenBy { phones[it].name })
private var caseTier = -1
private var albumTier = -1
private const val pageSize = 12
private var casePage = 0
private var albumPage = 0
private var searchText = ""
const val saveKey = "signal.collection.v1"
private const val rublesPerOldCoin = 190
var coins = 47_500L
var opened = 0
val owned = mutableSetOf<Int>()
private const val mainMenu = -1
var tab = mainMenu
var currentCase = 0
var filter = 0
var job = ""
var entry = ""
var jobMessage = ""
var modalOpen = false
var previousFocus: dynamic = null
var storageWarning = false

fun restore() {
    try {
        val raw = window.localStorage.getItem(saveKey) as? String ?: return
        val fields = raw.split("|")
        val legacy = fields.size == 3
        if (!legacy && (fields.size !in listOf(4, 6) || fields[3] != "RUB")) return
        val money = fields[0].toLongOrNull() ?: return
        if (legacy && money > Int.MAX_VALUE) return
        val total = fields[1].toIntOrNull() ?: return
        if (money < 0 || total < 0) return
        coins = if (legacy) money * rublesPerOldCoin else money
        opened = total
        owned.addAll(fields[2].split(",").mapNotNull { it.toIntOrNull() }.filter { it in phones.indices }.map(::canonicalId))
        if (fields.size == 6) restoreExtras(fields[4], fields[5])
        val migrated = fields[2] != owned.joinToString(",") || (fields.size == 6 &&
            (fields[4] != wishes.joinToString(",") || fields[5] != encodeHistory()))
        if (legacy || migrated) save()
    } catch (_: Throwable) { storageWarning = true }
}
fun save() {
    try { window.localStorage.setItem(saveKey, listOf(coins, opened, owned.joinToString(","), "RUB", wishes.joinToString(","), encodeHistory()).joinToString("|")) }
    catch (_: Throwable) { storageWarning = true }
}
fun art(name: String): String = window.signalArt[name] as String
fun icon(i: Int) = """<span class="ico" aria-hidden="true"><svg viewBox="0 0 24 24">${icons[i]}</svg></span>"""
fun rarity(phone: Phone): String {
    val extra = if (phone.foldable && phone.tier != foldableTier) " · Раскладушки" else ""
    return """<span class="rar r${phone.tier}"><i class="swatch"></i>${tiers[phone.tier]}$extra</span>"""
}
private fun Phone.matchesTier(selected: Int): Boolean =
    selected < 0 || tier == selected || (selected == foldableTier && foldable)
fun number(i: Int) = (i + 1).toString().padStart(2, '0')
fun btn(action: String, label: String, css: String = "action") =
    """<button class="$css" data-act="$action">$label</button>"""

private fun menuScreen() = """
    <main class="menu-screen">
      <div class="menu-content">
        <p class="menu-caption">Главное меню</p>
        <h1><span class="mark" aria-hidden="true"><i></i><i></i><i></i></span>Сигнал</h1>
        <button class="menu-case" data-act="tab-0" aria-label="Кейсы">
          <span class="menu-art"><img src="${art("case0")}" alt=""></span>
          <span class="menu-label">Кейсы <span aria-hidden="true">→</span></span>
        </button>
      </div>
    </main>"""

fun header(): String {
    val nav = labels.mapIndexed { i, label ->
        """<button class="${if (i == tab) "sel" else ""}" aria-current="${if (i == tab) "page" else "false"}" data-act="tab-$i">${icon(i)}$label</button>"""
    }.joinToString("")
    return """<header><div class="wrap bar"><button class="brand menu-back" data-act="menu" aria-label="В главное меню" title="Главное меню"><span class="mark" aria-hidden="true"><i></i><i></i><i></i></span>Сигнал <small>В меню</small></button><nav aria-label="Разделы">$nav</nav><div class="wallet" aria-label="Баланс: ${rubles(coins)}">${rubles(coins)}</div></div></header>"""
}
fun title(name: String, desc: String) =
    """<div class="title"><div><h1>$name</h1><p class="sub">$desc</p></div></div>"""

fun cards(ids: List<Int>): String = ids.joinToString("", prefix = "<div class='grid'>", postfix = "</div>") { id ->
    val p = phones[id]
    val check = if (id in owned) """<span class="ownership" aria-label="В коллекции">✓</span>""" else ""
    """<button class="albumcard r${p.tier}" data-act="phone-$id"><div class="thumb"><span class="num">№ ${number(id)}</span>$check<img class="phone" loading="lazy" decoding="async" src="${art("phone$id")}" alt=""></div><div class="cardbody"><b>${p.name}</b>${rarity(p)}<small class="model-price">${rubles(p.price)}</small></div></button>"""
}
private val tierTitles = listOf("Старые", "Обычные", "Редкие", "Эпические", "Легендарные", "Субфлагманы", "Флагманские", "Раскладушки", "Эксклюзивные", "Мифические", "Ультраредкие")
private fun tierPicker(selected: Int): String {
    val options = tiers.indices.reversed().joinToString("") { i ->
        """<option value="$i" ${if (selected == i) "selected" else ""}>${tierTitles[i]}</option>"""
    }
    return """<label class="tier-picker">Редкость <select id="tier-choice"><option value="-1">Все редкости</option>$options</select></label>"""
}
fun showcase(): String {
    val c = cases[currentCase]
    fun oddsRow(i: Int, label: String) =
        """<div class="odd r$i"><i class="swatch"></i>$label<strong>${chanceText(c.tickets[i])}</strong></div>"""
    val odds = (0 until firstExclusiveTier).joinToString("") { oddsRow(it, tiers[it]) }
    val exclusiveOdds = (tiers.lastIndex downTo firstExclusiveTier).joinToString("") { i ->
        oddsRow(i, phones[phonesByTier[i].single()].name.removePrefix("Samsung ").removePrefix("Apple "))
    }
    val buy = if (coins >= c.cost)
        btn("open","Открыть кейс <span class='price'>${rubles(c.cost)}</span>")
    else btn("tab-2","В мастерскую →") + """<p class="hint">Цена — ${rubles(c.cost)}. Не хватает ${rubles(c.cost - coins)}.</p>"""
    val dots = cases.indices.joinToString("") { "<i class='${if (it == currentCase) "on" else ""}'></i>" }
    val models = if (caseTier < 0) ordered else ordered.filter { phones[it].matchesTier(caseTier) }
    return title("Кейсы", "Открывай кейсы и собери все ${activePhoneIds.size} моделей.") +
        """<div class="showcase"><section class="hero theme$currentCase"><div class="casehead"><span class="tag">Кейс ${number(currentCase)} / 03</span><h2>${c.name}</h2></div><div class="artbox"><i class="corner c1"></i><i class="corner c2"></i><img class="caseart" src="${art("case$currentCase")}" alt="Пиксельный кейс ${c.name}"></div><div class="switch"><button class="arrow" data-act="prev" aria-label="Предыдущий кейс">←</button><div class="dots">$dots</div><button class="arrow" data-act="next" aria-label="Следующий кейс">→</button></div></section><section class="side"><h3>Шансы выпадения</h3><div class="odds">$odds</div><details class="exclusive-odds"><summary>Три редчайшие модели</summary><div class="odds">$exclusiveOdds</div></details><div class="buybox">$buy</div></section></div>
        ${historyPanel()}<section class="section"><div class="sectionhead"><h2>Внутри кейса</h2><span class="count">${activePhoneIds.size} моделей</span></div>${tierPicker(caseTier)}${catalogPage(models, casePage)}</section>"""
}
fun catalogPage(ids: List<Int>, page: Int): String {
    if (ids.isEmpty()) return """<p class="empty">Телефонов по этому запросу нет.</p>"""
    val total = (ids.size + pageSize - 1) / pageSize
    val index = page.coerceIn(0, total - 1)
    val pager = if (total <= 1) "" else """<div class="pager"><button class="arrow" data-act="page-prev" aria-label="Предыдущая страница" ${if (index == 0) "disabled" else ""}>←</button><span class="count">${index + 1} / $total</span><button class="arrow" data-act="page-next" aria-label="Следующая страница" ${if (index + 1 == total) "disabled" else ""}>→</button></div>"""
    val first = index * pageSize
    val groups = ids.subList(first, minOf(first + pageSize, ids.size)).groupBy { phones[it].tier }
    return groups.entries.joinToString("", prefix = """<div class="tier-groups">""", postfix = "</div>") { (tier, members) ->
        val single = if (phonesByTier[tier].size == 1) " solo-tier" else ""
        """<section class="tier-group r$tier$single"><h3><i class="swatch"></i>${tierTitles[tier]}</h3>${cards(members)}</section>"""
    } + pager
}
fun searchable(name: String) = name.lowercase().replace("+", "plus").filter { it.isLetterOrDigit() }
private val searchNames = phones.map { searchable(it.name) }
private val exactNameIds = phones.indices.groupBy { searchNames[it] }
private val digitRuns = Regex("""\d+""")
private val searchAliases = mapOf("редми" to "redmi", "ноут" to "note", "нот" to "note", "про" to "pro", "макс" to "max", "сяоми" to "xiaomi")
fun matchingPhones(text: String): List<Int> {
    if (text.isBlank()) return ordered
    val serial = text.trim().removePrefix("№").removePrefix("#").trim()
    val albumNumber = serial.toIntOrNull()
    if (serial != text.trim() && albumNumber != null) {
        return if (albumNumber - 1 in phones.indices) listOf(canonicalId(albumNumber - 1)) else emptyList()
    }
    val words = text.lowercase().trim().split(Regex("\\s+"))
    val year = words.firstOrNull { it.length == 4 && it.toIntOrNull() in 1980..2099 }
    val terms = words.filterNot { it == year }.map { searchable(searchAliases[it] ?: it) }
    val modelDigits = terms.filter { term -> term.isNotEmpty() && term.all { it.isDigit() } }
    val query = terms.joinToString("")
    val exact = exactNameIds[searchable(words.joinToString("") { searchAliases[it] ?: it })]
        .orEmpty().map(::canonicalId).distinct()
    val matches = ordered.filter {
        val phone = phones[it]
        val name = searchNames[it]
        (year == null || phone.year.toString() == year) && (name.contains(query) || terms.all { name.contains(it) }) &&
            modelDigits.all { digits -> digitRuns.findAll(name).any { it.value == digits } }
    }
    if (query.isEmpty()) return (exact + matches).distinct()
    val ranked = matches.sortedBy { id ->
        val name = searchNames[id]
        when {
            name.endsWith(query) -> 0
            name.contains(query) -> 1
            else -> 2
        }
    }
    return exact + ranked.filterNot { it in exact }
}
fun albumResults(): String {
    val matches = matchingPhones(searchText)
    val ids = if (filter == 0 && albumTier < 0) matches else matches.filter {
        (when (filter) { 1 -> it in owned; 2 -> it !in owned; 3 -> it in wishes; else -> true }) && phones[it].matchesTier(albumTier)
    }
    if (ids.isEmpty() && searchText.isBlank() && albumTier < 0) {
        val message = when (filter) {
            1 -> "Здесь появится первый телефон из кейса."
            3 -> "Открой карточку телефона и нажми «Хочу в коллекцию»."
            else -> "Все телефоны уже в альбоме."
        }
        return """<p class="empty">$message</p>"""
    }
    return catalogPage(ids, albumPage)
}
fun album(): String {
    val filters = listOf("Все", "Собраны", "Осталось", "Желания").mapIndexed { i, name ->
        btn("filter-$i", name, "filter " + if (i == filter) "active" else "")
    }.joinToString("")
    val desc = if (owned.size == activePhoneIds.size) "Коллекция собрана целиком!" else "Собрано ${owned.size} из ${activePhoneIds.size} моделей."
    return title("Альбом", desc) + """<div class="meter"><i style="width:${100 * owned.size / activePhoneIds.size}%"></i></div><input type="search" id="search" class="search" placeholder="Название, год или № карточки" aria-label="Найти телефон"><div class="filters">$filters</div>${tierPicker(albumTier)}<div id="results">${albumResults()}</div>"""
}
fun workshop(): String {
    val content = if (job.isEmpty()) {
        """<div class="screen"><p>СЕРВИСНЫЙ ТЕРМИНАЛ</p><strong>+${rubles(repairPay)}</strong><p>ЗА РЕМОНТ ПРОШИВКИ</p></div>""" + btn("start", "Принять заказ")
    } else {
        val keypad = (1..9).joinToString("") { btn("key-$it", it.toString(), "key") }
        """<div class="screen"><p>СЕРВИСНЫЙ КОД</p><strong>$job</strong><p>ВВОД • ${entry.padEnd(4, '·')}</p></div><div class="pad">$keypad</div>"""
    }
    return title("Мастерская", "Ремонтируй телефоны, чтобы заработать на новый кейс.") +
        """<section class="servicebox"><h2>Ремонт прошивки</h2><p class="sub">Введи четырёхзначный код с дисплея.</p>$content<p class="hint" role="status">$jobMessage</p></section>"""
}
fun render() {
    if (tab == mainMenu) {
        document.getElementById("app").innerHTML = menuScreen()
        return
    }
    val view = when (tab) { 1 -> album(); 2 -> workshop(); 3 -> settings(); else -> showcase() }
    val warning = if (storageWarning) """<p class="hint">Хранилище недоступно. Прогресс сохранится только до закрытия игры.</p>""" else ""
    document.getElementById("app").innerHTML = header() + """<main class="wrap">$view$warning</main>"""
    if (tab == 1) document.getElementById("search").value = searchText
}
fun showPhone(id: Int, reward: String? = null) {
    val p = phones[id]
    val rankLine = if (p.tier >= firstExclusiveTier) {
        val rank = tiers.size - p.tier
        """<p class="rank r${p.tier}">№ $rank по редкости · ${chanceText(cases[currentCase].tickets[p.tier])} в любом кейсе</p>"""
    } else ""
    val related = if (p.name in listOf("Xiaomi Redmi Note 8", "Xiaomi Redmi Note 8 2019", "Xiaomi Redmi Note 8 2021")) {
        btn("note8-pro", "Открыть Redmi Note 8 Pro →", "related-phone")
    } else ""
    val text = reward ?: ((if (id in owned) "В твоей коллекции. " else "Пока нет в коллекции. ") + p.note)
    openModal("""<div class="modalbg"><section class="modal" role="dialog" aria-modal="true" aria-labelledby="model-title"><div class="rewardpic r${p.tier}"><img src="${art("phone$id")}" alt="${p.name}"><button class="close" data-act="close" aria-label="Закрыть">×</button></div><div class="modalbody">${rarity(p)}<h2 id="model-title">${p.name}</h2>$rankLine<p>Оценка модели — <b>${rubles(p.price)}</b><br>Выкуп повтора — ${rubles(p.tradeIn)}</p><p>$text</p><small class="credit">${p.yearLabel} · ${p.credit}</small>$related${wishButton(id)}${btn("close", if (reward == null) "Закрыть" else "Продолжить")}</div></section></div>""")
}
private fun showIphoneMemory() {
    val id = phones.indexOfFirst { it.name == "Apple iPhone 12" }
    openModal("""
        <div class="modalbg"><section class="modal memory" role="dialog" aria-modal="true" aria-labelledby="memory-caption">
          <figure>
            <div class="memory-art" style="--wave:url('${art("memory-wave")}')">
              <img src="${art("phone$id")}" alt="Apple iPhone 12">
            </div>
            <figcaption id="memory-caption">Пока, мой iPhone 12. Жаль тебя. На тебе был джейлбрейк Dopamine.</figcaption>
          </figure>
          ${btn("close", "Вернуться в настройки")}
        </section></div>""")
}
internal fun openModal(markup: String) {
    if (!modalOpen) previousFocus = document.activeElement
    val overlay = document.getElementById("overlay")
    overlay.innerHTML = markup
    modalOpen = true
    document.getElementById("app").setAttribute("inert", "")
    overlay.querySelector(".action").focus()
}
fun closeModal() {
    if (!modalOpen) return
    if (revealSpin()) return
    document.getElementById("overlay").innerHTML = ""
    document.getElementById("app").removeAttribute("inert")
    modalOpen = false
    if (previousFocus != null && previousFocus != document.body && previousFocus.isConnected == true) previousFocus.focus()
    else document.querySelector("[data-act='open'],[data-act='tab-2'].action,.filter.active")?.focus()
}
fun creditRubles(amount: Long): Long {
    val credited = amount.coerceAtMost(Long.MAX_VALUE - coins)
    coins += credited
    return credited
}
fun openCase() {
    val c = cases[currentCase]
    if (modalOpen || coins < c.cost) return
    val id = c.drawPhone()
    coins -= c.cost
    opened++
    val isNew = owned.add(id)
    val message = if (isNew) {
        if (owned.size == activePhoneIds.size) "Последняя модель! Ты собрал все ${activePhoneIds.size} моделей." else "Новая модель! Модель добавлена в твой альбом."
    } else {
        val credit = creditRubles(phones[id].tradeIn.toLong())
        "Такая модель уже есть. Выкуп повтора принёс ${rubles(credit)}."
    }
    recordDrop(id, currentCase, isNew)
    save()
    render()
    spinCase(c, id, message)
}
fun enterDigit(digit: String) {
    if (job.isEmpty()) return
    val next = entry + digit
    if (!job.startsWith(next)) {
        entry = ""
        jobMessage = "Неверная цифра. Введи код с начала."
    } else if (next == job) {
        val credit = creditRubles(repairPay.toLong())
        job = ""
        entry = ""
        jobMessage = "Прошивка восстановлена. Получено ${rubles(credit)}!"
        save()
    } else {
        entry = next
        jobMessage = ""
    }
    render()
}
fun navigate(to: Int) {
    leaveAdmin()
    tab = to
    render()
    window.scrollTo(0, 0)
}
fun dispatch(action: String) {
    if (modalOpen) {
        when {
            action == "close" -> closeModal()
            action == "skip-spin" -> revealSpin()
            action == "note8-pro" -> showPhone(phones.indexOfFirst { it.name == "Xiaomi Redmi Note 8 Pro" })
            action.startsWith("wish-") -> {
                val id = action.substringAfter('-').toInt()
                if (!wishes.remove(id)) wishes.add(id)
                save()
                val button = document.querySelector("[data-act='wish-$id']")
                button.textContent = wishLabel(id)
                button.setAttribute("aria-pressed", (id in wishes).toString())
                if (tab == 1 && filter == 3) {
                    albumPage = 0
                    document.getElementById("results").innerHTML = albumResults()
                }
            }
        }
        return
    }
    when {
        action == "menu" -> navigate(mainMenu)
        action == "settings-memory" && tab == 3 && !adminIsOpen -> showIphoneMemory()
        action.startsWith("admin-") && tab == 3 -> adminAction(action)
        action == "open" -> openCase()
        action == "prev" || action == "next" -> {
            currentCase = (currentCase + if (action == "next") 1 else cases.size - 1) % cases.size
            render()
            document.querySelector("[data-act='$action']").focus()
        }
        action.startsWith("tab-") -> navigate(action.substringAfter('-').toInt())
        action.startsWith("filter-") -> { filter = action.substringAfter('-').toInt(); albumPage = 0; render() }
        action.startsWith("page-") -> {
            val step = if (action == "page-next") 1 else -1
            if (tab == 1) albumPage += step else casePage += step
            render()
            document.querySelector(".grid")?.scrollIntoView()
        }
        action.startsWith("phone-") -> showPhone(action.substringAfter('-').toInt())
        action == "start" -> {
            job = (1..4).joinToString("") { Random.nextInt(1, 10).toString() }
            entry = ""
            jobMessage = ""
            render()
        }
        action.startsWith("key-") -> enterDigit(action.substringAfter('-'))
    }
}
fun main() {
    restore()
    render()
    document.addEventListener("click", { e: dynamic ->
        val button = e.target.closest("[data-act]")
        val action = button?.getAttribute("data-act") as? String
        if (action != null) dispatch(action)
    })
    document.addEventListener("change", { e: dynamic ->
        if (e.target.id == "tier-choice") {
            val selected = (e.target.value as String).toInt()
            if (tab == 1) { albumTier = selected; albumPage = 0 }
            else { caseTier = selected; casePage = 0 }
            render()
            document.getElementById("tier-choice").focus()
        }
    })
    document.addEventListener("submit", { e: dynamic ->
        if (tab == 3) {
            e.preventDefault()
            adminAction(e.target.id as String)
        }
    })
    document.addEventListener("input", { e: dynamic ->
        if (e.target.id == "admin-search") updateAdminModels()
        if (e.target.id == "search") {
            searchText = e.target.value as String
            albumPage = 0
            document.getElementById("results").innerHTML = albumResults()
        }
    })
    document.addEventListener("keydown", { e: dynamic ->
        val key = e.key as String
        if (key == "Escape" && modalOpen) closeModal()
        else if (tab == 2 && !modalOpen && key.length == 1 && key[0] in '1'..'9') enterDigit(key)
    })
    window.signalBack = {
        when {
            modalOpen -> { closeModal(); true }
            tab == 3 && adminIsOpen -> { navigate(3); true }
            tab == mainMenu -> false
            tab == 0 -> { navigate(mainMenu); true }
            tab in labels.indices -> { navigate(0); true }
            else -> false
        }
    }
}
