package com.phonesimulator.game

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.content.res.ColorStateList
import android.text.Editable
import android.text.TextWatcher
import android.text.InputType
import android.widget.EditText
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import kotlin.random.Random

class MainActivity : Activity() {
    private lateinit var store: ProgressStore
    private var progress = Progress()
    private lateinit var root: LinearLayout
    private var page = "home"
    private var reward = -1
    private var animator: ValueAnimator? = null
    private var selectedPhone = 0
    private var wikiQuery = ""
    private var adminUnlocked = false
    private var balanceLabel: TextView? = null
    private var catalogQuery = ""
    private var favoritesOnly = false
    private var upgradeSource = -1
    private var upgradeTarget = -1
    private val ink = Color.rgb(10, 14, 20)
    private val muted = Color.rgb(221, 216, 245)
    private val mint = Color.rgb(246, 232, 255)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = ProgressStore(this)
        progress = store.load()
        page = savedInstanceState?.getString("page") ?: "home"
        reward = savedInstanceState?.getInt("reward", -1) ?: -1
        selectedPhone = savedInstanceState?.getInt("selectedPhone", 0)?.coerceIn(Catalog.phones.indices) ?: 0
        wikiQuery = savedInstanceState?.getString("wikiQuery").orEmpty()
        catalogQuery = savedInstanceState?.getString("catalogQuery").orEmpty()
        favoritesOnly = savedInstanceState?.getBoolean("favoritesOnly") ?: false
        upgradeSource = savedInstanceState?.getInt("upgradeSource", -1) ?: -1
        upgradeTarget = savedInstanceState?.getInt("upgradeTarget", -1) ?: -1
        render()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("page", page)
        outState.putString("catalogQuery", catalogQuery)
        outState.putBoolean("favoritesOnly", favoritesOnly)
        outState.putInt("upgradeSource", upgradeSource)
        outState.putInt("upgradeTarget", upgradeTarget)
        outState.putInt("selectedPhone", selectedPhone)
        outState.putString("wikiQuery", wikiQuery)
        outState.putInt("reward", reward)
        super.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        // The reward is already saved; backgrounding never loses a paid case.
        animator?.cancel()
        animator = null
        adminUnlocked = false
        if (page == "result" || page == "admin") render()
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun shape(color: Int = Color.WHITE, accent: Int = Color.WHITE) = GradientDrawable(
        GradientDrawable.Orientation.TL_BR,
        intArrayOf(0xFF343940.toInt(), 0xEE141821.toInt(), 0xEE20232E.toInt())
    ).apply {
        cornerRadius = dp(34).toFloat()
        setStroke(dp(1), Color.argb(150, Color.red(accent), Color.green(accent), Color.blue(accent)))
    }

    private fun label(text: String, size: Float = 18f, color: Int = Color.WHITE,
                      parent: LinearLayout = root): TextView = TextView(this).apply {
        this.text = text
        textSize = size
        setTextColor(color)
        setPadding(dp(8), dp(10), dp(8), dp(10))
        parent.addView(this, LinearLayout.LayoutParams(-1, -2))
    }

