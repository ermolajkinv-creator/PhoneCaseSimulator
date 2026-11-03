package com.phonesimulator.game

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

/** Finite confetti: no worker, delayed payout or animation loop survives a hidden screen. */
internal class RewardBurstView(context: Context,private val rarity: Rarity): View(context) {
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
    private var animation: ValueAnimator?=null
    private var phase=1f
    init { tag="reward-burst";importantForAccessibility=IMPORTANT_FOR_ACCESSIBILITY_NO }
    override fun onAttachedToWindow(){super.onAttachedToWindow();start()}
    private fun start(){
        if(!GameMotion.full(context) || windowVisibility!=VISIBLE || animation!=null)return
        animation=ValueAnimator.ofFloat(0f,1f).apply {
            duration=1100L
            addUpdateListener { phase=it.animatedValue as Float;invalidate() }
            start()
        }
    }
    override fun onWindowVisibilityChanged(visibility: Int){super.onWindowVisibilityChanged(visibility);if(visibility!=VISIBLE){animation?.cancel();animation=null;phase=1f}else if(isAttachedToWindow)start()}
    override fun onDetachedFromWindow(){animation?.cancel();animation=null;super.onDetachedFromWindow()}
    override fun onDraw(canvas: Canvas){
        super.onDraw(canvas)
        val cx=width/2f;val cy=height/2f
        paint.color=rarity.accent
        for(i in 0..23){
            val angle=i*Math.PI/12;val radius=(0.15f+phase*0.85f)*width*0.44f
            paint.alpha=((1-phase)*220).toInt();val x=cx+cos(angle).toFloat()*radius;val y=cy+sin(angle).toFloat()*height*0.4f*phase
            canvas.drawCircle(x,y,3f*resources.displayMetrics.density,paint)
        }
        paint.alpha=255;paint.textSize=18f*resources.displayMetrics.scaledDensity;paint.textAlign=Paint.Align.CENTER
        paint.typeface=android.graphics.Typeface.DEFAULT_BOLD
        canvas.drawText("✦ ${rarity.title} ✦",cx,cy+paint.textSize/3,paint)
    }
}
