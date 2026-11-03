package com.phonesimulator.game

import android.graphics.Color
import android.graphics.drawable.GradientDrawable

/** Purely cosmetic, saved with the player's WorldData. All palettes retain readable light text. */
data class AppLook(val theme:Int=0,val accent:Int=0,val background:Int=0,val corners:Int=0,val motion:Int=0,val largeText:Boolean=false) {
    fun valid()=copy(theme=theme.coerceIn(0,3),accent=accent.coerceIn(0,5),background=background.coerceIn(0,2),corners=corners.coerceIn(0,2),motion=motion.coerceIn(0,2))
    val accentColor get()=accents[accent.coerceIn(0,5)]
    val base get()=intArrayOf(0xFF090D19.toInt(),0xFF160E22.toInt(),0xFF061A20.toInt(),Color.BLACK)[theme.coerceIn(0,3)]
    val surface get()=intArrayOf(0xFF1B243A.toInt(),0xFF30203F.toInt(),0xFF17353D.toInt(),0xFF181818.toInt())[theme.coerceIn(0,3)]
    val muted get()=0xFFC6CDDF.toInt()
    val radius get()=intArrayOf(26,16,8)[corners.coerceIn(0,2)]
    val textScale get()=if(largeText)1.12f else 1f
    fun panel(density:Float,edge:Int=accentColor,strong:Boolean=false)=GradientDrawable(GradientDrawable.Orientation.TL_BR,
        intArrayOf(if(strong)blend(surface,edge,0.24f) else blend(surface,Color.WHITE,0.05f),surface,blend(surface,base,0.35f))).apply {
        cornerRadius=radius*density
        setStroke(maxOf(1,density.toInt()),Color.argb(if(strong)180 else 70,Color.red(edge),Color.green(edge),Color.blue(edge)))
    }
    fun backdrop()=GradientDrawable(GradientDrawable.Orientation.BL_TR,when(background){
        1->intArrayOf(base,blend(base,accentColor,0.16f),base)
        2->intArrayOf(base,base)
        else->intArrayOf(blend(base,accentColor,0.10f),base,blend(base,surface,0.65f))
    })
    companion object {
        val themes=listOf("Полночь","Аметист","Океан","OLED")
        val accentNames=listOf("Лаванда","Мята","Роза","Золото","Лёд","Персик")
        val accents=intArrayOf(0xFFACACFF.toInt(),0xFF80E7C7.toInt(),0xFFFF9DCB.toInt(),0xFFFFD282.toInt(),0xFF86D9FF.toInt(),0xFFFFB599.toInt())
        val backgrounds=listOf("Северное сияние","Акцентный градиент","Однотонный")
        val cornerNames=listOf("Мягкие","Скруглённые","Строгие")
        val motions=listOf("Полные","Минимальные","Выключены")
        private fun blend(a:Int,b:Int,f:Float)=Color.rgb((Color.red(a)*(1-f)+Color.red(b)*f).toInt(),(Color.green(a)*(1-f)+Color.green(b)*f).toInt(),(Color.blue(a)*(1-f)+Color.blue(b)*f).toInt())
    }
}
