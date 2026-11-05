package com.phonesimulator.game

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.StateListAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.OvershootInterpolator

/** Short, finite UI effects; the system animation preference is respected on API 26+. */
internal object GameMotion {
    fun enabled(context:android.content.Context?=null) = ValueAnimator.areAnimatorsEnabled() && (context as? MainActivity)?.look?.motion!=2
    fun full(context:android.content.Context) = enabled(context) && ((context as? MainActivity)?.look?.motion ?: 0)==0
    fun press(view: View) {
        if(!full(view.context))return
        fun scale(value: Float)=AnimatorSet().apply {
            playTogether(ObjectAnimator.ofFloat(view,"scaleX",value),ObjectAnimator.ofFloat(view,"scaleY",value))
            duration=100L
        }
        view.stateListAnimator=StateListAnimator().apply {
            addState(intArrayOf(android.R.attr.state_pressed,android.R.attr.state_enabled),scale(0.97f))
            addState(intArrayOf(),scale(1f))
        }
    }
    fun enter(view: View,delay: Long=0L,pop: Boolean=false) {
        if(!enabled(view.context))return
        if(!full(view.context)){view.alpha=0f;view.animate().alpha(1f).setDuration(100L).start();return}
        view.alpha=0f;view.translationY=12f*view.resources.displayMetrics.density
        if(pop){view.scaleX=0.92f;view.scaleY=0.92f}
        view.animate().alpha(1f).translationY(0f).scaleX(1f).scaleY(1f)
            .setStartDelay(delay).setDuration(if(pop)420L else 180L)
            .setInterpolator(OvershootInterpolator(if(pop)0.8f else 0f)).start()
    }
}
