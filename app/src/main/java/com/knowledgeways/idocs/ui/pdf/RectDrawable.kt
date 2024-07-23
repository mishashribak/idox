package com.knowledgeways.idocs.ui.pdf

import android.graphics.*
import android.util.Log
import androidx.annotation.UiThread
import androidx.core.graphics.toRectF
import com.pspdfkit.ui.drawable.PdfDrawable

class RectDrawable(
    private val text: String,
    startingPoint: PointF,
    mTextColor: String,
    mTextSize: Float,
    mTextAlpha: Double
) : PdfDrawable() {

    private val pageCoordinates = RectF()

    private val redPaint = Paint().apply {
        color = Color.parseColor(mTextColor)

        style = Paint.Style.FILL_AND_STROKE
        alpha = (mTextAlpha * 100).toInt()
        textSize = mTextSize
    }

    init {
        calculatePageCoordinates(text, startingPoint)
    }

    private fun calculatePageCoordinates(text: String, point: PointF) {
        val textBounds = Rect()
        redPaint.getTextBounds(text, 0, text.length, textBounds)
        pageCoordinates.set(
            point.x,
            point.y + textBounds.height().toFloat(),
            point.x + textBounds.width().toFloat(),
            point.y
        )

    }

    private val bluePaint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.FILL
        alpha = 50
    }

    private val screenCoordinates = RectF()

    /**
     * This method performs all the drawing required by this drawable.
     * Keep this method fast to maintain performant UI.
     */
    override fun draw(canvas: Canvas) {
        val bounds = bounds.toRectF()
        canvas.save()
        // Rotate canvas by 45 degrees.
        canvas.rotate(-45f, bounds.left, bounds.bottom)
        canvas.drawText(text, bounds.left + 20, bounds.bottom, redPaint)

        canvas.restore()
    }

    /**
     * PSPDFKit calls this method every time the page was moved or resized on screen.
     * It will provide a fresh transformation for calculating screen coordinates from
     * PDF coordinates.
     */
    override fun updatePdfToViewTransformation(matrix: Matrix) {
        super.updatePdfToViewTransformation(matrix)
        updateScreenCoordinates()
    }

    private fun updateScreenCoordinates() {
        // Calculate the screen coordinates by applying the PDF-to-view transformation.
        getPdfToPageTransformation().mapRect(screenCoordinates, pageCoordinates)

        // Rounding out to ensure that content does not clip.
        val bounds = Rect()
        screenCoordinates.roundOut(bounds)
        this.bounds = bounds
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