    private fun button(text: String, parent: LinearLayout = root, action: () -> Unit): Button =
        Button(this).apply {
            this.text = text
            textSize = 16f
            isAllCaps = false
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            background = RippleDrawable(ColorStateList.valueOf(0x40FFFFFF), shape(), shape())
            minHeight = dp(56)
            setPadding(dp(16), dp(12), dp(16), dp(12))
            parent.addView(this, LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(10); bottomMargin = dp(6)
            })
            setOnClickListener { action() }
        }

    private fun screen(title: String) {
        val scroll = ScrollView(this).apply {
            background = GradientDrawable(GradientDrawable.Orientation.BL_TR,
                intArrayOf(0xFF050608.toInt(), 0xFF101624.toInt(), 0xFF050608.toInt()))
            isFillViewport = true
        }
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        }
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(18), dp(20), dp(24))
        }
        container.addView(root, LinearLayout.LayoutParams(
            minOf(resources.displayMetrics.widthPixels, dp(640)), -2))
        container.addOnLayoutChangeListener { _, left, _, right, _, _, _, _, _ ->
            val available = (right - left).coerceAtLeast(0)
            val targetWidth = minOf(available, dp(640))
            if (targetWidth > 0 && root.layoutParams.width != targetWidth) {
                root.layoutParams = root.layoutParams.apply { width = targetWidth }
            }
        }
        scroll.addView(container)
        val frame = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(ink) }
        frame.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        val dock = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; background = shape()
            setPadding(dp(4), dp(4), dp(4), dp(4))
        }
        listOf("Каталог" to "catalog", "Кейсы" to "cases", "Инвентарь" to "collection",
            "Кликер" to "clicker", "Апгрейд" to "upgrade", "Профиль" to "profile").forEach { (name, route) ->
            dock.addView(Button(this).apply {
                text = name; textSize = 10f; isAllCaps = false
                minWidth = 0; minimumWidth = 0; setPadding(dp(2),0,dp(2),0)
                setTextColor(if (page == route || page == "home" && route == "catalog") 0xFF95B6FF.toInt() else muted)
                background = if (page == route) shape() else null
                setOnClickListener { navigate(route) }
                contentDescription = "Раздел $name"
            }, LinearLayout.LayoutParams(0, dp(60), 1f))
        }
        frame.addView(dock, LinearLayout.LayoutParams(-1,-2).apply {
            setMargins(dp(6),dp(4),dp(6),dp(4))
        })
        frame.setOnApplyWindowInsetsListener { view, insets ->
            if (android.os.Build.VERSION.SDK_INT >= 30) {
                val safe = insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout())
                view.setPadding(safe.left, safe.top, safe.right, safe.bottom)
            } else {
                @Suppress("DEPRECATION")
                view.setPadding(insets.systemWindowInsetLeft, insets.systemWindowInsetTop,
                    insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
            }
            insets
        }
        setContentView(frame)
        frame.requestApplyInsets()
        label("P H O N E   L A B", 14f, mint).apply {
            letterSpacing = 0.14f
        }
        label(title, 30f).setTypeface(null, Typeface.BOLD)
        balanceLabel = label("◈ ${progress.coins} ₽  ·  ${progress.owned.size + progress.used.size} телефонов", 16f, muted)
        if (title != "Открываем кейс…" && progress.giftCooldownRemaining() > 0L) {
            val countdown = label("", 16f, muted)
            countdown.post(object : Runnable {
                override fun run() {
                    if (!countdown.isAttachedToWindow || isDestroyed || isFinishing) return
                    val remaining = progress.giftCooldownRemaining()
                    if (remaining == 0L) { render(); return }
                    val seconds = (remaining + 999L) / 1000L
                    countdown.text = String.format(java.util.Locale.ROOT,
                        "Подарок через %02d:%02d", seconds / 60L, seconds % 60L)
                    countdown.postDelayed(this, 1000L)
                }
            })
        }
        if (title != "Открываем кейс…" && progress.canReceiveGift(currentListings())) {
            label("Не хватает ни на кейс, ни на телефон? Забери подарок и продолжай!", 16f, mint)
            button("Получить подарок · ${Economy.BONUS} ₽") {
                val next = progress.receiveGift(currentListings())
                if (persist(next)) render()
            }
        }
    }

    private fun currentListings() = Market.listings(System.currentTimeMillis() / 86_400_000L)

    private fun card(index: Int, count: Int? = null, parent: LinearLayout = root) {
        val phone = Catalog.phones[index]
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            background = shape(Color.WHITE, phone.rarity.accent)
            elevation = dp(2).toFloat()
            parent.addView(this, LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(16); bottomMargin = dp(12)
            })
        }
        panel.addView(PhoneArtView(this, phone), LinearLayout.LayoutParams(-1, dp(156)))
        label(phone.rarity.title.uppercase(), 12f, phone.rarity.accent, panel)
        label(phone.name, 20f, parent = panel).setTypeface(null, Typeface.BOLD)
        label("${phone.price} ₽", 20f, parent = panel)
        button(if (index in progress.favorites) "♥ В избранном" else "♡ В избранное", panel) {
            if (persist(progress.toggleFavorite(index))) render()
        }
        label("Шанс ${phone.tickets / 10.0}%" + (count?.let { "  ·  В коллекции: $it" } ?: ""),
            14f, muted, panel)
        if(index != 5) button("Купить новый · ${ExpansionRules.newPrice(index)} ₽", panel) {
            val next = progress.buyNew(index)
            if(next == progress) Toast.makeText(this,"Недостаточно денег",Toast.LENGTH_SHORT).show()
            else if(persist(next,"Покупка нового телефона")) render()
        }
        button("О телефоне", panel) { selectedPhone = index; navigate("details") }
        if (count != null) button("Продать один · ${phone.price} ₽", panel) {
            if (persist(progress.sell(index))) render()
        }
    }

    private fun persist(next: Progress, reason: String = "Игровая операция"): Boolean {
        val decorated = next.copy(badges = next.badges + Progression.goals(next),
            expansion = next.expansion.record(progress, next, System.currentTimeMillis(), reason))
        if (!store.save(decorated)) {
            Toast.makeText(this, "Не удалось сохранить прогресс. Повторите попытку.", Toast.LENGTH_LONG).show()
            return false
        }
        progress = decorated
        return true
    }

    private fun navigate(destination: String) { animator?.cancel(); animator = null; page = destination; render() }
    private fun render() {
        when (page) {
            "expansion" -> expansionHub()
            "contracts" -> contracts()
            "clients" -> clients()
            "quests" -> quests()
            "catalog" -> catalog()
            "clicker" -> clicker()
            "upgrade" -> upgrader()
            "profile" -> profile()
            "daily" -> daily()
            "workshop" -> workshop()
            "cases" -> cases()
            "market" -> market()
            "wiki" -> wiki()
            "details" -> details()
            "admin" -> admin()
            "collection" -> {
                screen("Коллекция")
                button("Прошивки и джейлбрейк") { navigate("workshop") }
                if (progress.used.isNotEmpty()) {
                    label("Б/у телефонов: ${progress.used.size}", 18f)
                    button("Посмотреть и продать б/у") { navigate("market") }
                }
                if (progress.owned.isEmpty() && progress.used.isEmpty()) label("Первый телефон ждёт в кейсе.", color = muted)
                pagedPhones(progress.owned.distinct().sorted(), root) { phoneGrid(it, root, true) }
                button("На главную") { navigate("home") }
            }
            "shop" -> {
                screen("Магазин")
                label("Пополняй запас и продолжай коллекцию", 22f)
                label("Бонус покрывает один классический кейс. Уже накопленный баланс сохранён.", 15f, muted)
                label("Бесплатные ${Economy.BONUS} ₽. Без рекламы и покупок за реальные деньги.", color = muted)
                button("Получить ${Economy.BONUS} ₽") {
                    if (progress.giftCooldownRemaining() > 0L) {
                        Toast.makeText(this, "Подарок ещё перезаряжается", Toast.LENGTH_SHORT).show()
                    } else if (persist(progress.bonus())) render()
                }
                label("Общая перезарядка подарка и бонуса — 10 минут.", 15f, muted)
                button("На главную") { navigate("home") }
            }
            "result" -> {
                if (reward !in Catalog.phones.indices) { navigate("home"); return }
                screen("Новый телефон!")
                card(reward)
                label("Телефон уже сохранён в коллекции.", 15f, muted)
                button("Ещё кейс · ${CaseKind.CLASSIC.cost} ₽") { openCase() }
                button("Коллекция") { navigate("collection") }
                button("На главную") { navigate("home") }
            }
            else -> {
                screen("Открой свой следующий")
                button("Ежедневные задания") { navigate("quests") }
                button("Ежедневная награда") { navigate("daily") }
                button("Каталог телефонов") { navigate("catalog") }
                label("Каждый кейс — новый шанс", 22f)
                card(5)
                label("Samsung Z Fold 8 Ultra\nУльтралегендарный · 1 из 1000", 17f, muted)
                button("Открыть кейс · ${CaseKind.CLASSIC.cost} ₽") { openCase() }
                button("Коллекция") { navigate("collection") }
                button("Магазин") { navigate("shop") }
                button("Новое в 3.0") { navigate("expansion") }
                button("Клиенты и сервис") { navigate("clients") }
                button("Мастерская · прошивки") { navigate("workshop") }
                button("Все кейсы · 4 вида") { navigate("cases") }
                button("Авито · рынок б/у") { navigate("market") }
                button("Википедия телефонов · ${Catalog.phones.size} моделей") { navigate("wiki") }
                button("Админ-панель") { navigate("admin") }
                label("Шансы по редкостям", 21f)
                Rarity.entries.forEach { rarity ->
                    val tickets = Catalog.phones.filter { it.rarity == rarity }.sumOf { it.tickets }
                    infoRow(rarity.title, "${tickets / 10.0}%")
                }
            }
        }
    }

    private fun phoneGrid(models: List<Int>, target: LinearLayout, inventory: Boolean = false) {
        models.chunked(2).forEach { pair ->
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            target.addView(row, LinearLayout.LayoutParams(-1,-2))
            pair.forEach { index ->
                val cell = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
                row.addView(cell, LinearLayout.LayoutParams(0,-2,1f).apply { setMargins(dp(3),0,dp(3),0) })
                card(index, if (inventory) progress.owned.count { it == index } else null, cell)
            }
            if (pair.size == 1) row.addView(View(this), LinearLayout.LayoutParams(0,1,1f))
        }
    }

    /** Build a small batch of views at a time, including when searching 500 models. */
    private fun pagedPhones(models: List<Int>, parent: LinearLayout, renderPage: (List<Int>) -> Unit) {
        var shown = 0
        var more: Button? = null
        fun nextPage() {
            more?.let { parent.removeView(it) }
            val page = models.drop(shown).take(32)
            renderPage(page)
            shown += page.size
            more = if (shown < models.size) button("Показать ещё · $shown из ${models.size}", parent) { nextPage() } else null
        }
        nextPage()
    }

    private fun catalog() {
        screen("Каталог телефонов")
        label("${Catalog.phones.size} моделей в коллекции игры", 16f, muted)
        button("Ежедневная награда") { navigate("daily") }
        button(if (favoritesOnly) "Показать все" else "Только избранное") { favoritesOnly = !favoritesOnly; render() }
        lateinit var list: LinearLayout
        fun refresh() {
            list.removeAllViews()
            val matches = Catalog.phones.indices.filter { index ->
                Catalog.phones[index].name.contains(catalogQuery.trim(), true) && (!favoritesOnly || index in progress.favorites)
            }
            if (matches.isEmpty()) label("Ничего не найдено", parent = list)
            pagedPhones(matches, list) { phoneGrid(it, list) }
        }
        searchField("Поиск телефонов", catalogQuery) { catalogQuery = it; refresh() }
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; root.addView(this) }
        refresh()
        button("Википедия телефонов") { navigate("wiki") }
        button("На главную") { navigate("home") }
    }

    private fun daily() {
        screen("Ежедневная награда")
        val day = System.currentTimeMillis() / Progression.DAY_MS
        val nextStreak = if (day == progress.lastDailyDay+1) (progress.dailyStreak+1).coerceAtMost(7) else 1
        button("Ежедневные задания") { navigate("quests") }
        label("Серия: ${progress.dailyStreak} / 7 дней", 23f)
        label("Один раз за календарный день UTC. Пропуск дня сбрасывает серию. Это отдельная ежедневная награда; подарок сохраняет перезарядку 10 минут.", 15f, muted)
        if (day <= progress.lastDailyDay) label("Сегодня награда уже получена")
        else button("Забрать награду · ${Progression.dailyReward(nextStreak)} ₽") {
            if (persist(progress.claimDaily(System.currentTimeMillis()/Progression.DAY_MS))) render()
        }
        (1..7).forEach { infoRow("День $it", "${Progression.dailyReward(it)} ₽") }
        button("На главную") { navigate("home") }
    }

    private fun clicker() {
        screen("Кликер")
        label("Нажимай и получай +10 ₽", 24f)
        val score = label("Нажатий: ${progress.clicks}", 20f)
        button("◉ +10 ₽") {
            if (persist(progress.clickCoin())) {
                score.text = "Нажатий: ${progress.clicks}"
                balanceLabel?.text = "◈ ${progress.coins} ₽  ·  ${progress.owned.size + progress.used.size} телефонов"
            }
        }.apply {
            textSize = 28f; setTextColor(0xFF795600.toInt())
            layoutParams = LinearLayout.LayoutParams(dp(200),dp(200)).apply {
                gravity = Gravity.CENTER_HORIZONTAL; topMargin = dp(24); bottomMargin = dp(24)
            }
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR,
                intArrayOf(0xFFFFED91.toInt(),0xFFFFBD27.toInt())).apply { shape = GradientDrawable.OVAL }
        }
        label("Доход сохраняется после каждого нажатия. Автоклик не начисляет деньги в фоне.", 15f, muted)
        button("На главную") { navigate("home") }
    }

    private fun upgrader() {
        screen("Апгрейдер")
        label("Обмен с риском: исходный телефон расходуется при любом результате. Денег не списываем. Ultra здесь получить нельзя.", 15f, muted)
        val targets = if (upgradeSource in progress.owned) Catalog.phones.indices.filter {
            it != 5 && Catalog.phones[it].price > Catalog.phones[upgradeSource].price
        }.sortedBy { Catalog.phones[it].price } else emptyList()
        if (targets.isNotEmpty()) {
            if (upgradeTarget !in targets) upgradeTarget = targets.first()
            val panel = glassPanel()
            infoRow("Исходный", Catalog.phones[upgradeSource].name, panel)
            infoRow("Цель", Catalog.phones[upgradeTarget].name, panel)
            val chance = progress.upgradeChance(upgradeSource, upgradeTarget)
            label("Шанс: ${chance/10.0}%", 30f, parent = panel)
            button("Попробовать улучшить", panel) {
                if (upgradeSource in progress.owned) {
                    val (next, won) = progress.upgrade(upgradeSource, upgradeTarget, Random.nextInt(1000))
                    if (persist(next)) {
                        upgradeSource = -1; upgradeTarget = -1
                        render()
                        Toast.makeText(this, if (won) "Улучшение успешно!" else "Неудача: исходный телефон израсходован", Toast.LENGTH_LONG).show()
                    }
                }
            }
            label("Выбери цель", 22f)
            targets.forEach { target -> button("Цель: ${Catalog.phones[target].name}") { upgradeTarget = target; render() } }
        } else label("Выбери телефон ниже. Нужна модель с более дорогой доступной целью.", 17f, muted)
        label("Твои телефоны из кейсов", 23f)
        progress.owned.distinct().forEach { source ->
            button("Выбрать ${Catalog.phones[source].name}") { upgradeSource = source; upgradeTarget = -1; render() }
        }
        if (progress.owned.isEmpty()) label("Для апгрейда нужен телефон из кейса или админ-панели.", 16f, muted)
        button("На главную") { navigate("home") }
    }

    private fun expansionHub() {
        screen("Новое в 3.0")
        label("1 · Магазин новых телефонов",24f)
        label("Новые аппараты стоят 110% базовой оценки. Ultra напрямую не продаётся.",15f,muted)
        button("Магазин новых телефонов") { navigate("catalog") }
        label("2 · Торг и 3 · Реставрация",24f)
        label("Одна попытка скидки 15% на объявление, шанс 60%. Свои б/у телефоны можно ремонтировать до следующего состояния.",15f,muted)
        button("Торг и ремонт на рынке") { navigate("market") }
        label("4 · Награды за достижения",24f)
        Progression.goalNames.forEach { (id,title) ->
            val panel=glassPanel()
            label("$title · ${ExpansionRules.awardPrices.getValue(id)} ₽",18f,parent=panel)
            when {
                id in progress.expansion.awardsClaimed -> label("Награда получена",15f,muted,panel)
                id in (progress.badges+Progression.goals(progress)) -> button("Забрать награду: $title",panel) {
                    if(persist(progress.claimAward(id),"Награда за достижение")) render()
                }
                else -> label("Сначала выполни достижение",15f,muted,panel)
            }
        }
        label("5 · Контракты обмена",24f)
        button("Открыть контракты") { navigate("contracts") }
        label("6 · Заказ на закупку",24f)
        val day=System.currentTimeMillis()/Progression.DAY_MS
        val wanted=ExpansionRules.deliveryModel(day)
        label("Нужен новый ${Catalog.phones[wanted].name}. Оплата ${ExpansionRules.deliveryPrice(day)} ₽. Один заказ за день UTC; б/у не подходит.",16f)
        if(day<=progress.expansion.deliveryDay) label("Заказ дня выполнен",16f,muted)
        else button("Передать телефон по заказу") {
            val next=progress.deliver(day)
            if(next==progress) Toast.makeText(this,"Нужной модели нет в коллекции новых телефонов",Toast.LENGTH_SHORT).show()
            else if(persist(next,"Заказ на закупку")) render()
        }
        label("7 · Доход сервисного центра",24f)
        label("${progress.workshopLevel*100} ₽ за полную минуту, максимум за 2 часа. Начисление начинается после активации. После сбора отсчёт идёт заново; используется текущий уровень сервиса.",15f,muted)
        button(if(progress.expansion.idleAt==0L) "Активировать доход сервиса" else "Забрать доход сервиса") {
            val next=progress.claimIdle(System.currentTimeMillis())
            if(next==progress) Toast.makeText(this,"Ещё не прошла полная минута",Toast.LENGTH_SHORT).show()
            else if(persist(next,"Доход сервиса")) render()
        }
        label("8 · Сейф",24f)
        infoRow("В сейфе", "${progress.expansion.vault} ₽")
        label("Отдельное хранение без процентов. Лимит кошелька и сейфа — по 2 147 483 647 ₽.",14f,muted)
        val amount=searchField("Сумма перевода") { }
        amount.id=R.id.vault_money;amount.inputType=InputType.TYPE_CLASS_NUMBER
        amount.filters=arrayOf(android.text.InputFilter.LengthFilter(10))
        fun transfer(deposit: Boolean) {
            val value=amount.text.toString().toLongOrNull()
            if(value==null || value !in 1L..Int.MAX_VALUE.toLong()) {
                Toast.makeText(this,"Введите положительную сумму до 2147483647",Toast.LENGTH_SHORT).show();return
            }
            val next=if(deposit) progress.moveToVault(value.toInt()) else progress.withdrawVault(value.toInt())
            if(next==progress) Toast.makeText(this,"Недостаточно средств или достигнут лимит",Toast.LENGTH_SHORT).show()
            else if(persist(next,"Перевод в сейф")) render()
        }
        button("Положить в сейф") { transfer(true) }
        button("Забрать из сейфа") { transfer(false) }
        label("9 · Финансы",24f)
        label("Движение денег с версии 3.0. Переводы в сейф не считаются заработком. Стоимость коллекции сюда не включена.",14f,muted)
        infoRow("Всего получено", "${progress.expansion.income} ₽")
        infoRow("Всего потрачено", "${progress.expansion.expenses} ₽")
        progress.expansion.ledger.asReversed().forEach { event ->
            label("${android.text.format.DateFormat.format("dd.MM HH:mm",event.at)} · ${event.title} · ${if(event.amount>0) "+" else ""}${event.amount} ₽",13f,muted)
        }
        label("10 · Локальная резервная копия",24f)
        label("Одна копия внутри приложения. Удаление его данных удалит и копию. Восстановление заменяет деньги и коллекцию; таймер подарка и отметки ограниченных наград не откатываются.",14f,muted)
        val at=store.backupTime()
        label(if(at==0L) "Копии пока нет" else "Копия: ${android.text.format.DateFormat.format("dd.MM.yyyy HH:mm",at)}",15f)
        button("Создать резервную копию") {
            android.app.AlertDialog.Builder(this).setTitle("Сохранить копию?")
                .setMessage("Предыдущая локальная копия будет заменена текущим прогрессом.")
                .setNegativeButton("Отмена",null).setPositiveButton("Сохранить") { _,_ ->
                    val saved=store.createBackup()
                    Toast.makeText(this,if(saved) "Копия сохранена" else "Не удалось сохранить",Toast.LENGTH_SHORT).show()
                    render()
                }.show()
        }
        if(at>0L) button("Восстановить резервную копию") {
            android.app.AlertDialog.Builder(this).setTitle("Восстановить прогресс?")
                .setMessage("Текущие деньги, коллекция и настройки будут заменены локальной копией. Отметки выданных ограниченных наград сохранятся.")
                .setNegativeButton("Отмена",null).setPositiveButton("Восстановить") { _,_ ->
                    if(store.restoreBackup()) { progress=store.load();render() }
                    else Toast.makeText(this,"Копия недоступна или повреждена",Toast.LENGTH_SHORT).show()
                }.show()
        }
        button("На главную") { navigate("home") }
    }

    private fun contracts() {
        screen("Контракты обмена")
        label("Три новых телефона одной редкости → один случайный следующей редкости. Используются первые три подходящих экземпляра, б/у не расходуется. Источники исчезнут. Ultra исключена.",15f,muted)
        Rarity.entries.filter { it.ordinal<Rarity.LEGENDARY.ordinal }.forEach { rarity ->
            val panel=glassPanel()
            label("${rarity.title} → ${Rarity.entries[rarity.ordinal+1].title}",21f,parent=panel)
            val inputs=progress.contractInputs(rarity)
            label("Подходящих: ${inputs.size}/3",16f,parent=panel)
            inputs.forEach { label(Catalog.phones[it.value].name,15f,muted,panel) }
            if(inputs.size==3) {
                val fee=progress.contractFee(rarity)
                label("Доплата: $fee ₽. Рассчитана по средней цене награды, чтобы избежать бесконечной выгодной перепродажи.",14f,muted,panel)
                button("Заключить контракт: ${rarity.title}",panel) {
                    android.app.AlertDialog.Builder(this).setTitle("Подтвердить обмен")
                        .setMessage("Будут израсходованы: ${inputs.joinToString { Catalog.phones[it.value].name }}. Доплата $fee ₽. Вернуть исходные телефоны нельзя.")
                        .setNegativeButton("Отмена",null).setPositiveButton("Обменять") { _,_ ->
                            val next=progress.contract(rarity,Random.nextInt(1000))
                            if(next==progress) Toast.makeText(this,"Не хватает денег или телефонов",Toast.LENGTH_SHORT).show()
                            else if(persist(next,"Контракт обмена")) render()
                        }.show()
                }
            }
        }
        button("Новое в 3.0") { navigate("expansion") }
    }

    private fun clients() {
        val now = System.currentTimeMillis()
        val synced = progress.syncClients(now)
        if (synced != progress) persist(synced)
        screen("Клиенты и сервис")
        label("Клиенты — игровые персонажи. Их устройства не попадают в твою коллекцию. Один заказ одновременно; новый посетитель каждые 2 минуты, очередь до 3 предложений.", 15f, muted)
        infoRow("Уровень сервиса", "${progress.workshopLevel}/5")
        infoRow("Репутация", progress.reputation.toString())
        if (progress.workshopLevel < 5) {
            val cost = Clients.upgradeCosts[progress.workshopLevel-1]
            val rep = Clients.reputationRequired[progress.workshopLevel-1]
            button("Улучшить сервис · $cost ₽ · репутация $rep") {
                val next = progress.improveWorkshop()
                if (next == progress) Toast.makeText(this, "Недостаточно денег или репутации", Toast.LENGTH_SHORT).show()
                else if (persist(next)) render()
            }
        }
        label("Уровень 2: кастомные прошивки. Уровень 3: джейлбрейк. Развитие ускоряет работу и повышает оплату.", 15f, muted)
        val job = progress.clientJob
        if (job != null) {
            val panel = glassPanel()
            label("В работе: ${job.offer.customer}", 22f, parent = panel)
            label("${Catalog.phones[job.offer.phone].name} · ${job.offer.kind.title}", 17f, parent = panel)
            val status = label("", 20f, mint, panel)
            val complete = button("Выдать телефон · ${job.offer.reward} ₽", panel) {
                if (persist(progress.finishClient(System.currentTimeMillis()))) render()
            }
            status.post(object : Runnable {
                override fun run() {
                    if (!status.isAttachedToWindow || isDestroyed || isFinishing) return
                    val remaining = (job.readyAt-System.currentTimeMillis()).coerceAtLeast(0L)
                    status.text = if (remaining == 0L) "Работа завершена" else "Осталось ${(remaining+999)/1000} с"
                    complete.isEnabled = remaining == 0L
                    if (remaining > 0) status.postDelayed(this, 1000)
                }
            })
        }
        val arrivals = label("", 16f, muted)
        val renderedSlot = now / Clients.INTERVAL
        arrivals.post(object : Runnable {
            override fun run() {
                if (!arrivals.isAttachedToWindow || isDestroyed || isFinishing) return
                val time = System.currentTimeMillis()
                if (time / Clients.INTERVAL > renderedSlot) { render(); return }
                val left = (((renderedSlot+1)*Clients.INTERVAL-time).coerceAtLeast(0L)+999)/1000
                arrivals.text = String.format(java.util.Locale.ROOT, "Новый посетитель через %02d:%02d", left/60, left%60)
                arrivals.postDelayed(this, 1000)
            }
        })
        label("Посетители", 24f)
        progress.clientOffers(now).forEach { offer ->
            val panel = glassPanel()
            label("${offer.customer} · ${Catalog.phones[offer.phone].name}", 22f, parent = panel)
            label(offer.kind.title, 18f, parent = panel)
            infoRow("Детали, списание сразу", "${offer.cost} ₽", panel)
            infoRow("Оплата при выдаче", "${offer.reward} ₽", panel)
            infoRow("Чистый доход", "${offer.reward-offer.cost} ₽", panel)
            infoRow("Работа", "${offer.seconds} с", panel)
            if (progress.workshopLevel < offer.kind.level) label("Нужен уровень ${offer.kind.level}", 16f, muted, panel)
            else if (job == null) button("Принять: ${offer.customer} · ${offer.kind.title}", panel) {
                val next = progress.acceptClient(offer.slot, System.currentTimeMillis())
                if (next.clientJob == null) Toast.makeText(this, "Заказ недоступен или не хватает денег на детали", Toast.LENGTH_SHORT).show()
                else if (persist(next)) render()
            }
        }
        button("Обновить посетителей") { render() }
        button("Прошивки и джейлбрейк") { navigate("workshop") }
        button("На главную") { navigate("home") }
    }

    private fun quests() {
        val day = System.currentTimeMillis()/Progression.DAY_MS
        val ready = progress.forQuestDay(day)
        if (ready != progress) persist(ready)
        screen("Ежедневные задания")
        label("Сброс — в полночь UTC. Каждая награда выдаётся один раз за день. Перезарядка подарка остаётся 10 минут.", 15f, muted)
        listOf(Triple("clicks", "Сделать 100 кликов", "${progress.questClicks}/100 · 5 000 ₽"),
            Triple("cases", "Открыть 3 кейса", "${progress.questCases}/3 · 10 000 ₽")).forEach { (id, title, detail) ->
            val panel = glassPanel()
            label(title, 22f, parent = panel)
            label(detail, 17f, muted, panel)
            if (id in progress.claimedQuests) label("Награда получена", 16f, muted, panel)
            else button(if (id == "clicks") "Забрать награду за клики" else "Забрать награду за кейсы", panel) {
                val next = progress.claimQuest(id, System.currentTimeMillis()/Progression.DAY_MS)
                if (id !in next.claimedQuests || next == progress) Toast.makeText(this, "Сначала выполни задание", Toast.LENGTH_SHORT).show()
                else if (persist(next)) render()
            }
        }
        button("Кликер") { navigate("clicker") }
        button("На главную") { navigate("home") }
    }

    private fun profile() {
        val earned = progress.badges + Progression.goals(progress)
        if (earned != progress.badges) persist(progress.copy(badges = earned))
        screen("Профиль")
        button("Новое в 3.0") { navigate("expansion") }
        label(progress.nickname, 27f)
        val input = searchField("Имя игрока", progress.nickname) { }
        button("Сохранить имя") {
            val name = input.text.toString().trim().take(24).ifBlank { "Коллекционер" }
            if (persist(progress.copy(nickname = name))) render()
        }
        val all = progress.owned + progress.used.map { it.phone }
        val panel = glassPanel()
        infoRow("Баланс", "${progress.coins} ₽", panel)
        infoRow("В коллекции", all.size.toString(), panel)
        infoRow("Разных моделей", all.toSet().size.toString(), panel)
        infoRow("Избранных", progress.favorites.size.toString(), panel)
        infoRow("Кейсов с версии 2.6", progress.casesOpened.toString(), panel)
        infoRow("Нажатий", progress.clicks.toString(), panel)
        infoRow("Клиентов обслужено", progress.servicesCompleted.toString(), panel)
        infoRow("Репутация", progress.reputation.toString(), panel)
        button("Клиенты и сервис") { navigate("clients") }
        Rarity.entries.forEach { rarity ->
            infoRow(rarity.title, all.count { Catalog.phones[it].rarity == rarity }.toString(), panel)
            panel.addView(android.widget.ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
                max = all.size.coerceAtLeast(1)
                progress = all.count { Catalog.phones[it].rarity == rarity }
                progressTintList = ColorStateList.valueOf(rarity.accent)
            }, LinearLayout.LayoutParams(-1, dp(10)))
        }
        label("Достижения", 24f)
        val unlocked = progress.badges + Progression.goals(progress)
        Progression.goalNames.forEach { (id, title) -> label((if(id in unlocked) "✓ " else "○ ")+title, 18f) }
        button("Админ-панель") { navigate("admin") }
        button("На главную") { navigate("home") }
    }

    private fun glassPanel(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(16), dp(16), dp(16), dp(16))
        background = shape()
        root.addView(this, LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = dp(14); bottomMargin = dp(14)
        })
    }

    private fun infoRow(key: String, value: String, parent: LinearLayout = root) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(6), 0, dp(6))
        }
        fun cell(text: String, weight: Float, color: Int, align: Int) {
            row.addView(TextView(this).apply {
                this.text = text; textSize = 16f; setTextColor(color)
                gravity = align; setPadding(dp(6), dp(6), dp(6), dp(6))
            }, LinearLayout.LayoutParams(0, -2, weight))
        }
        cell(key, 0.9f, muted, Gravity.START)
        cell(value, 1.3f, Color.WHITE, Gravity.END)
        parent.addView(row)
    }

    private fun searchField(hint: String, initial: String = "", onChange: (String) -> Unit): EditText =
        EditText(this).apply {
            this.hint = hint
            setTextColor(Color.WHITE); setHintTextColor(muted)
            textSize = 17f; isSingleLine = true
            background = shape()
            setPadding(dp(18), dp(16), dp(18), dp(16))
            setText(initial)
            root.addView(this, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(12) })
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    onChange(s.toString())
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }

    private fun wiki() {
        screen("Википедия телефонов")
        label("${Catalog.phones.size} моделей · характеристики и история", 16f, muted)
        button("На главную") { navigate("home") }
        lateinit var list: LinearLayout
        fun refresh(query: String) {
            list.removeAllViews()
            val matches = Catalog.phones.indices.filter {
                val p = Catalog.phones[it]
                "${p.name} ${p.rarity.title} ${p.chip} ${p.system}".contains(query.trim(), ignoreCase = true)
            }
            if (matches.isEmpty()) label("Ничего не найдено", parent = list)
            pagedPhones(matches, list) { page -> page.forEach { index ->
                val phone = Catalog.phones[index]
                button("${phone.name}\n${phone.rarity.title} · ${phone.tickets / 10.0}%", list) {
                    selectedPhone = index; navigate("details")
                }
            } }
        }
        searchField("Поиск модели, чипа или системы", wikiQuery) { wikiQuery = it; refresh(it) }
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; root.addView(this) }
        refresh(wikiQuery)
    }

    private fun details() {
        val phone = Catalog.phones[selectedPhone]
        screen(phone.name)
        val panel = glassPanel()
        panel.addView(PhoneArtView(this, phone), LinearLayout.LayoutParams(-1, dp(190)))
        infoRow("Устройство", phone.name, panel)
        infoRow("Редкость", phone.rarity.title, panel)
        infoRow("Шанс в классическом", "${phone.tickets / 10.0}%", panel)
        infoRow("Чипсет", phone.chip, panel)
        infoRow("Дисплей", phone.display, panel)
        infoRow("Система", phone.system, panel)
        infoRow("Оценка РФ · ₽", phone.price.toString(), panel)
        infoRow("Прошивка в игре", progress.firmware[selectedPhone] ?: "Заводская", panel)
        label("Цены — офлайн-ориентиры рынка РФ, не текущие предложения магазинов. Год/память/состояние влияют на реальную цену.", 13f, muted, panel)
        infoRow("В коллекции", (progress.owned.count { it == selectedPhone } + progress.used.count { it.phone == selectedPhone }).toString(), panel)
        label(phone.description, 17f, parent = panel)
        label("Справочник офлайн. Данные относятся к указанной версии модели и системе при выпуске; региональные варианты могут отличаться.", 13f, muted)
        button("Прошивки и джейлбрейк") { navigate("workshop") }
        button("Википедия телефонов") { navigate("wiki") }
        button("На главную") { navigate("home") }
    }

    private fun admin() {
        screen("Админ-панель")
        if (!adminUnlocked) {
            label("Вход по паролю", 22f)
            val input = searchField("Пароль администратора") { }
            input.id = R.id.admin_password
            input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            input.isSaveEnabled = false
            val error = label("", 15f, muted).apply { accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE }
            button("Войти") {
                if (AdminAccess.accepts(input.text.toString())) {
                    adminUnlocked = true
                    (getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager)
                        .hideSoftInputFromWindow(input.windowToken, 0)
                    input.text.clear()
                    render()
                } else {
                    error.text = "Неверный пароль"
                    input.text.clear()
                }
            }
        } else {
            label("Выбери любой телефон — он появится в коллекции бесплатно.", 17f, muted)
            button("Заблокировать панель") { adminUnlocked = false; render() }
            label("Выдать деньги", 24f)
            val amount = searchField("Сумма от 1 до 2147483647") { }
            amount.inputType = InputType.TYPE_CLASS_NUMBER
            amount.id = R.id.admin_money
            amount.filters = arrayOf(android.text.InputFilter.LengthFilter(10))
            val moneyError = label("Максимальный баланс: 2 147 483 647 ₽", 14f, muted)
            button("Выдать сумму") {
                val value = amount.text.toString().toLongOrNull()
                if (!adminUnlocked) return@button
                if (value == null || value !in 1L..Int.MAX_VALUE.toLong()) moneyError.text = "Введите сумму от 1 до 2147483647"
                else if (persist(progress.adminCredit(value))) render()
            }
            button("Максимум денег") {
                if (adminUnlocked && persist(progress.adminCredit(Int.MAX_VALUE.toLong()))) render()
            }
            lateinit var phones: LinearLayout
            fun refreshPhones(query: String) {
                phones.removeAllViews()
                val matches = Catalog.phones.indices.filter { Catalog.phones[it].name.contains(query.trim(), true) }
                if (matches.isEmpty()) label("Ничего не найдено", parent = phones)
                pagedPhones(matches, phones) { page -> page.forEach { index ->
                    val phone = Catalog.phones[index]
                    button("Взять ${phone.name}", phones) {
                        if (adminUnlocked && persist(progress.grant(index))) {
                            Toast.makeText(this, "${phone.name} добавлен в коллекцию", Toast.LENGTH_SHORT).show()
                            render()
                        }
                    }
                } }
            }
            searchField("Найти телефон для выдачи") { refreshPhones(it) }
            phones = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; root.addView(this) }
            refreshPhones("")
        }
        button("На главную") { navigate("home") }
    }

    private fun workshop() {
        screen("Прошивки и джейлбрейк")
        button("Клиенты и сервис") { navigate("clients") }
        label("Игровая симуляция: действия не прошивают настоящий телефон. Доступность реального кастома и джейлбрейка зависит от модели, загрузчика и версии ОС.", 15f, muted)
        label("Профиль применяется к вашим экземплярам выбранной модели.", 14f, muted)
        button("На главную") { navigate("home") }
        val models = (progress.owned + progress.used.map { it.phone }).distinct()
        if (models.isEmpty()) label("Сначала получи телефон в коллекцию.")
        models.forEach { index ->
            val phone = Catalog.phones[index]
            val panel = glassPanel()
            label(phone.name, 22f, parent = panel)
            infoRow("Установлено", progress.firmware[index] ?: "Заводская", panel)
            val options = Workshop.options(phone)
            if (options.size == 1) label("Для этой платформы доступна только заводская система.", 15f, muted, panel)
            options.forEach { rom ->
                button("Установить $rom · ${phone.name}", panel) {
                    if (persist(progress.tune(index, rom))) render()
                }
            }
        }
    }

    private fun cases() {
        screen("Кейсы 2.5")
        label("В каждом кейсе Z Fold 8 Ultra — 0,1%. Остальные модели зависят от типа кейса.", 16f, muted)
        CaseKind.entries.forEach { kind ->
            val panel = glassPanel()
            label(kind.title, 24f, parent = panel)
            label("От редкости: ${kind.minimum.title} · ${kind.cost} ₽", 16f, muted, panel)
            val pool = if (kind == CaseKind.CLASSIC) Catalog.phones.indices.toList()
                else Catalog.phones.indices.filter { Catalog.phones[it].rarity.ordinal >= kind.minimum.ordinal }
            label("Моделей: ${pool.size}. Шансы:", 14f, muted, panel)
            val chances = (0..999).map(kind::roll).groupingBy { it }.eachCount()
            Rarity.entries.forEach { rarity ->
                val total = chances.filterKeys { Catalog.phones[it].rarity == rarity }.values.sum()
                if (total > 0) label("${rarity.title}: ${total / 10.0}%", 14f, rarity.accent, panel)
            }
            button("Открыть ${kind.title} · ${kind.cost}", panel) { openCase(kind) }
        }
        button("На главную") { navigate("home") }
    }

    private fun market() {
        screen("Авито · рынок б/у")
        label("Внутриигровая площадка, не связана с сервисом Avito. Только игровые ₽ы.", 14f, muted)
        button("На главную") { navigate("home") }
        label("Объявления дня", 24f)
        val day = System.currentTimeMillis() / 86_400_000L
        Market.listings(day).forEach { listing ->
            val panel = glassPanel()
            label(Catalog.phones[listing.phone].name, 21f, parent = panel)
            infoRow("Состояние", Market.conditions[listing.condition], panel)
            infoRow("Цена", "${listing.price} ₽", panel)
            if (listing.id in progress.purchased) label("Уже куплено", 16f, muted, panel)
            else {
                if(listing.id !in progress.expansion.negotiated) button("Предложить скидку 15%", panel) {
                    if(persist(progress.bargain(listing,Random.nextInt(100)),"Торг")) render()
                }
                if(listing.id in progress.expansion.negotiated && listing.id !in progress.expansion.discounts) label("Продавец отказался от скидки",15f,muted,panel)
                if(listing.id in progress.expansion.discounts) label("Цена после торга: ${progress.askPrice(listing)} ₽",16f,parent=panel)
                button("Купить · ${progress.askPrice(listing)}", panel) {
                val next = progress.buy(listing.copy(price=progress.askPrice(listing)))
                if (next == null) Toast.makeText(this, "Недостаточно ₽ или лот уже куплен", Toast.LENGTH_SHORT).show()
                else if (persist(next,"Покупка б/у телефона")) render()
                }
            }
        }
        label("Продать б/у телефон", 24f)
        if (progress.used.isEmpty()) label("Купленные б/у телефоны появятся здесь.", 16f, muted)
        progress.used.forEachIndexed { index, item ->
            val panel = glassPanel()
            label(Catalog.phones[item.phone].name, 21f, parent = panel)
            infoRow("Состояние", Market.conditions[item.condition], panel)
            if(item.condition<3) button("Ремонт до следующего состояния · ${progress.repairPrice(index)} ₽",panel) {
                if(index in progress.used.indices && progress.used[index]==item) {
                    val next=progress.repairUsed(index)
                    if(next==progress) Toast.makeText(this,"Недостаточно денег на ремонт",Toast.LENGTH_SHORT).show()
                    else if(persist(next,"Ремонт б/у")) render()
                }
            }
            button("Продать б/у · ${Market.price(item.phone, item.condition)}", panel) {
                if (persist(progress.sellUsed(index))) render()
            }
        }
        label("Продать телефон из кейса", 24f)
        progress.owned.distinct().forEach { index ->
            button("Продать ${Catalog.phones[index].name} · ${Catalog.phones[index].price}") {
                if (persist(progress.sell(index))) render()
            }
        }
    }

    private fun openCase(kind: CaseKind = CaseKind.CLASSIC) {
        if (animator != null) return
        val result = progress.openCase(kind, Random.nextInt(1000))
        if (result == null) {
            Toast.makeText(this, "Недостаточно ₽ — загляни в магазин", Toast.LENGTH_SHORT).show()
            return
        }
        if (!persist(result.first)) return
        reward = result.second
        page = "result"
        screen("Открываем кейс…")
        val crate = label("▣", 100f, mint).apply {
            gravity = Gravity.CENTER
            setShadowLayer(dp(24).toFloat(), 0f, 0f, mint)
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        val ticker = label("Загрузка неонового кейса", 22f).apply {
            gravity = Gravity.CENTER
            accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
        }
        button("Показать телефон") { animator?.end() }
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1800
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                val fraction = it.animatedValue as Float
                crate.rotation = kotlin.math.sin(fraction * 40f) * (1f - fraction) * 12f
                crate.scaleX = 1f + fraction * 0.15f
                crate.scaleY = crate.scaleX
                ticker.text = listOf("Сканируем кейс…", "Ловим неоновый сигнал…", "Телефон найден!")[(fraction * 2.99f).toInt()]
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    animator = null
                    if (!isDestroyed && !isFinishing) render()
                }
            })
        }
        animator?.start()
    }
}
