package com.knowledgeways.idocs.ui.pdf

import android.graphics.*
import android.util.Log
import androidx.annotation.UiThread
import androidx.core.graphics.toRectF
import com.pspdfkit.ui.drawable.PdfDrawable

class WatermarkDrawable(private val text: String, startingPoint: PointF) : PdfDrawable() {

    private val redPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
        alpha = 100
        textSize = 30f
    }

    private val pageCoordinates = RectF()
    private val screenCoordinates = RectF()

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

    private fun updateScreenCoordinates() {
        getPdfToPageTransformation().mapRect(screenCoordinates, pageCoordinates)

        // Rounding out ensures no clipping of content.
        val bounds = bounds
        screenCoordinates.roundOut(bounds)
        this.bounds = bounds
    }

    /**
     * This method performs all the drawing required by this drawable.
     * Keep this method fast to maintain a performant UI.
     */
    override fun draw(canvas: Canvas) {
        val bounds = bounds.toRectF()
        canvas.save()

        // Rotate canvas by 45 degrees.
        canvas.rotate(-45f, bounds.left, bounds.bottom)
        // Recalculate text size to much new bounds.
      //  setTextSizeForWidth(redPaint, bounds.width(), text)
        // Draw the text on the rotated canvas.
        canvas.drawText(text, bounds.left, bounds.bottom, redPaint)

        canvas.restore()
    }

    private fun setTextSizeForWidth(
        paint: Paint,
        desiredWidth: Float,
        text: String
    ) {
        // Pick a reasonably large value for the test.
        val testTextSize = 40f

        // Get the bounds of the text using `testTextSize`.
        paint.textSize = testTextSize

        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)

        // Calculate the desired size as a proportion of `testTextSize`.
        val desiredTextSize = testTextSize * desiredWidth / bounds.width()

        // Set the paint for that size.
        paint.textSize = desiredTextSize
    }

    /**
     * PSPDFKit calls this method every time the page was moved or resized on the screen.
     * It will provide a fresh transformation for calculating screen coordinates from
     * PDF coordinates.
     */
    override fun updatePDFToViewTransformation(matrix: Matrix) {
        super.updatePDFToViewTransformation(matrix)
        updateScreenCoordinates()
    }

    @UiThread
    override fun setAlpha(alpha: Int) {
        redPaint.alpha = alpha
        invalidateSelf()
    }

    @UiThread
    override fun setColorFilter(colorFilter: ColorFilter?) {
        redPaint.colorFilter = colorFilter
        invalidateSelf()
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }
}
