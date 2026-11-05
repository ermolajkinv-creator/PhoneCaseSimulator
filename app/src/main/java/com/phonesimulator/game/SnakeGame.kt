package com.phonesimulator.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import kotlin.random.Random

data class SnakeState(val body: List<Int>,val direction: Int=1,val queued: Int=1,val food: Int=0,val score: Int=0,val over: Boolean=false) {
    fun turn(direction: Int): SnakeState {
        require(direction in 0..3)
        return if(over || (direction-this.direction+4)%4==2) this else copy(queued=direction)
    }
    fun tick(seed: Long): SnakeState {
        if(over) return this
        val head=body.first();val x=head%20;val y=head/20
        val nx=x+listOf(0,1,0,-1)[queued];val ny=y+listOf(-1,0,1,0)[queued]
        if(nx !in 0..19 || ny !in 0..19) return copy(over=true)
        val next=ny*20+nx;val eating=next==food
        val tail=if(eating) body else body.dropLast(1)
        if(next in tail) return copy(over=true)
        val grown=listOf(next)+tail
        val free=(0 until 400).filter { it !in grown }
        return copy(body=grown,direction=queued,food=if(eating && free.isNotEmpty()) free[Random(seed+score).nextInt(free.size)] else food,
            score=score+if(eating) 1 else 0,over=free.isEmpty())
    }
    companion object {
        fun start(seed: Long): SnakeState {
            val body=listOf(210,209,208);val free=(0 until 400).filter { it !in body }
            return SnakeState(body,food=free[Random(seed).nextInt(free.size)])
        }
    }
}
class SnakeView(context: Context,private val seed: Long,private val onState: (SnakeState)->Unit): View(context) {
    private val paint=Paint()
    var state=SnakeState.start(seed);private set
    var running=false;private set
    private val tick=object: Runnable {
        override fun run() {
            if(!running || !isAttachedToWindow || windowVisibility!=VISIBLE) return
            state=state.tick(seed);if(state.over) running=false
            onState(state);invalidate();if(running) postDelayed(this,220L)
        }
    }
    init { contentDescription="Чёрно-белая пиксельная Змейка, поле 20 на 20" }
    fun turn(direction: Int){state=state.turn(direction)}
    fun playPause(){removeCallbacks(tick);if(state.over)state=SnakeState.start(seed);running=!running;onState(state);if(running)postDelayed(tick,220L);invalidate()}
    fun snapshot(): String = "${state.direction};${state.queued};${state.food};${state.score};${state.over};${state.body.joinToString(",")}"
    fun restore(raw: String){
        runCatching {
            val p=raw.split(';');val body=p[5].split(',').map { it.toInt() }
            require(body.isNotEmpty() && body.size<=400 && body.all { it in 0..399 } && body.toSet().size==body.size)
            val restored=SnakeState(body,p[0].toInt(),p[1].toInt(),p[2].toInt(),p[3].toInt(),p[4].toBooleanStrict())
            require(restored.direction in 0..3 && restored.queued in 0..3 && restored.food in 0..399 && restored.score in 0..400)
            state=restored;running=false;invalidate()
        }
    }
    override fun onWindowVisibilityChanged(visibility: Int){super.onWindowVisibilityChanged(visibility);if(visibility!=VISIBLE){running=false;removeCallbacks(tick)}}
    override fun onDetachedFromWindow(){running=false;removeCallbacks(tick);super.onDetachedFromWindow()}
    override fun onDraw(canvas: Canvas){
        super.onDraw(canvas);canvas.drawColor(0xFF9EA97E.toInt())
        val size=minOf(width,height)/20f;val left=(width-size*20)/2;val top=(height-size*20)/2
        paint.color=Color.BLACK
        fun cell(value: Int){val x=left+value%20*size;val y=top+value/20*size;canvas.drawRect(x+1,y+1,x+size-1,y+size-1,paint)}
        state.body.forEach(::cell);cell(state.food)
    }
}
