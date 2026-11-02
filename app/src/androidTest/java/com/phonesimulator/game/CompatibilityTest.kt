package com.phonesimulator.game

import android.test.ActivityInstrumentationTestCase2
import android.widget.Button
import android.widget.TextView
import android.view.View
import android.view.ViewGroup

/** Platform-only tests run on the actual emulator OS, without an AndroidX runtime. */
@Suppress("DEPRECATION")
class CompatibilityTest : ActivityInstrumentationTestCase2<MainActivity>(MainActivity::class.java) {
    override fun setUp() {
        super.setUp()
        instrumentation.targetContext.getSharedPreferences("game", 0).edit().clear().commit()
    }
    private fun views(view: View): List<View> = listOf(view) +
        if (view is ViewGroup) (0 until view.childCount).flatMap { views(view.getChildAt(it)) } else emptyList()
    private fun click(text: String) {
        instrumentation.runOnMainSync {
            val button = views(activity.window.decorView).filterIsInstance<Button>().first { it.text.toString() == text }
            assertTrue(button.performClick())
        }
        instrumentation.waitForIdleSync()
    }
    fun testLaunchOpenAndSaveAcrossRecreation() {
        val first = activity
        assertNotNull(first)
        click("Открыть кейс · 100 монет")
        click("Показать телефон")
        val store = ProgressStore(instrumentation.targetContext)
        assertEquals(900, store.load().coins)
        assertEquals(1, store.load().owned.size)
        instrumentation.runOnMainSync { first.recreate() }
        instrumentation.waitForIdleSync()
        assertEquals(900, store.load().coins)
        assertEquals(1, store.load().owned.size)
    }
    fun testShopAndCollection() {
        activity
        click("Магазин")
        click("Получить 500 монет")
        assertEquals(1500, ProgressStore(instrumentation.targetContext).load().coins)
        click("На главную")
        click("Коллекция")
        instrumentation.runOnMainSync {
            assertTrue(views(activity.window.decorView).filterIsInstance<TextView>()
                .any { it.text.toString() == "Первый телефон ждёт в кейсе." })
        }
    }
    fun testLegacyPreferencesSurviveStoreRecreation() {
        val context = instrumentation.targetContext
        context.getSharedPreferences("game", 0).edit().putInt("coins", 777)
            .putString("owned", "0,5,5,99,broken").commit()
        assertEquals(Progress(777, listOf(0, 5, 5)), ProgressStore(context).load())
        val next = ProgressStore(context).load().sell(5)
        assertTrue(ProgressStore(context).save(next))
        assertEquals(next, ProgressStore(context).load())
    }
}
