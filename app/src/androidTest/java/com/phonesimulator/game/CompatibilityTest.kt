package com.phonesimulator.game

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Each matrix entry installs the APK and runs these flows on that emulator OS. */
@RunWith(AndroidJUnit4::class)
class CompatibilityTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    @Before fun resetProgress() {
        context.getSharedPreferences("game", 0).edit().clear().commit()
    }
    private fun views(view: View): List<View> = listOf(view) +
        if (view is ViewGroup) (0 until view.childCount).flatMap { views(view.getChildAt(it)) } else emptyList()
    private fun click(scenario: ActivityScenario<MainActivity>, text: String) {
        scenario.onActivity { activity ->
            val button = views(activity.window.decorView).filterIsInstance<Button>()
                .first { it.text.toString() == text }
            assertTrue(button.performClick())
        }
    }
    private fun assertText(scenario: ActivityScenario<MainActivity>, text: String) {
        scenario.onActivity { activity ->
            assertTrue(views(activity.window.decorView).filterIsInstance<TextView>()
                .any { it.text.toString() == text })
        }
    }
    @Test fun openSaveRecreateAndRelaunch() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            click(scenario, "Открыть кейс · 100 монет")
            click(scenario, "Показать телефон")
            assertText(scenario, "Новый телефон!")
            assertEquals(900, ProgressStore(context).load().coins)
            assertEquals(1, ProgressStore(context).load().owned.size)
            scenario.recreate()
            assertText(scenario, "Новый телефон!")
            assertEquals(900, ProgressStore(context).load().coins)
        }
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            assertEquals(1, ProgressStore(context).load().owned.size)
            click(scenario, "Коллекция")
            assertText(scenario, "Коллекция")
        }
    }
    @Test fun shopAndEmptyCollection() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            click(scenario, "Магазин")
            click(scenario, "Получить 500 монет")
            assertEquals(1500, ProgressStore(context).load().coins)
            click(scenario, "На главную")
            click(scenario, "Коллекция")
            assertText(scenario, "Первый телефон ждёт в кейсе.")
        }
    }
    @Test fun legacySaveAndDuplicateSale() {
        context.getSharedPreferences("game", 0).edit().putInt("coins", 777)
            .putString("owned", "0,5,5,99,broken").commit()
        assertEquals(Progress(777, listOf(0, 5, 5)), ProgressStore(context).load())
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            click(scenario, "Коллекция")
            click(scenario, "Продать один · 25000 монет")
            assertEquals(Progress(25777, listOf(0, 5)), ProgressStore(context).load())
            scenario.recreate()
            assertText(scenario, "Коллекция")
        }
    }
}
