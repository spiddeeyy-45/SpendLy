package Util.Home

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.min


class DonutChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    data class DonutSlice(
        val label: String,
        val value: Float,
        val color: String
    )

    private var slices: List<DonutSlice> = emptyList()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val oval = RectF()
    private val gapDeg = 2f

    fun setData(data: List<DonutSlice>) {
        slices = data
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (slices.isEmpty()) return

        val w = width.toFloat()
        val h = height.toFloat()
        val size = min(w, h)

        val strokeW = size * 0.18f
        val radius   = (size - strokeW) / 2f
        val cx = w / 2f
        val cy = h / 2f

        oval.set(cx - radius, cy - radius, cx + radius, cy + radius)

        val total = slices.sumOf { it.value.toDouble() }.toFloat()
        val totalGap = gapDeg * slices.size
        val sweepScale = (360f - totalGap) / total

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeW
        paint.strokeCap = Paint.Cap.BUTT

        // Background track
        paint.color = Color.parseColor("#1AFFFFFF")
        canvas.drawOval(oval, paint)

        // Start from top (-90°)
        var startAngle = -90f

        slices.forEach { slice ->
            val sweep = slice.value * sweepScale
            paint.color = Color.parseColor(slice.color)
            canvas.drawArc(oval, startAngle, sweep, false, paint)
            startAngle += sweep + gapDeg
        }
    }
}