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
            click(scenario, "Открыть кейс · ${CaseKind.CLASSIC.cost} ₽")
            scenario.onActivity { activity ->
                // A busy emulator may finish the animation before this callback runs.
                // Both the skip control and an already revealed reward are valid states.
                val all = views(activity.window.decorView)
                val reveal = all.filterIsInstance<Button>().firstOrNull { it.text.toString() == "Показать телефон" }
                if (reveal != null) assertTrue(reveal.performClick())
                else assertTrue("Expected the completed case reveal", all.filterIsInstance<TextView>()
                    .any { it.text.toString() == "Новый телефон!" })
            }
            assertText(scenario, "Новый телефон!")
            assertEquals(Economy.START_BALANCE - CaseKind.CLASSIC.cost, ProgressStore(context).load().coins)
            assertEquals(1, ProgressStore(context).load().owned.size)
            scenario.recreate()
            assertText(scenario, "Новый телефон!")
            assertEquals(Economy.START_BALANCE - CaseKind.CLASSIC.cost, ProgressStore(context).load().coins)
        }
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            assertEquals(1, ProgressStore(context).load().owned.size)
            click(scenario, "Коллекция")
            assertText(scenario, "Коллекция")
        }
    }
    @Test fun animationCompletesAndBackgroundKeepsReward() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            click(scenario, "Открыть кейс · ${CaseKind.CLASSIC.cost} ₽")
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
            click(scenario, "Ещё кейс · ${CaseKind.CLASSIC.cost} ₽")
            scenario.moveToState(androidx.lifecycle.Lifecycle.State.CREATED)
            scenario.moveToState(androidx.lifecycle.Lifecycle.State.RESUMED)
            assertText(scenario, "Новый телефон!")
            assertEquals(ProgressStore(context).load().coins, Economy.START_BALANCE - 2 * CaseKind.CLASSIC.cost)
            assertEquals(ProgressStore(context).load().owned.size, 2)
        }
    }
    @Test fun shopAndEmptyCollection() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            click(scenario, "Магазин")
            click(scenario, "Получить ${Economy.BONUS} ₽")
            assertEquals(Economy.START_BALANCE + Economy.BONUS, ProgressStore(context).load().coins)
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
                activity.findViewById<android.widget.EditText>(R.id.admin_password).setText("Prism-8426")
            }
            click(scenario, "Войти")
            click(scenario, "Взять Samsung Z Fold 8 Ultra")
            click(scenario, "Взять Sony Xperia 1 VI")
            click(scenario, "Взять OnePlus 15")
            assertEquals(Progress(Economy.START_BALANCE, listOf(5, 23, 24), badges = setOf("first", "ultra")), ProgressStore(context).load())
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
            click(scenario, "Википедия телефонов · 100 моделей")
            scenario.onActivity { activity ->
                views(activity.window.decorView).filterIsInstance<android.widget.EditText>()
                    .first().setText("Pixel 4")
            }
            click(scenario, "Google Pixel 4\nРедкий · ${Catalog.phones[12].tickets / 10.0}%")
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
    @Test fun marketPersistenceAndNewCases() {
        val store = ProgressStore(context)
        val listing = Listing("saved-test", 42, 2, 100)
        val bought = Progress().buy(listing)!!
        assertTrue(store.save(bought))
        assertEquals(bought, ProgressStore(context).load())
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            click(scenario, "Авито · рынок б/у")
            assertText(scenario, "Хорошее")
            click(scenario, "Продать б/у · ${Market.price(42, 2)}")
            assertTrue(ProgressStore(context).load().used.isEmpty())
            click(scenario, "На главную")
            click(scenario, "Все кейсы · 4 вида")
            assertText(scenario, "Премиум")
        }
    }
    @Test fun workshopPersistsAcrossRecreation() {
        val store = ProgressStore(context)
        assertTrue(store.save(Progress(777, listOf(1, 4))))
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            click(scenario, "Мастерская · прошивки")
            click(scenario, "Установить LineageOS (симуляция) · Redmi Note 10S")
            click(scenario, "Установить Джейлбрейк (симуляция) · iPhone 17 Pro")
            scenario.recreate()
            assertText(scenario, "Джейлбрейк (симуляция)")
            assertEquals(777, ProgressStore(context).load().coins)
            assertEquals("LineageOS (симуляция)", ProgressStore(context).load().firmware[1])
            click(scenario, "Установить Заводская · iPhone 17 Pro")
            assertEquals("Заводская", ProgressStore(context).load().firmware[4])
        }
    }
    @Test fun povertyGiftPersistsAndDisappears() {
        val store = ProgressStore(context)
        assertTrue(store.save(Progress(0, listOf(5))))
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            click(scenario, "Получить подарок · ${Economy.BONUS} ₽")
            assertEquals(Economy.BONUS, store.load().coins)
            assertEquals(listOf(5), store.load().owned)
            scenario.recreate()
            assertEquals(Economy.BONUS, ProgressStore(context).load().coins)
            scenario.onActivity { activity ->
                assertFalse(views(activity.window.decorView).filterIsInstance<Button>()
                    .any { it.text.toString().startsWith("Получить подарок") })
            }
        }
    }
    @Test fun giftCooldownSurvivesRelaunchAndBlocksShopBonus() {
        val store = ProgressStore(context)
        assertTrue(store.save(Progress(0)))
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            click(scenario, "Получить подарок · ${Economy.BONUS} ₽")
        }
        val received = ProgressStore(context).load()
        assertTrue(received.lastGiftAt > 0L)
        // Simulate spending all money, then relaunch a fresh Activity/store.
        assertTrue(store.save(received.copy(coins = 0)))
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            assertTrue(ProgressStore(context).load().giftCooldownRemaining() > 0L)
            scenario.onActivity { activity ->
                assertFalse(views(activity.window.decorView).filterIsInstance<Button>()
                    .any { it.text.toString().startsWith("Получить подарок") })
            }
            click(scenario, "Магазин")
            click(scenario, "Получить ${Economy.BONUS} ₽")
            assertEquals(0, ProgressStore(context).load().coins)
            scenario.recreate()
            assertEquals(received.lastGiftAt, ProgressStore(context).load().lastGiftAt)
        }
    }
    @Test fun dailyClickerProfileAndFavoritesPersist() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            click(scenario, "Ежедневная награда")
            click(scenario, "Забрать награду · ${Economy.BONUS} ₽")
            assertText(scenario, "Сегодня награда уже получена")
            click(scenario, "Кликер")
            click(scenario, "◉ +10 ₽")
            assertEquals(Economy.START_BALANCE + Economy.BONUS + 10, ProgressStore(context).load().coins)
            click(scenario, "Каталог")
            scenario.onActivity { activity ->
                views(activity.window.decorView).filterIsInstance<android.widget.EditText>().first().setText("Nokia 3310")
            }
            click(scenario, "♡ В избранное")
            assertEquals(setOf(0), ProgressStore(context).load().favorites)
            click(scenario, "Профиль")
            scenario.onActivity { activity ->
                views(activity.window.decorView).filterIsInstance<android.widget.EditText>().first().setText("Игрок")
            }
            click(scenario, "Сохранить имя")
            scenario.recreate()
            assertText(scenario, "Игрок")
            assertEquals(1, ProgressStore(context).load().clicks)
            assertEquals(setOf(0), ProgressStore(context).load().favorites)
        }
    }
    @Test fun upgradeConsumesOnlyOnePhoneAndPreservesCash() {
        assertTrue(ProgressStore(context).save(Progress(777, listOf(0,0))))
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            click(scenario, "Апгрейд")
            click(scenario, "Выбрать Nokia 3310")
            click(scenario, "Попробовать улучшить")
            val loaded = ProgressStore(context).load()
            assertEquals(777, loaded.coins)
            assertTrue(loaded.owned.size in 1..2)
            assertEquals(1, loaded.owned.count { it == 0 })
            scenario.recreate()
            assertText(scenario, "Апгрейдер")
        }
    }
    @Test fun adminMoneyAndQuestRewardPersistence() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            click(scenario, "Админ-панель")
            scenario.onActivity { activity ->
                assertFalse(views(activity.window.decorView).filterIsInstance<Button>().any { it.text == "Максимум денег" })
                activity.findViewById<android.widget.EditText>(R.id.admin_password).setText("Prism-8426")
            }
            click(scenario, "Войти")
            scenario.onActivity { it.findViewById<android.widget.EditText>(R.id.admin_money).setText("12345") }
            click(scenario, "Выдать сумму")
            assertEquals(Economy.START_BALANCE+12345, ProgressStore(context).load().coins)
            click(scenario, "Максимум денег")
            assertEquals(Int.MAX_VALUE, ProgressStore(context).load().coins)
        }
        val day = System.currentTimeMillis()/Progression.DAY_MS
        assertTrue(ProgressStore(context).save(Progress(0, questDay = day, questClicks = 100)))
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            click(scenario, "Ежедневные задания")
            click(scenario, "Забрать задание: clicks")
            scenario.recreate()
            assertEquals(5000, ProgressStore(context).load().coins)
            assertEquals(setOf("clicks"), ProgressStore(context).load().claimedQuests)
            assertEquals(0L, ProgressStore(context).load().lastGiftAt)
            click(scenario, "Инвентарь")
            click(scenario, "Прошивки и джейлбрейк")
            assertText(scenario, "Сначала получи телефон в коллекцию.")
        }
    }
    @Test fun legacySaveAndDuplicateSale() {
        context.getSharedPreferences("game", 0).edit().putInt("coins", 777)
            .putString("owned", "0,5,5,999,broken").commit()
        assertEquals(Progress(777, listOf(0, 5, 5)), ProgressStore(context).load())
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            click(scenario, "Коллекция")
            click(scenario, "Продать один · ${Catalog.phones[5].price} ₽")
            assertEquals(Progress(777 + Catalog.phones[5].price, listOf(0, 5), badges = setOf("first", "ultra")), ProgressStore(context).load())
            scenario.recreate()
            assertText(scenario, "Коллекция")
        }
    }
}
