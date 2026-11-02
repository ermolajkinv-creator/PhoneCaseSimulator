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
    private val ink = Color.rgb(33, 38, 91)
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
        render()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("page", page)
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
        intArrayOf(Color.argb(46, Color.red(color), Color.green(color), Color.blue(color)), 0x16FFFFFF)
    ).apply {
        cornerRadius = dp(30).toFloat()
        setStroke(dp(1), Color.argb(80, Color.red(accent), Color.green(accent), Color.blue(accent)))
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
                intArrayOf(0xFF173F85.toInt(), 0xFF54428B.toInt(), 0xFF9B4D82.toInt()))
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
        scroll.addView(container)
        scroll.setOnApplyWindowInsetsListener { view, insets ->
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
        setContentView(scroll)
        scroll.requestApplyInsets()
        label("P H O N E   L A B", 14f, mint).apply {
            letterSpacing = 0.14f
        }
        label(title, 30f).setTypeface(null, Typeface.BOLD)
        label("◈ ${progress.coins} монет  ·  ${progress.owned.size} телефонов", 16f, muted)
    }

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
        label(phone.name, 23f, parent = panel).setTypeface(null, Typeface.BOLD)
        label("Шанс ${phone.tickets / 10.0}%" + (count?.let { "  ·  В коллекции: $it" } ?: ""),
            14f, muted, panel)
        button("О телефоне", panel) { selectedPhone = index; navigate("details") }
        if (count != null) button("Продать один · ${phone.price} монет", panel) {
            if (persist(progress.sell(index))) render()
        }
    }

    private fun persist(next: Progress): Boolean {
        if (!store.save(next)) {
            Toast.makeText(this, "Не удалось сохранить прогресс. Повторите попытку.", Toast.LENGTH_LONG).show()
            return false
        }
        progress = next
        return true
    }

    private fun navigate(destination: String) { page = destination; render() }
    private fun render() {
        when (page) {
            "wiki" -> wiki()
            "details" -> details()
            "admin" -> admin()
            "collection" -> {
                screen("Коллекция")
                if (progress.owned.isEmpty()) label("Первый телефон ждёт в кейсе.", color = muted)
                progress.owned.groupingBy { it }.eachCount().toSortedMap().forEach { (index, count) ->
                    card(index, count)
                }
                button("На главную") { navigate("home") }
            }
            "shop" -> {
                screen("Магазин")
                label("Пополняй запас и продолжай коллекцию", 22f)
                label("Бесплатные 500 монет. Без рекламы и покупок за реальные деньги.", color = muted)
                button("Получить 500 монет") { if (persist(progress.bonus())) render() }
                button("На главную") { navigate("home") }
            }
            "result" -> {
                if (reward !in Catalog.phones.indices) { navigate("home"); return }
                screen("Новый телефон!")
                card(reward)
                label("Телефон уже сохранён в коллекции.", 15f, muted)
                button("Ещё кейс · 100 монет") { openCase() }
                button("Коллекция") { navigate("collection") }
                button("На главную") { navigate("home") }
            }
            else -> {
                screen("Открой свой следующий")
                label("Каждый кейс — новый шанс", 22f)
                card(5)
                label("Samsung Z Fold 8 Ultra\nУльтралегендарный · 1 из 1000", 17f, muted)
                button("Открыть кейс · 100 монет") { openCase() }
                button("Коллекция") { navigate("collection") }
                button("Магазин") { navigate("shop") }
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
            matches.forEach { index ->
                val phone = Catalog.phones[index]
                button("${phone.name}\n${phone.rarity.title} · ${phone.tickets / 10.0}%", list) {
                    selectedPhone = index; navigate("details")
                }
            }
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
        infoRow("Шанс в кейсе", "${phone.tickets / 10.0}%", panel)
        infoRow("Чипсет", phone.chip, panel)
        infoRow("Дисплей", phone.display, panel)
        infoRow("Система", phone.system, panel)
        infoRow("Цена продажи", "${phone.price} монет", panel)
        infoRow("В коллекции", progress.owned.count { it == selectedPhone }.toString(), panel)
        label(phone.description, 17f, parent = panel)
        label("Справочник офлайн. Данные относятся к указанной версии модели и системе при выпуске; региональные варианты могут отличаться.", 13f, muted)
        button("Википедия телефонов") { navigate("wiki") }
        button("На главную") { navigate("home") }
    }

    private fun admin() {
        screen("Админ-панель")
        if (!adminUnlocked) {
            label("Вход по паролю", 22f)
            val input = searchField("Пароль администратора") { }
            input.id = 1001
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
            Catalog.phones.forEachIndexed { index, phone ->
                button("Взять ${phone.name}") {
                    if (adminUnlocked && persist(progress.grant(index))) {
                        Toast.makeText(this, "${phone.name} добавлен в коллекцию", Toast.LENGTH_SHORT).show()
                        render()
                    }
                }
            }
        }
        button("На главную") { navigate("home") }
    }

    private fun openCase() {
        if (animator != null) return
        val result = progress.open(Random.nextInt(1000))
        if (result == null) {
            Toast.makeText(this, "Недостаточно монет — загляни в магазин", Toast.LENGTH_SHORT).show()
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
