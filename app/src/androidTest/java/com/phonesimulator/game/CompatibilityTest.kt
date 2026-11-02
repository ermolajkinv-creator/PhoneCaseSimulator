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
    @Test fun animationCompletesAndBackgroundKeepsReward() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            click(scenario, "Открыть кейс · 100 монет")
            val deadline = android.os.SystemClock.uptimeMillis() + 6000
            var revealed = false
            while (!revealed && android.os.SystemClock.uptimeMillis() < deadline) {
                scenario.onActivity { activity ->
                    revealed = views(activity.window.decorView).filterIsInstance<TextView>()
                        .any { it.text.toString() == "Новый телефон!" }
                }
                if (!revealed) android.os.SystemClock.sleep(100)
            }
            assertTrue("Animation did not reveal the saved phone", revealed)
            click(scenario, "Ещё кейс · 100 монет")
            scenario.moveToState(androidx.lifecycle.Lifecycle.State.CREATED)
            scenario.moveToState(androidx.lifecycle.Lifecycle.State.RESUMED)
            assertText(scenario, "Новый телефон!")
            assertEquals(ProgressStore(context).load().coins, 800)
            assertEquals(ProgressStore(context).load().owned.size, 2)
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
    @Test fun adminRequiresPasswordGrantsAndLocks() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            click(scenario, "Админ-панель")
            click(scenario, "Войти")
            assertText(scenario, "Неверный пароль")
            assertEquals(Progress(), ProgressStore(context).load())
            scenario.onActivity { activity ->
                activity.findViewById<android.widget.EditText>(1001).setText("Prism-8426")
            }
            click(scenario, "Войти")
            click(scenario, "Взять Samsung Z Fold 8 Ultra")
            click(scenario, "Взять Sony Xperia 1 VI")
            assertEquals(Progress(1000, listOf(5, 23)), ProgressStore(context).load())
            scenario.moveToState(androidx.lifecycle.Lifecycle.State.CREATED)
            scenario.moveToState(androidx.lifecycle.Lifecycle.State.RESUMED)
            assertText(scenario, "Вход по паролю")
            scenario.onActivity { activity ->
                assertFalse(views(activity.window.decorView).filterIsInstance<Button>()
                    .any { it.text.toString().startsWith("Взять ") })
            }
            scenario.recreate()
            assertText(scenario, "Вход по паролю")
        }
    }
    @Test fun wikiSearchAndDetails() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            click(scenario, "Википедия телефонов · 24 моделей")
            scenario.onActivity { activity ->
                views(activity.window.decorView).filterIsInstance<android.widget.EditText>()
                    .first().setText("Pixel 4")
            }
            click(scenario, "Google Pixel 4\nРедкий · 2.5%")
            assertText(scenario, "Snapdragon 855")
            assertText(scenario, "Android 10 при выпуске")
            scenario.recreate()
            assertText(scenario, "Snapdragon 855")
            click(scenario, "Википедия телефонов")
            scenario.onActivity { activity ->
                val input = views(activity.window.decorView).filterIsInstance<android.widget.EditText>().first()
                assertEquals("Pixel 4", input.text.toString())
                input.setText("no-such-phone")
            }
            assertText(scenario, "Ничего не найдено")
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
