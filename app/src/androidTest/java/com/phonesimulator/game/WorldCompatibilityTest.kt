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

@RunWith(AndroidJUnit4::class)
class WorldCompatibilityTest {
    private val context get()=InstrumentationRegistry.getInstrumentation().targetContext
    @Before fun reset(){
        context.getSharedPreferences("local_players",0).edit().clear().commit()
        listOf("game","game_backup","game_p1","game_p2","game_p3").forEach { context.getSharedPreferences(it,0).edit().clear().commit() }
    }
    private fun views(v: View): List<View> = listOf(v)+if(v is ViewGroup)(0 until v.childCount).flatMap { views(v.getChildAt(it)) } else emptyList()
    private fun click(s: ActivityScenario<MainActivity>,text: String){s.onActivity { activity->
        assertTrue(views(activity.window.decorView).filterIsInstance<Button>().first { it.text.toString()==text }.performClick())
    }}
    private fun shown(s: ActivityScenario<MainActivity>,text: String){s.onActivity { activity->
        assertTrue("Missing: $text",views(activity.window.decorView).filterIsInstance<TextView>().any { it.text.toString()==text })
    }}
    private fun hub(s: ActivityScenario<MainActivity>){click(s,"Мир телефонов 4.0")}

    @Test fun virtualPhoneAppsStylesSnakeAnd3dPersist(){
        val start=Progress(200000,listOf(0,1)).identifyPhones();val key=start.devices()[0].key
        assertTrue(ProgressStore(context).save(start))
        ActivityScenario.launch(MainActivity::class.java).use { s->
            hub(s);click(s,"📱 Экземпляры и виртуальная ОС");click(s,"Открыть экран · Nokia 3310")
            shown(s,"NOKIA • МЕНЮ")
            click(s,"⚙ Настройки");click(s,"Магазин приложений")
            click(s,"Установить Змейка · 3000 ₽");click(s,"Установить Галерея · 2000 ₽")
            assertEquals(195000,ProgressStore(context).load().coins)
            click(s,"Экран телефона");click(s,"Змейка");click(s,"Начать / пауза")
            s.recreate();assertEquals(195000,ProgressStore(context).load().coins)
            click(s,"Экран телефона");click(s,"Галерея")
            click(s,"Корпус 2");click(s,"Обои 4");click(s,"Тема: Классический КПК")
            assertEquals(192000,ProgressStore(context).load().coins)
            click(s,"Корпус 2");assertEquals(192000,ProgressStore(context).load().coins)
            click(s,"Экран телефона");shown(s,"▦ Пуск · Phone Mobile")
            click(s,"3D-просмотр");click(s,"Повернуть на 90°");click(s,"Приблизить");click(s,"Сбросить камеру")
            s.recreate()
            s.onActivity { activity->assertTrue(views(activity.window.decorView).any { it.tag=="phone3d" }) }
        }
        val saved=ProgressStore(context).load().virtualProfile(key)
        assertEquals(setOf("snake","gallery"),saved.apps);assertEquals("pda",saved.theme)
        assertEquals(VirtualApps.colors[1],saved.color);assertEquals(3,saved.wallpaper)
    }
    @Test fun serializedMutationsRepairAndBackupDoNotDuplicateEscrow(){
        val now=System.currentTimeMillis()
        var state=Progress(1_000_000).appendPhone(1,PhoneTraits(Mutation.DIAMOND,1)).identifyPhones()
        state=state.updateVirtualProfile(state.devices()[0].key,VirtualProfile(apps=setOf("gallery"),firmware="LineageOS (симуляция)"))
        state=state.copy(used=listOf(UsedPhone(42,2,Mutation.GLITCH,500)),world=state.world.copy(serial=1000,appTokens=2))
        assertEquals(state.world,WorldCodec.decode(WorldCodec.encode(state.world)))
        val product=CompanyPhone(900,"QA One",2,VirtualApps.colors[0])
        val companyWorld=state.world.copy(company=Company("QA",2,produced=1,phones=listOf(product)))
        assertEquals(companyWorld,WorldCodec.decode(WorldCodec.encode(companyWorld)))
        val fixtureLot=AuctionLot(950,DeviceSnapshot(-1,PhoneTraits(uid=900),company=product),10000,now,now+120000,12000)
        val auctionWorld=state.world.copy(auctions=listOf(fixtureLot))
        assertEquals(auctionWorld,WorldCodec.decode(WorldCodec.encode(auctionWorld)))
        assertTrue(ProgressStore(context).save(state));assertEquals(state,ProgressStore(context).load())
        val fee=state.repairOwnedPrice(0);val key=state.devices()[0].key
        ActivityScenario.launch(MainActivity::class.java).use { s->
            hub(s);click(s,"📱 Экземпляры и виртуальная ОС");click(s,"Ремонт · $fee ₽")
            s.recreate();assertEquals(2,ProgressStore(context).load().traitsAt(0).condition)
            assertEquals(state.coins-fee,ProgressStore(context).load().coins)
        }
        val store=ProgressStore(context);assertTrue(store.createBackup())
        val listed=store.load().listAuction(key,80,now)
        assertTrue(store.save(listed));assertTrue(store.restoreBackup())
        assertNull(store.load().device(key));assertEquals(1,store.load().world.auctions.size)
        val lot=store.load().world.auctions.first();val paid=store.load().settleAuction(lot.id,lot.endsAt)
        assertTrue(store.save(paid));assertTrue(store.restoreBackup())
        assertEquals(paid.coins,store.load().coins);assertTrue(store.load().world.auctions.isEmpty())
        assertNull(store.load().device(key));assertEquals(store.load(),store.load().settleAuction(lot.id,lot.endsAt))
    }
    @Test fun batchOpeningSurvivesRecreation(){
        assertTrue(ProgressStore(context).save(Progress(2_000_000)))
        ActivityScenario.launch(MainActivity::class.java).use { s->
            click(s,"Все кейсы · 4 вида")
            click(s,"Открыть 3 × Классический · ${CaseKind.CLASSIC.cost*3L} ₽")
            s.onActivity { activity->
                views(activity.window.decorView).filterIsInstance<Button>().firstOrNull { it.text.toString()=="Показать результат" }?.performClick()
            }
            shown(s,"Результат открытия ×3")
            val saved=ProgressStore(context).load()
            assertEquals(2_000_000-CaseKind.CLASSIC.cost*3,saved.coins);assertEquals(3,saved.owned.size)
            assertEquals(3,saved.devices().map { it.key }.toSet().size)
            s.recreate();shown(s,"Результат открытия ×3")
            assertEquals(saved,ProgressStore(context).load())
        }
    }
    @Test fun localProfilesAndHotSeatDuelsKeepWalletsSeparate(){
        val first=Progress(50000,listOf(0),nickname="Первый").identifyPhones()
        assertTrue(ProgressStore(context).save(first))
        val book=LocalProfiles(context);val friend=requireNotNull(book.create("Друг"))
        assertTrue(ProgressStore(context,friend).save(Progress(62000,listOf(1),nickname="Друг").identifyPhones()))
        ActivityScenario.launch(MainActivity::class.java).use { s->
            hub(s);click(s,"👥 Игроки на этом телефоне");click(s,"Играть: Друг")
            assertEquals(friend,LocalProfiles.active(context));assertEquals(62000,ProgressStore(context).load().coins)
            hub(s);click(s,"👥 Игроки на этом телефоне");click(s,"Играть: Первый")
            assertEquals("main",LocalProfiles.active(context));assertEquals(50000,ProgressStore(context).load().coins)
            hub(s);click(s,"⚔ Битвы и дуэли с другом");click(s,"Дуэль с другом на одном телефоне")
            click(s,"Друг: Друг");click(s,"Первый игрок: Nokia 3310 · Стандарт");click(s,"Первый игрок: Разгон")
            shown(s,"Передай телефон: Друг");click(s,"Второй игрок готов")
            click(s,"Второй игрок: Redmi Note 10S · Стандарт");click(s,"Второй игрок: Энергосбережение")
            assertEquals(1,book.duels().size);s.recreate();assertEquals(1,book.duels().size)
        }
        assertEquals(50000,ProgressStore(context,"main").load().coins)
        assertEquals(62000,ProgressStore(context,friend).load().coins)
    }
    @Test fun minerStopsWhenTheGameLeavesForeground(){
        var state=Progress(100000,listOf(0)).identifyPhones();val key=state.devices()[0].key
        state=state.unlockApp(key,"wallet",false).activateMiner(key)
        assertTrue(ProgressStore(context).save(state))
        ActivityScenario.launch(MainActivity::class.java).use { android.os.SystemClock.sleep(1500L) }
        val after=ProgressStore(context).load()
        assertTrue(after.world.miner.activeMs>=1000L)
        android.os.SystemClock.sleep(1000L)
        assertEquals(after.world.miner.activeMs,ProgressStore(context).load().world.miner.activeMs)
        assertEquals(state.coins,after.coins)
    }
    @Test fun newTabletsUseCorrectPlatformsAndSurviveRestart(){
        var state=Progress(1_000_000)
        for(model in 501..503)state=state.buyNew(model)
        assertTrue(ProgressStore(context).save(state))
        ActivityScenario.launch(MainActivity::class.java).use { s->
            hub(s);click(s,"📱 Экземпляры и виртуальная ОС")
            click(s,"Открыть экран · iPad Air 11-inch (M3)")
            shown(s,"Виртуальный iPadOS")
            click(s,"⚙ Настройки")
            shown(s,"Установить: Джейлбрейк (симуляция)")
            s.onActivity { activity->
                assertFalse(views(activity.window.decorView).filterIsInstance<TextView>().any { it.text.toString()=="Установить: LineageOS (симуляция)" })
            }
            s.recreate()
            assertEquals(listOf(501,502,503),ProgressStore(context).load().owned)
            click(s,"Главное");hub(s);click(s,"📱 Экземпляры и виртуальная ОС")
            click(s,"Открыть экран · Samsung Galaxy Tab S9 FE+");click(s,"⚙ Настройки")
            shown(s,"Установить: LineageOS (симуляция)")
        }
    }
    @Test fun visualWorldFlows(){
        var state=Progress(2_000_000,listOf(0,1,4,5,24,500,501,502,503)).identifyPhones()
        val key=state.devices().first().key
        state=state.updateVirtualProfile(key,VirtualProfile(apps=setOf("snake","gallery","messages","wallet","clicker")))
        assertTrue(ProgressStore(context).save(state))
        ActivityScenario.launch(MainActivity::class.java).use { s->
            hub(s);shown(s,"Мир телефонов 4.0")
            click(s,"📱 Экземпляры и виртуальная ОС");click(s,"Открыть экран · Nokia 3310")
            shown(s,"NOKIA • МЕНЮ");click(s,"3D-просмотр");click(s,"Повернуть на 90°")
            s.onActivity { activity->
                for(label in listOf("Повернуть на 90°","Сбросить камеру","Приблизить","Отдалить")){
                    val button=views(activity.window.decorView).filterIsInstance<Button>().first { it.text.toString()==label }
                    assertTrue("Camera control must be visible: $label",button.getGlobalVisibleRect(android.graphics.Rect()))
                }
            }
            click(s,"Главное")
            assertEquals(9,ProgressStore(context).load().owned.size)
        }
    }
}
