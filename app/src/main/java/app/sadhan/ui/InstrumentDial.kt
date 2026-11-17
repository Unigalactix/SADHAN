package app.sadhan.ui

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun InstrumentDial(seconds: Float, modifier: Modifier, hours: Float? = null) {
    val accent = MaterialTheme.colorScheme.primary
    val cyan = MaterialTheme.colorScheme.secondary
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    Canvas(modifier) {
        val small = size.minDimension < 120.dp.toPx()
        val radius = size.minDimension / 2 - (if (small) 5.dp else 20.dp).toPx()
        fun point(value: Float, distance: Float): Offset {
            val angle = value / 60 * 2 * PI - PI / 2
            return center + Offset((cos(angle) * distance).toFloat(), (sin(angle) * distance).toFloat())
        }
        repeat(60) { tick ->
            drawLine(cyan.copy(alpha = if (tick % 5 == 0) .85f else .35f), point(tick.toFloat(), radius), point(tick.toFloat(), radius - if (small) 3.dp.toPx() else if (tick % 5 == 0) 12.dp.toPx() else 5.dp.toPx()), (if (small) 1.dp else 2.dp).toPx(), StrokeCap.Round)
        }
        if (size.minDimension >= 200.dp.toPx()) {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = textColor; textAlign = Paint.Align.CENTER; textSize = 13.dp.toPx() }
            repeat(12) {
                val location = point((it * 5).toFloat(), radius * .78f)
                drawContext.canvas.nativeCanvas.drawText(if (it == 0) "00" else (it * 5).toString(), location.x, location.y + paint.textSize / 3, paint)
            }
        }
        hours?.let { drawLine(cyan, center, point(it * 5, radius * .5f), 4.dp.toPx(), StrokeCap.Round) }
        drawLine(accent, point(seconds + 30, radius * .12f), point(seconds, radius * .7f), 3.dp.toPx(), StrokeCap.Round)
        drawCircle(accent, 6.dp.toPx())
    }
}
