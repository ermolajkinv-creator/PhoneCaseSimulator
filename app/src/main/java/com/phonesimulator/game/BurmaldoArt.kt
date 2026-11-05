package com.phonesimulator.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

/** Original offline meme illustration, shared by the card and the 3D rear texture. */
internal object BurmaldoArt {
    fun face(canvas:Canvas,bounds:RectF){
        val p=Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.save();canvas.translate(bounds.left,bounds.top);canvas.scale(bounds.width()/100f,bounds.height()/140f)
        p.color=0xFFDBF98B.toInt();canvas.drawRoundRect(RectF(5f,13f,95f,125f),24f,24f,p)
        p.color=0xFF272338.toInt();canvas.drawCircle(29f,46f,17f,p);canvas.drawCircle(71f,46f,17f,p)
        p.color=0xFF8062BB.toInt();canvas.drawCircle(29f,46f,11f,p);canvas.drawCircle(71f,46f,11f,p)
        p.color=Color.WHITE;canvas.drawCircle(25f,41f,4f,p);canvas.drawCircle(67f,41f,4f,p)
        p.color=0xFF272338.toInt();p.strokeWidth=6f;p.strokeCap=Paint.Cap.ROUND
        canvas.drawLine(16f,17f,41f,25f,p);canvas.drawLine(59f,25f,84f,17f,p)
        canvas.drawRoundRect(RectF(19f,77f,81f,108f),13f,13f,p)
        p.color=Color.WHITE;canvas.drawRoundRect(RectF(24f,80f,76f,91f),3f,3f,p)
        p.color=0xFF272338.toInt();p.strokeWidth=2f
        for(x in 34..66 step 10)canvas.drawLine(x.toFloat(),80f,x.toFloat(),91f,p)
        p.color=0xFFFF8FB8.toInt();canvas.drawOval(RectF(39f,97f,64f,107f),p)
        canvas.restore()
    }
}
