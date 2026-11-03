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
        context.getSharedPreferences("local_players",0).edit().clear().commit()
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
            assertEquals(Progress(), ProgressStore(context).load().copy(expansion = Expansion()))
            scenario.onActivity { activity ->
                activity.findViewById<android.widget.EditText>(R.id.admin_password).setText("Prism-8426")
            }
            click(scenario, "Войти")
            click(scenario, "Взять Samsung Z Fold 8 Ultra")
            click(scenario, "Взять Sony Xperia 1 VI")
            click(scenario, "Взять OnePlus 15")
            assertEquals(Progress(Economy.START_BALANCE, listOf(5, 23, 24), badges = setOf("first", "ultra")), ProgressStore(context).load().copy(expansion = Expansion()))
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
            click(scenario, "Википедия телефонов · ${Catalog.modelCountLabel}")
            scenario.onActivity { activity ->
                views(activity.window.decorView).filterIsInstance<android.widget.EditText>()
                    .first().setText("Pixel 4")
            }
            click(scenario, "Google Pixel 4\nРедкий · ${Catalog.percent(Catalog.phones[12].tickets)}%")
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
        assertEquals(bought, ProgressStore(context).load().copy(expansion = Expansion()))
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
            click(scenario, "Забрать награду за клики")
            scenario.recreate()
            assertEquals(5000, ProgressStore(context).load().coins)
            assertEquals(setOf("clicks"), ProgressStore(context).load().claimedQuests)
            assertEquals(0L, ProgressStore(context).load().lastGiftAt)
            click(scenario, "Инвентарь")
            click(scenario, "Прошивки и джейлбрейк")
            assertText(scenario, "Сначала получи телефон в коллекцию.")
        }
    }
    @Test fun clientJobSurvivesRestartAndPaysOnlyOnce() {
        val now = System.currentTimeMillis()
        val slot = now/Clients.INTERVAL
        val phone = 1
        val job = ClientJob(ClientOffer(slot, phone, ServiceKind.SETUP, 0, 1500, 20), now-1000)
        val start = Progress(100, listOf(5), clientJob = job, serviceSlot = slot)
        assertTrue(ProgressStore(context).save(start))
        assertEquals(job, ProgressStore(context).load().clientJob)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            click(scenario, "Клиенты и сервис")
            click(scenario, "Выдать телефон · 1500 ₽")
            val result = ProgressStore(context).load()
            assertEquals(1600, result.coins)
            assertEquals(listOf(5), result.owned)
            assertEquals(1, result.reputation)
            assertNull(result.clientJob)
            scenario.recreate()
            assertEquals(1600, ProgressStore(context).load().coins)
            assertEquals(1, ProgressStore(context).load().servicesCompleted)
            scenario.onActivity { activity ->
                assertFalse(views(activity.window.decorView).filterIsInstance<Button>()
                    .any { it.text.toString().startsWith("Выдать телефон") })
            }
        }
    }
    @Test fun versionThreeVaultAndLocalBackup() {
        assertTrue(ProgressStore(context).save(Progress(50000, listOf(0))))
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            click(scenario, "Новое в 3.0")
            scenario.onActivity { it.findViewById<android.widget.EditText>(R.id.vault_money).setText("1000") }
            click(scenario, "Положить в сейф")
            val stored=ProgressStore(context).load()
            assertEquals(49000,stored.coins)
            assertEquals(1000,stored.expansion.vault)
            assertEquals(0L,stored.expansion.income)
            assertEquals(0L,stored.expansion.expenses)
            scenario.recreate()
            assertEquals(1000,ProgressStore(context).load().expansion.vault)
        }
        val store=ProgressStore(context)
        assertTrue(store.createBackup())
        val baseline=store.load()
        val changed=baseline.copy(coins=0,lastGiftAt=System.currentTimeMillis())
        assertTrue(store.save(changed))
        assertTrue(store.restoreBackup())
        assertEquals(49000,store.load().coins)
        assertEquals(1000,store.load().expansion.vault)
        assertEquals(changed.lastGiftAt,store.load().lastGiftAt)
        val raw=Expansion(vault=123,income=456,negotiated=setOf("a"),discounts=mapOf("a" to 50),
            awardsClaimed=setOf("first"),ledger=listOf(CashEvent(100,99,"Продажа")),idleAt=500)
        assertEquals(raw,ExpansionCodec.decode(ExpansionCodec.encode(raw)))
    }
    @Test fun expandedCatalogSearchPurchaseAndPersistence() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            click(scenario, "Каталог телефонов")
            assertText(scenario, "Показать ещё · 32 из ${Catalog.phones.size}")
            scenario.onActivity { activity ->
                assertEquals(32, views(activity.window.decorView).filterIsInstance<Button>()
                    .count { it.text.toString() == "О телефоне" })
            }
            click(scenario, "Показать ещё · 32 из ${Catalog.phones.size}")
            assertText(scenario, "Показать ещё · 64 из ${Catalog.phones.size}")
            scenario.onActivity { activity ->
                views(activity.window.decorView).filterIsInstance<android.widget.EditText>().first().setText("ZTE Axon 40 Ultra")
            }
            click(scenario, "Купить новый · ${ExpansionRules.newPrice(499)} ₽")
            assertEquals(listOf(499), ProgressStore(context).load().owned)
            scenario.recreate()
            assertEquals(listOf(499), ProgressStore(context).load().owned)
            click(scenario, "На главную")
            click(scenario, "Википедия телефонов · ${Catalog.modelCountLabel}")
            scenario.onActivity { activity ->
                views(activity.window.decorView).filterIsInstance<android.widget.EditText>().first().setText("ZTE Axon 40 Ultra")
            }
            val phone = Catalog.phones[499]
            click(scenario, "${phone.name}\n${phone.rarity.title} · ${Catalog.percent(phone.tickets)}%")
            assertText(scenario, "Snapdragon 8 Gen 1")
            assertText(scenario, "Android 12 при выпуске")
        }
    }

    @Test fun clickerPausePersistsAndCountdownReenablesButton() {
        val store = ProgressStore(context)
        assertTrue(store.save(Progress(777)))
        assertTrue(store.createBackup())
        val now = System.currentTimeMillis()
        var fast = store.load()
        repeat(12) { fast = fast.clickCoin(now / Progression.DAY_MS, now) }
        assertTrue(store.save(fast))
        assertEquals(fast.clickGuard, ProgressStore(context).load().clickGuard)
        fast = ProgressStore(context).load().clickCoin(now / Progression.DAY_MS, now)
        assertTrue(store.save(fast))
        val deadline = fast.clickGuard.blockedUntil
        assertEquals(now + 30_000L, deadline)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            click(scenario, "Кликер")
            scenario.onActivity { activity ->
                val coin = views(activity.window.decorView).filterIsInstance<Button>().first { it.text.toString() == "◉ +10 ₽" }
                assertFalse(coin.isEnabled)
                coin.performClick() // Even a programmatic callback must not award cash while blocked.
                assertTrue(views(activity.window.decorView).filterIsInstance<TextView>()
                    .any { it.text.toString().startsWith("Слишком частые нажатия. Пауза:") })
            }
            assertEquals(fast.coins, store.load().coins)
            assertEquals(12, store.load().clicks)
            scenario.recreate()
            assertEquals(deadline, store.load().clickGuard.blockedUntil)
            click(scenario, "На главную")
            click(scenario, "Кликер")
            scenario.onActivity { activity ->
                assertFalse(views(activity.window.decorView).filterIsInstance<Button>().first { it.text.toString() == "◉ +10 ₽" }.isEnabled)
            }
        }
        assertTrue(store.restoreBackup())
        assertEquals(deadline, store.load().clickGuard.blockedUntil)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            click(scenario, "Кликер")
            scenario.onActivity { activity ->
                assertFalse(views(activity.window.decorView).filterIsInstance<Button>().first { it.text.toString() == "◉ +10 ₽" }.isEnabled)
            }
        }
        // Seed a short remaining duration; domain tests cover the exact 30-second deadline.
        assertTrue(store.save(store.load().copy(clickGuard = ClickGuard(blockedUntil = System.currentTimeMillis() + 1200L))))
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            click(scenario, "Кликер")
            var enabled = false
            val timeout = android.os.SystemClock.uptimeMillis() + 5000L
            while (!enabled && android.os.SystemClock.uptimeMillis() < timeout) {
                scenario.onActivity { activity ->
                    enabled = views(activity.window.decorView).filterIsInstance<Button>().first { it.text.toString() == "◉ +10 ₽" }.isEnabled
                }
                if (!enabled) android.os.SystemClock.sleep(100L)
            }
            assertTrue("Clicker should resume automatically", enabled)
            val before = store.load().coins
            click(scenario, "◉ +10 ₽")
            assertEquals(before + 10, store.load().coins)
        }
    }

    @Test fun bothUltrasAreFeaturedAndHomeIsAlwaysAccessible() {
        val store = ProgressStore(context)
        assertTrue(store.save(Progress(777, listOf(5, 500))))
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            assertText(scenario, "Samsung Z Fold 8 Ultra")
            assertText(scenario, "Палас улыбашкэ")
            assertText(scenario, "Шанс 0.1%")
            assertText(scenario, "Шанс 0.15%")
            click(scenario, "Каталог")
            click(scenario, "Главное")
            assertText(scenario, "Палас улыбашкэ")
            assertText(scenario, "Ежедневные задания")
            assertText(scenario, "Клиенты и сервис")
            click(scenario, "Википедия телефонов · ${Catalog.modelCountLabel}")
            scenario.onActivity { activity ->
                views(activity.window.decorView).filterIsInstance<android.widget.EditText>().first().setText("Палас улыбашкэ")
            }
            click(scenario, "Палас улыбашкэ\nУльтралегендарный · 0.15%")
            assertText(scenario, "Игровая платформа")
            scenario.recreate()
            assertText(scenario, "Игровая платформа")
            click(scenario, "Главное")
            assertText(scenario, "Палас улыбашкэ")
            assertEquals(listOf(5, 500), store.load().owned)
        }
        context.assets.open("palas-smile.jpg").use {
            val image = requireNotNull(android.graphics.BitmapFactory.decodeStream(it))
            assertTrue(image.width > 0 && image.height > 0)
            image.recycle()
        }
    }

    @Test fun repairPracticeSurvivesRotationWithoutPaying() {
        assertTrue(ProgressStore(context).save(Progress(777)))
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            click(scenario, "Мини-игра ремонта")
            assertText(scenario, "Тренировка · Redmi Note 10S")
            val first = RepairGame.question(ServiceKind.REPAIR, 0, 0)
            click(scenario, first.choices[first.answer])
            scenario.recreate()
            assertText(scenario, "Проверок пройдено: 1/3 · Ошибки: 0/3")
            for (step in 1..2) {
                val q = RepairGame.question(ServiceKind.REPAIR, step, 0)
                click(scenario, q.choices[q.answer])
            }
            assertText(scenario, "Ремонт выполнен · ★★★")
            assertEquals(777, ProgressStore(context).load().coins)
            assertNull(ProgressStore(context).load().clientJob)
            click(scenario, "Новая тренировка")
            assertText(scenario, "Проверок пройдено: 0/3 · Ошибки: 0/3")
        }
    }

    @Test fun clientRepairBonusPersistsAndCannotBeReissued() {
        val store = ProgressStore(context)
        val now = System.currentTimeMillis()
        val slot = now / Clients.INTERVAL
        val offer = ClientOffer(slot, 1, ServiceKind.SETUP, 0, 1500, 20)
        // The previous seven-field job format still loads with an untouched repair round.
        context.getSharedPreferences("game", 0).edit().putString("clientJob", "$slot,1,SETUP,0,1500,20,${now-1000}").commit()
        assertEquals(RepairSession(), store.load().clientJob!!.repair)
        assertTrue(store.save(Progress(700, listOf(5), clientJob = ClientJob(offer, now-1000), serviceSlot = slot)))
        assertTrue(store.createBackup())
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            click(scenario, "Мини-игра ремонта")
            val q = RepairGame.question(offer.kind, 0, slot)
            click(scenario, q.choices[q.answer])
            scenario.recreate()
            assertEquals(RepairSession(1,0), store.load().clientJob!!.repair)
        }
        assertTrue(store.restoreBackup())
        assertEquals(RepairSession(1,0), store.load().clientJob!!.repair)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            click(scenario, "Мини-игра ремонта")
            for (step in 1..2) {
                val q = RepairGame.question(offer.kind, step, slot)
                click(scenario, q.choices[q.answer])
            }
            assertEquals(700, store.load().coins)
            assertEquals(500, store.load().clientJob!!.repair.bonus)
            click(scenario, "Клиенты и сервис")
            click(scenario, "Выдать телефон · 2000 ₽")
            assertEquals(2700, store.load().coins)
            assertEquals(1, store.load().servicesCompleted)
            assertNull(store.load().clientJob)
            scenario.recreate()
            scenario.onActivity { activity ->
                assertFalse(views(activity.window.decorView).filterIsInstance<Button>()
                    .any { it.text.toString().startsWith("Выдать телефон") })
            }
        }
        assertTrue(store.restoreBackup())
        assertNull(store.load().clientJob)
        val restored = store.load()
        assertEquals(restored.coins, restored.finishClient(System.currentTimeMillis()).coins)
    }

    @Test fun legacySaveAndDuplicateSale() {
        context.getSharedPreferences("game", 0).edit().putInt("coins", 777)
            .putString("owned", "0,5,5,999,broken").commit()
        assertEquals(Progress(777, listOf(0, 5, 5)), ProgressStore(context).load().copy(expansion = Expansion()))
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            click(scenario, "Коллекция")
            click(scenario, "Продать один · ${Catalog.phones[5].price} ₽")
            assertEquals(Progress(777 + Catalog.phones[5].price, listOf(0, 5), badges = setOf("first", "ultra")), ProgressStore(context).load().copy(expansion = Expansion()))
            scenario.recreate()
            assertText(scenario, "Коллекция")
        }
    }
}
