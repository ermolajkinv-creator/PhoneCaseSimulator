package com.phonesimulator.game
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
@RunWith(AndroidJUnit4::class)
class EmpireCompatibilityTest {
    private fun views(v:View):List<View> = listOf(v)+if(v is ViewGroup)(0 until v.childCount).flatMap { views(v.getChildAt(it)) } else emptyList()
    @Test fun allTwentyFivePagesAndWorkshopSaveRoundTrip(){
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        context.getSharedPreferences("local_players",0).edit().clear().commit()
        context.getSharedPreferences("game",0).edit().clear().commit()
        val store=ProgressStore(context);assertTrue(store.save(Progress(100000,listOf(0,1,4,5,500))))
        ActivityScenario.launch(MainActivity::class.java).use { s->
            fun click(label:String){s.onActivity { a->views(a.window.decorView).filterIsInstance<Button>().first { it.text.toString()==label }.performClick() }}
            click("Новое")
            for((i,f) in EmpireFeature.entries.withIndex()){
                click("${i+1}. ${f.title}")
                s.onActivity { a->assertTrue(views(a.window.decorView).filterIsInstance<TextView>().any { it.text.toString()==f.description }) }
                click("Все 25 функций")
            }
            click("11. Склад запчастей");click("Купить 5 деталей · 2000 ₽");click("Все 25 функций")
            click("4. Диагностика");click("Принять заказ");click("Проверить: Экран");click("Перейти к ремонту");click("Выполнить ремонт")
            val saved=store.load();assertEquals(99500,saved.coins);assertEquals(1L,saved.world.empire["jobs"])
            s.recreate();assertEquals(saved.world.empire,store.load().world.empire)
            assertEquals(saved.world,WorldCodec.decode(WorldCodec.encode(saved.world)))
            assertEquals(Empire(),WorldCodec.decode("{}").empire)
            assertTrue(store.createBackup())
            val next=EmpireRules.act(saved,EmpireFeature.PARTS).progress;assertTrue(store.save(next));assertTrue(store.restoreBackup())
            assertEquals(next.world.empire,store.load().world.empire);assertEquals(next.coins,store.load().coins)
        }
    }
}
