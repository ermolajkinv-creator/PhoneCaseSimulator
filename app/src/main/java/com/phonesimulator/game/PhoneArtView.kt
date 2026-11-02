package com.phonesimulator.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.view.View

/** Vector phone artwork stays crisp on large and foldable screens; no image downloads. */
class PhoneArtView(context: Context, private val phone: Phone) : View(context) {
    private val photo = if (phone.rarity == Rarity.ULTRA)
        context.assets.open("fold8-ultra.jpg").use { android.graphics.BitmapFactory.decodeStream(it) } else null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    init {
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        minimumHeight = (156 * resources.displayMetrics.density).toInt()
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        photo?.let { bitmap ->
            val scale = minOf(width.toFloat() / bitmap.width, height.toFloat() / bitmap.height)
            val w = bitmap.width * scale
            val h = bitmap.height * scale
            canvas.drawBitmap(bitmap, null, RectF((width-w)/2, (height-h)/2, (width+w)/2, (height+h)/2), paint)
            return
        }
        val unit = height / 160f
        canvas.save()
        canvas.translate(width / 2f, height / 2f)
        canvas.scale(unit, unit)
        val folded = phone.name.contains("Fold", ignoreCase = true)
        val halfWidth = if (folded) 57f else 35f
        paint.shader = null
        paint.color = phone.rarity.accent
        paint.alpha = 22
        canvas.drawOval(RectF(-90f, 52f, 90f, 72f), paint)
        paint.alpha = 255
        paint.shader = LinearGradient(-halfWidth, -64f, halfWidth, 64f,
            intArrayOf(phone.rarity.accent, 0xFF151E35.toInt()), null, Shader.TileMode.CLAMP)
        canvas.drawRoundRect(RectF(-halfWidth, -66f, halfWidth, 66f), 12f, 12f, paint)
        paint.shader = null
        paint.color = 0xFF0B1123.toInt()
        canvas.drawRoundRect(RectF(-halfWidth + 3f, -63f, halfWidth - 3f, 63f), 10f, 10f, paint)
        paint.shader = LinearGradient(-halfWidth, -55f, halfWidth, 55f,
            intArrayOf(0xFF182748.toInt(), phone.rarity.accent, 0xFF293255.toInt()),
            floatArrayOf(0f, 0.55f, 1f), Shader.TileMode.CLAMP)
        canvas.drawRoundRect(RectF(-halfWidth + 7f, -56f, halfWidth - 7f, 54f), 7f, 7f, paint)
        paint.shader = null
        paint.color = 0xFF0B1123.toInt()
        canvas.drawRoundRect(RectF(-12f, -59f, 12f, -52f), 4f, 4f, paint)
        if (folded) {
            paint.strokeWidth = 2f
            paint.alpha = 95
            canvas.drawLine(0f, -56f, 0f, 54f, paint)
            paint.alpha = 255
        }
        paint.color = 0xFFFFFFFF.toInt()
        paint.alpha = 180
        canvas.drawRoundRect(RectF(-12f, 58f, 12f, 60f), 1f, 1f, paint)
        paint.alpha = 255
        canvas.restore()
    }
}
