package com.phonesimulator.game

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

/** Procedural 3D cuboid with perspective textured faces, not a rotating flat image. */
class Phone3DView(context: Context,private val item: DeviceSnapshot,private val style: VirtualProfile): View(context) {
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private var yaw=25f
    private var pitch=-8f
    private var zoom=1f
    private var lastX=0f;private var lastY=0f
    private val scale=ScaleGestureDetector(context,object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean { zoomBy(detector.scaleFactor);return true }
    })
    private val bodyColor=if(style.color!=0) style.color else if(item.company!=null) item.company.color else item.traits.mutation.accent
    private val front=texture(true)
    private val back=texture(false)
    init { contentDescription="Объёмный ${item.phone.name}. Вращение пальцем и масштабирование двумя пальцами." }
    fun rotate() { yaw=(yaw+90)%360;invalidate() }
    fun zoomBy(factor: Float) { zoom=(zoom*factor).coerceIn(0.6f,2.4f);invalidate() }
    fun reset() { yaw=25f;pitch=-8f;zoom=1f;invalidate() }
    private fun texture(isFront: Boolean): Bitmap {
        val bitmap=Bitmap.createBitmap(256,512,Bitmap.Config.ARGB_8888)
        val c=Canvas(bitmap);val p=Paint(Paint.ANTI_ALIAS_FLAG)
        if(isFront) {
            if(item.phone.name=="Nokia 3310") {
                c.drawColor(0xFF3A4150.toInt());p.color=0xFF9FAE83.toInt();c.drawRoundRect(25f,55f,231f,250f,12f,12f,p)
                p.color=Color.BLACK;p.textSize=32f;p.typeface=Typeface.MONOSPACE;c.drawText("NOKIA",64f,120f,p);c.drawText("12:00",72f,175f,p)
                p.color=0xFFD1D4D9.toInt();for(row in 0..3)for(col in 0..2)c.drawRoundRect(28f+col*70,285f+row*49,87f+col*70,320f+row*49,8f,8f,p)
            } else {
                p.shader=LinearGradient(0f,0f,256f,512f,intArrayOf(bodyColor,0xFF172248.toInt(),0xFF090F1B.toInt()),null,Shader.TileMode.CLAMP)
                c.drawRect(0f,0f,256f,512f,p);p.shader=null;p.color=Color.WHITE;p.textSize=48f;c.drawText("12:00",60f,110f,p)
                p.textSize=14f;c.drawText("Phone OS",92f,140f,p)
                for(row in 0..2)for(col in 0..2){p.color=VirtualApps.colors[(row*3+col+style.wallpaper)%6];c.drawRoundRect(28f+col*78,230f+row*73,76f+col*78,278f+row*73,10f,10f,p)}
                p.color=Color.BLACK;c.drawRoundRect(95f,10f,161f,23f,9f,9f,p)
            }
        } else {
            c.drawColor(bodyColor)
            if(item.traits.mutation==Mutation.RAINBOW || style.theme=="rainbow"){
                p.shader=LinearGradient(0f,0f,256f,512f,intArrayOf(0xFFF38FB1.toInt(),0xFFFFD878.toInt(),0xFF70D6B8.toInt(),0xFF719FFF.toInt(),0xFFB08CF6.toInt()),null,Shader.TileMode.CLAMP)
                c.drawRect(0f,0f,256f,512f,p);p.shader=null
            }
            if(item.traits.mutation==Mutation.GALAXY || style.theme=="galaxy"){
                p.color=Color.WHITE;for(i in 0..35)c.drawCircle(((i*47)%250+3).toFloat(),((i*83)%500+5).toFloat(),if(i%4==0)2.5f else 1.2f,p)
            }
            if(item.traits.mutation==Mutation.GLITCH || style.theme=="glitch"){
                p.color=0xAA8BFFDA.toInt();for(i in 0..8)c.drawRect((i*29%180).toFloat(),(i*59+15).toFloat(),(i*29%180+70).toFloat(),(i*59+22).toFloat(),p)
            }
            if(item.model==Catalog.PALAS) {
                p.color=0xFFDBDEE2.toInt();c.drawCircle(128f,120f,85f,p);p.color=Color.BLACK
                c.drawCircle(99f,95f,20f,p);c.drawCircle(157f,95f,20f,p)
                p.style=Paint.Style.STROKE;p.strokeWidth=7f;c.drawArc(RectF(76f,105f,180f,175f),0f,180f,false,p);p.style=Paint.Style.FILL
            } else if(item.phone.name!="Nokia 3310") {
                p.color=0xFF242A34.toInt();c.drawRoundRect(14f,18f,110f,145f,16f,16f,p)
                for(i in 0..2){p.color=0xFF848D9A.toInt();c.drawCircle(45f,43f+i*40,15f,p);p.color=0xFF071222.toInt();c.drawCircle(45f,43f+i*40,11f,p)}
            }
            p.color=0xFF202632.toInt();p.textSize=18f;p.textAlign=Paint.Align.CENTER
            c.drawText(item.phone.name.take(24),128f,375f,p)
            if(item.traits.mutation!=Mutation.NORMAL){p.textSize=23f;c.drawText(item.traits.mutation.title,128f,425f,p)}
        }
        return bitmap
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        scale.onTouchEvent(event)
        when(event.actionMasked){
            MotionEvent.ACTION_DOWN->{lastX=event.x;lastY=event.y;parent?.requestDisallowInterceptTouchEvent(true)}
            MotionEvent.ACTION_MOVE->{if(!scale.isInProgress && event.pointerCount==1){yaw=(yaw+(event.x-lastX)*0.65f)%360;pitch=(pitch+(event.y-lastY)*0.35f).coerceIn(-70f,70f);invalidate()};lastX=event.x;lastY=event.y}
            MotionEvent.ACTION_UP->{parent?.requestDisallowInterceptTouchEvent(false);performClick()}
            MotionEvent.ACTION_CANCEL->parent?.requestDisallowInterceptTouchEvent(false)
        };return true
    }
    override fun performClick(): Boolean { super.performClick();return true }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val a=Math.toRadians(yaw.toDouble());val b=Math.toRadians(pitch.toDouble())
        val wide=if(item.phone.name.contains("Fold") || item.phone.name.startsWith("OPPO Find N")) 0.85 else 0.52
        val points=listOf(doubleArrayOf(-wide,-1.0,-0.07),doubleArrayOf(wide,-1.0,-0.07),doubleArrayOf(wide,1.0,-0.07),doubleArrayOf(-wide,1.0,-0.07),
            doubleArrayOf(-wide,-1.0,0.07),doubleArrayOf(wide,-1.0,0.07),doubleArrayOf(wide,1.0,0.07),doubleArrayOf(-wide,1.0,0.07)).map {
            val x=it[0]*cos(a)+it[2]*sin(a);val z=-it[0]*sin(a)+it[2]*cos(a)
            doubleArrayOf(x,it[1]*cos(b)-z*sin(b),it[1]*sin(b)+z*cos(b))
        }
        val pulse=if(style.animation=="pulse") 1f+sin(System.currentTimeMillis()/500.0).toFloat()*0.025f else 1f
        val size=minOf(width/2.4f,height/2.7f)*zoom*pulse
        fun project(i: Int): PointF { val p=points[i];val perspective=4.0/(4.0-p[2]);return PointF(width/2f+(p[0]*perspective*size).toFloat(),height/2f+(p[1]*perspective*size).toFloat()) }
        val faces=listOf(listOf(1,0,3,2),listOf(4,5,6,7),listOf(0,1,5,4),listOf(1,2,6,5),listOf(2,3,7,6),listOf(3,0,4,7))
        faces.withIndex().sortedBy { face -> face.value.sumOf { points[it][2] } }.forEach { face ->
            val positions=face.value.map(::project);val path=Path().apply { moveTo(positions[0].x,positions[0].y);positions.drop(1).forEach { lineTo(it.x,it.y) };close() }
            paint.shader=null;paint.color=if(face.index<2) bodyColor else 0xFF485362.toInt();paint.style=Paint.Style.FILL;canvas.drawPath(path,paint)
            if(face.index<2){
                val bitmap=if(face.index==1) front else back
                val src=floatArrayOf(0f,0f,256f,0f,256f,512f,0f,512f)
                val dst=positions.flatMap { listOf(it.x,it.y) }.toFloatArray();val matrix=Matrix()
                if(matrix.setPolyToPoly(src,0,dst,0,4)){canvas.save();canvas.clipPath(path);canvas.drawBitmap(bitmap,matrix,paint);canvas.restore()}
            }
            paint.style=Paint.Style.STROKE;paint.strokeWidth=2f;paint.color=0xFFAEBACD.toInt();canvas.drawPath(path,paint);paint.style=Paint.Style.FILL
        }
        if(style.animation=="sparkle"){
            paint.color=0xFFFFE599.toInt();for(i in 0..9){val phase=System.currentTimeMillis()/400.0+i*1.7
                canvas.drawCircle(width/2f+sin(phase).toFloat()*width*0.35f,height/2f+cos(phase*0.8).toFloat()*height*0.35f,2f+sin(phase).toFloat(),paint)}
        }
        if(style.animation in listOf("pulse","sparkle") && isAttachedToWindow && windowVisibility==VISIBLE) postInvalidateOnAnimation()
    }
}
