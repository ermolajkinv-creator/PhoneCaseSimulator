package com.phonesimulator.game

import android.content.Context
import android.graphics.*
import android.view.View

/** Offline stylized illustrations: camera layouts and form factors distinguish families. */
class PhoneArtView(context: Context, private val phone: Phone,private val colorOverride: Int = 0) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val photo = when (phone.name) {
        "Samsung Z Fold 8 Ultra" -> "fold8-ultra.jpg"
        "Палас улыбашкэ" -> "palas-smile.jpg"
        "ТелИфон" -> "telifon.jpg"
        else -> null
    }?.let { path -> context.assets.open(path).use { BitmapFactory.decodeStream(it) } }
    init { importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO }
    private fun box(c: Canvas, x: Float, y: Float, w: Float, h: Float, color: Int, radius: Float = 7f) {
        paint.shader = null; paint.color = color
        c.drawRoundRect(RectF(x,y,x+w,y+h),radius,radius,paint)
    }
    private fun lens(c: Canvas, x: Float, y: Float, radius: Float = 7f) {
        paint.color = 0xFF656971.toInt(); c.drawCircle(x,y,radius+1.5f,paint)
        paint.color = 0xFF10151D.toInt(); c.drawCircle(x,y,radius,paint)
        paint.color = 0xFF253D59.toInt(); c.drawCircle(x,y,radius*0.55f,paint)
        paint.color = 0xFF96BAC9.toInt(); c.drawCircle(x-2,y-2,1.5f,paint)
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        photo?.let {
            val scale = minOf(width.toFloat()/it.width,height.toFloat()/it.height)
            val w=it.width*scale; val h=it.height*scale
            canvas.drawBitmap(it,null,RectF((width-w)/2,(height-h)/2,(width+w)/2,(height+h)/2),paint)
            return
        }
        canvas.save();canvas.translate(width/2f,height/2f);canvas.scale(minOf(height/160f,width/170f),minOf(height/160f,width/170f))
        val n=phone.name
        val iphone=n.startsWith("iPhone")
        val pixel=n.contains("Pixel")
        val flip=n.contains("Flip") || n.contains("Razr")
        val fold=n.contains("Fold") || (n.startsWith("OPPO Find N") && !flip)
        val nokia=n=="Nokia 3310"
        val tones=intArrayOf(0xFFCCC4B9.toInt(),0xFF334A47.toInt(),0xFF333544.toInt(),0xFFC6BACE.toInt())
        val body=if(colorOverride!=0)colorOverride else if(n=="Бурмалдофон") 0xFFAD88DC.toInt() else if(n.contains("17 Pro")) 0xFFD78C54.toInt() else tones[(n.hashCode() and Int.MAX_VALUE)%tones.size]
        if(phone.formFactor==FormFactor.TABLET) {
            // Front and rear at tablet aspect ratios; cameras stay in the bezel.
            box(canvas,-81f,-58f,79f,116f,body,6f)
            lens(canvas,-72f,-48f,4f)
            if(n.contains("S9 FE")) lens(canvas,-72f,-36f,4f)
            box(canvas,0f,-58f,81f,116f,0xFF171B23.toInt(),6f)
            paint.shader=LinearGradient(4f,-52f,76f,53f,intArrayOf(body,0xFF243661.toInt(),phone.rarity.accent),null,Shader.TileMode.CLAMP)
            canvas.drawRoundRect(RectF(5f,-51f,76f,51f),3f,3f,paint);paint.shader=null
            paint.color=0xFF546476.toInt();canvas.drawCircle(3f,0f,1.2f,paint)
            paint.color=0x885B6878.toInt();paint.textSize=7f;paint.textAlign=Paint.Align.CENTER
            canvas.drawText(if(phone.isApple) "iPad" else if(n.startsWith("Samsung")) "SAMSUNG" else "BLACKVIEW",-42f,40f,paint)
            paint.textAlign=Paint.Align.LEFT
            canvas.restore();return
        }
        box(canvas,-76f,-64f,64f,128f,body,if(n.contains("Ultra")) 3f else 10f)
        when {
            n=="Бурмалдофон" -> BurmaldoArt.face(canvas,RectF(-74f,-59f,-14f,42f))
            nokia -> {
                box(canvas,-65f,-48f,42f,38f,0xFF9EA98A.toInt(),3f)
                for(row in 0..3) for(col in 0..2) box(canvas,-64f+col*14,-1f+row*13,11f,8f,0xFFBEC3C9.toInt(),3f)
            }
            pixel && (n == "Google Pixel 4" || n == "Google Pixel 5") -> {
                box(canvas,-72f,-59f,32f,32f,0xFF20252B.toInt())
                lens(canvas,-62f,-49f,5f);lens(canvas,-49f,-37f,5f)
            }
            pixel -> {
                box(canvas,-76f,-45f,64f,24f,0xFF7F898D.toInt(),if(n.contains("9")||n.contains("10"))12f else 1f)
                box(canvas,-69f,-41f,44f,16f,0xFF171E25.toInt(),9f)
                lens(canvas,-60f,-33f,5f);lens(canvas,-46f,-33f,5f)
                if(n.contains("Pro")) lens(canvas,-32f,-33f,5f)
            }
            iphone -> {
                val pro=n.contains("Pro")
                box(canvas,-72f,-59f,if(n.contains("17 Pro"))56f else 34f,37f,0xFFA6A3A0.toInt(),8f)
                lens(canvas,-61f,-49f);lens(canvas,if(pro)-61f else -43f,-32f)
                if(pro) lens(canvas,-43f,-41f)
            }
            n.contains("Galaxy") && !flip && !fold -> {
                lens(canvas,-65f,-49f);lens(canvas,-65f,-29f);lens(canvas,-65f,-9f)
                if(n.contains("Ultra")) {lens(canvas,-46f,-41f,4f);lens(canvas,-46f,-23f,4f)}
            }
            n.contains("Magic") || n.contains("OnePlus 12") || n.contains("OnePlus 13") || n.contains("Xiaomi 14 Ultra") -> {
                paint.color=0xFF353A40.toInt();canvas.drawCircle(-44f,-35f,23f,paint)
                lens(canvas,-53f,-43f);lens(canvas,-35f,-43f);lens(canvas,-44f,-25f)
            }
            else -> {
                box(canvas,-72f,-59f,35f,41f,0xFF343B44.toInt())
                lens(canvas,-62f,-48f);lens(canvas,-48f,-31f)
                if(n.contains("Nothing")) {
                    paint.color=Color.WHITE;paint.style=Paint.Style.STROKE;paint.strokeWidth=2f
                    canvas.drawCircle(-44f,5f,17f,paint);canvas.drawLine(-44f,27f,-44f,49f,paint);paint.style=Paint.Style.FILL
                }
            }
        }
        val fw=if(fold)82f else 61f
        box(canvas,-5f,-64f,fw,128f,0xFF171B23.toInt(),if(fold)4f else 9f)
        paint.shader=LinearGradient(0f,-60f,fw,55f,intArrayOf(body,0xFF212D4C.toInt(),phone.rarity.accent),null,Shader.TileMode.CLAMP)
        canvas.drawRoundRect(RectF(-1f,-60f,fw-9f,60f),5f,5f,paint);paint.shader=null
        if(iphone) box(canvas,14f,-57f,22f,6f,Color.BLACK,5f)
        else {paint.color=Color.BLACK;canvas.drawCircle(fw/2-5,-54f,2f,paint)}
        if(fold||flip) {
            paint.color=0x88656C78.toInt();paint.strokeWidth=1f
            if(fold) canvas.drawLine(35f,-60f,35f,60f,paint) else canvas.drawLine(0f,0f,fw-9f,0f,paint)
        }
        canvas.restore()
    }
}
