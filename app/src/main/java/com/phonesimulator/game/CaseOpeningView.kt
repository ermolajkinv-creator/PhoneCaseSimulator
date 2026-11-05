package com.phonesimulator.game

import android.content.Context
import android.graphics.*
import android.view.View
import kotlin.math.sin

/** A presentation of an already saved draw. Frames never generate or grant rewards. */
internal class CaseOpeningView(context: Context,model: Int,private val count: Int=1): View(context) {
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val rect=RectF()
    private val slots=(0..12).map { if(it==12)model else (it*37+11)%Catalog.phones.size }
    private val art=slots.distinct().associateWith { index ->
        Bitmap.createBitmap(140,180,Bitmap.Config.ARGB_8888).also { bitmap ->
            PhoneArtView(context,Catalog.phones[index]).apply {
                measure(MeasureSpec.makeMeasureSpec(140,MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(180,MeasureSpec.EXACTLY))
                layout(0,0,140,180);draw(Canvas(bitmap))
            }
        }
    }
    var progress=0f
        set(value){field=value.coerceIn(0f,1f);invalidate()}
    init { tag="case-reel";importantForAccessibility=IMPORTANT_FOR_ACCESSIBILITY_NO }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w=width.toFloat();val h=height.toFloat();val cx=w/2;val cy=h/2
        val accent=Catalog.phones[slots.last()].rarity.accent
        paint.shader=RadialGradient(cx,cy,maxOf(w,h)*0.6f,intArrayOf(0x553F58A5,Color.TRANSPARENT),null,Shader.TileMode.CLAMP)
        canvas.drawRect(0f,0f,w,h,paint);paint.shader=null
        if(progress<0.2f){
            canvas.save();canvas.rotate(sin(progress*150f)*7f,cx,cy)
            val size=minOf(w*0.25f,h*0.3f)*(1f+progress)
            paint.color=0xFF232D48.toInt();rect.set(cx-size,cy-size,cx+size,cy+size);canvas.drawRoundRect(rect,18f,18f,paint)
            paint.style=Paint.Style.STROKE;paint.strokeWidth=4f;paint.color=0xFF9AAEFF.toInt();canvas.drawRoundRect(rect,18f,18f,paint)
            canvas.drawLine(cx-size,cy-size/3,cx+size,cy-size/3,paint);paint.style=Paint.Style.FILL
            paint.color=Color.WHITE;paint.textSize=size*0.55f;paint.textAlign=Paint.Align.CENTER;canvas.drawText(if(count>1)"×$count" else "?",cx,cy+size*0.4f,paint)
            canvas.restore();return
        }
        val travel=((progress-0.2f)/0.8f).coerceIn(0f,1f)*12f
        val cardW=minOf(w*0.62f,h*0.75f);val cardH=h*0.76f;val gap=cardW+12f
        for(i in slots.indices){
            val x=cx+(i-travel)*gap
            if(x+cardW/2<0 || x-cardW/2>w)continue
            val phone=Catalog.phones[slots[i]]
            paint.color=0xFF192237.toInt();rect.set(x-cardW/2,cy-cardH/2,x+cardW/2,cy+cardH/2);canvas.drawRoundRect(rect,18f,18f,paint)
            paint.style=Paint.Style.STROKE;paint.strokeWidth=3f;paint.color=phone.rarity.accent;canvas.drawRoundRect(rect,18f,18f,paint);paint.style=Paint.Style.FILL
            rect.set(x-cardW*0.44f,cy-cardH*0.43f,x+cardW*0.44f,cy+cardH*0.25f)
            canvas.drawBitmap(art.getValue(slots[i]),null,rect,paint)
            paint.textAlign=Paint.Align.CENTER;paint.color=Color.WHITE;paint.textSize=minOf(cardW/13,18f*resources.displayMetrics.scaledDensity)
            canvas.drawText(phone.name.take(24),x,cy+cardH*0.37f,paint)
        }
        paint.color=if(progress>0.92f)accent else Color.WHITE
        val path=Path().apply { moveTo(cx-10,4f);lineTo(cx+10,4f);lineTo(cx,22f);close() };canvas.drawPath(path,paint)
        paint.style=Paint.Style.STROKE;paint.strokeWidth=2f;canvas.drawLine(cx,h-23f,cx,h-5f,paint);paint.style=Paint.Style.FILL
    }
}
