package com.phonesimulator.game
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
@RunWith(AndroidJUnit4::class)
class BurmaldoCompatibilityTest {
    private fun views(v:View):List<View> = listOf(v)+if(v is ViewGroup)(0 until v.childCount).flatMap { views(v.getChildAt(it)) } else emptyList()
    @Test fun ultrasOpenTheirScreenAnd3dThenRestore(){
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        context.getSharedPreferences("local_players",0).edit().clear().commit()
        context.getSharedPreferences("game",0).edit().clear().commit()
        val ids=listOf(Catalog.NOKIA,Catalog.BURMALDO,Catalog.TELIFON)
        val store=ProgressStore(context);assertTrue(store.save(Progress(200000,ids)))
        context.assets.open("telifon.jpg").use { assertNotNull(android.graphics.BitmapFactory.decodeStream(it)) }
        ActivityScenario.launch(MainActivity::class.java).use { s->
            fun click(label:String){s.onActivity { a->views(a.window.decorView).filterIsInstance<Button>().first { it.text.toString()==label }.performClick() }}
            for(id in ids){
                click("Каталог")
                s.onActivity { a->
                    views(a.window.decorView).filterIsInstance<EditText>().first().setText(Catalog.phones[id].name)
                    assertFalse(views(a.window.decorView).filterIsInstance<Button>().any { it.text.startsWith("Купить новый") })
                }
                click("Открыть экран");click("3D-просмотр");click("Повернуть на 90°")
                s.recreate()
                s.onActivity { a->assertTrue(views(a.window.decorView).any { it.tag=="phone3d" }) }
                assertEquals(ids,store.load().owned)
            }
        }
    }
}
