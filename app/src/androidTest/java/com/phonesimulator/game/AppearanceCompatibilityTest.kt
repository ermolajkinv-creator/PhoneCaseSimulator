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
class AppearanceCompatibilityTest {
    private fun views(v:View):List<View> = listOf(v)+if(v is ViewGroup)(0 until v.childCount).flatMap { views(v.getChildAt(it)) } else emptyList()
    @Test fun customizationPersistsIsolatedFromFriendsAndDoesNotChangeEconomy(){
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        context.getSharedPreferences("local_players",0).edit().clear().commit()
        context.getSharedPreferences("game",0).edit().clear().commit()
        val store=ProgressStore(context);assertTrue(store.save(Progress(200000,listOf(0,501,502))))
        val friend=LocalProfiles(context).create("Друг оформления")!!
        ActivityScenario.launch(MainActivity::class.java).use { s->
            fun click(label:String){s.onActivity { a->views(a.window.decorView).filterIsInstance<Button>().first { it.text.toString()==label }.performClick() }}
            click("Оформление игры")
            click("Сменить тему");click("Сменить акцент");click("Сменить фон");click("Сменить форму карточек");click("Изменить размер текста")
            click("Сменить анимации");click("Сменить анимации")
            val expected=AppLook(1,1,1,1,2,true)
            assertEquals(expected,store.load().world.look)
            assertEquals(200000,store.load().coins);assertEquals(listOf(0,501,502),store.load().owned)
            s.recreate();assertEquals(expected,store.load().world.look)
            assertEquals(expected,WorldCodec.decode(WorldCodec.encode(store.load().world)).look)
            assertEquals(AppLook(),WorldCodec.decode("{}").look)
            assertEquals(AppLook(3,0,2,0,2,false),WorldCodec.decode("{\"look\":{\"theme\":99,\"accent\":-1,\"background\":99,\"corners\":-1,\"motion\":99}}").look)
            s.onActivity { a->a.switchPlayer(friend);assertEquals(AppLook(),a.look);a.switchPlayer("main");assertEquals(expected,a.look) }
            s.onActivity { a->
                assertFalse(GameMotion.full(a));assertFalse(GameMotion.enabled(a))
                views(a.window.decorView).filterIsInstance<Button>().first { it.text.toString()=="Открыть кейс · ${CaseKind.CLASSIC.cost} ₽" }.performClick()
                assertTrue(views(a.window.decorView).filterIsInstance<TextView>().any { it.text.toString()=="Новый телефон!" })
                assertFalse(views(a.window.decorView).any { it.tag=="case-reel" })
            }
            assertEquals(200000-CaseKind.CLASSIC.cost,store.load().coins);assertEquals(4,store.load().owned.size)
            click("Главное");click("Оформление игры");click("Сбросить оформление")
            assertEquals(AppLook(),store.load().world.look)
            assertEquals(4,store.load().owned.size)
        }
    }
}
