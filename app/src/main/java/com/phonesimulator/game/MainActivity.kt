package com.phonesimulator.game

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
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
    private val ink = Color.rgb(9, 13, 25)
    private val muted = Color.rgb(160, 174, 200)
    private val mint = Color.rgb(89, 243, 220)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = ProgressStore(this)
        progress = store.load()
        page = savedInstanceState?.getString("page") ?: "home"
        reward = savedInstanceState?.getInt("reward", -1) ?: -1
        render()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("page", page)
        outState.putInt("reward", reward)
        super.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        // The reward is already saved; backgrounding never loses a paid case.
        animator?.cancel()
        animator = null
        if (page == "result") render()
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun shape(color: Int, accent: Int = color) = GradientDrawable(
        GradientDrawable.Orientation.TL_BR, intArrayOf(color, ink)
    ).apply { cornerRadius = dp(22).toFloat(); setStroke(dp(1), accent) }

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
            setTextColor(mint)
            background = shape(Color.rgb(24, 45, 56), mint)
            minHeight = dp(56)
            setPadding(dp(16), dp(12), dp(16), dp(12))
            parent.addView(this, LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(10); bottomMargin = dp(6)
            })
            setOnClickListener { action() }
        }

    private fun screen(title: String) {
        val scroll = ScrollView(this).apply { setBackgroundColor(ink); isFillViewport = true }
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
        label("PHONE LAB  /  CASE SIMULATOR", 12f, mint).apply {
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
            background = shape(Color.rgb(25, 31, 52), phone.rarity.accent)
            elevation = dp(6).toFloat()
            parent.addView(this, LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(16); bottomMargin = dp(12)
            })
        }
        panel.addView(PhoneArtView(this, phone), LinearLayout.LayoutParams(-1, dp(156)))
        label(phone.rarity.title.uppercase(), 12f, phone.rarity.accent, panel)
        label(phone.name, 23f, parent = panel).setTypeface(null, Typeface.BOLD)
        label("Шанс ${phone.tickets / 10.0}%" + (count?.let { "  ·  В коллекции: $it" } ?: ""),
            14f, muted, panel)
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
                label("Шансы выпадения", 21f)
                Catalog.phones.forEach { label("${it.rarity.title} · ${it.tickets / 10.0}%", 14f, it.rarity.accent) }
            }
        }
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
