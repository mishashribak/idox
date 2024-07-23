package com.knowledgeways.idocs.ui.pdf

import android.graphics.*
import androidx.annotation.UiThread
import androidx.core.graphics.toRectF
import com.pspdfkit.ui.drawable.PdfDrawable

class CustomDrawable(private val text: String, textsize: Int) : PdfDrawable() {

    private val redPaint = Paint().apply {
        color = Color.RED

        style = Paint.Style.FILL_AND_STROKE
        alpha = 50
        textSize = textsize.toFloat()
    }

    fun calculatePageRect(text: String) : Rect {
        val textBounds = Rect()
        redPaint.getTextBounds(text, 0, text.length, textBounds)

        return textBounds
    }


    private val bluePaint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.FILL
        alpha = 50
    }


    /**
     * This method performs all the drawing required by this drawable.
     * Keep this method fast to maintain performant UI.
     */
    override fun draw(canvas: Canvas) {
        val bounds = bounds.toRectF()
        canvas.save()
        // Rotate canvas by 45 degrees.
        canvas.rotate(-45f, bounds.left, bounds.bottom)
        canvas.drawText(text, bounds.left, bounds.bottom, redPaint)

        canvas.restore()
    }

    @UiThread
    override fun setAlpha(alpha: Int) {
        bluePaint.alpha = alpha
        redPaint.alpha = alpha
        invalidateSelf()
    }

    @UiThread
    override fun setColorFilter(colorFilter: ColorFilter?) {
        bluePaint.colorFilter = colorFilter
        redPaint.colorFilter = colorFilter
        invalidateSelf()
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }
}
