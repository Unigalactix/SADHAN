package app.sadhan.ui.tools

import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.sadhan.ui.ErrorText
import app.sadhan.ui.ResultCard
import app.sadhan.ui.SensorSession
import app.sadhan.ui.ToolColumn
import app.sadhan.ui.theme.Amber
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun CompassScreen() {
    val context = LocalContext.current
    val foreground = MaterialTheme.colorScheme.onSurface.toArgb()
    val accent = MaterialTheme.colorScheme.primary.toArgb()
    val manager = remember { context.getSystemService(SensorManager::class.java) }
    val rotation = remember { manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) }
    val sensors = remember {
        if (rotation != null) listOf(rotation)
        else listOfNotNull(manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD))
    }
    val supported = rotation != null || sensors.size == 2
    var heading by remember { mutableFloatStateOf(0f) }
    var received by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val gravity = remember { FloatArray(3) }
    val magnetic = remember { FloatArray(3) }
    val matrix = remember { FloatArray(9) }
    val orientation = remember { FloatArray(3) }
    if (supported) SensorSession(manager, sensors, { error = it }) { event ->
        val valid = if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(matrix, event.values)
            true
        } else {
            val target = if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) gravity else magnetic
            repeat(3) { target[it] = .8f * target[it] + .2f * event.values[it] }
            SensorManager.getRotationMatrix(matrix, null, gravity, magnetic)
        }
        if (valid) {
            SensorManager.getOrientation(matrix, orientation)
            heading = ((Math.toDegrees(orientation[0].toDouble()) + 360) % 360).toFloat()
            received = true
        }
    }
    ToolColumn {
        if (!supported) ResultCard("Compass", "Compass sensors are unavailable on this device")
        else {
            ResultCard("Magnetic heading", if (received) "${heading.roundToInt() % 360}°" else "Waiting for sensor...")
            Canvas(Modifier.fillMaxWidth().height(300.dp)) {
                val radius = size.minDimension / 2 - 30.dp.toPx()
                drawCircle(Color.DarkGray, radius, style = Stroke(2.dp.toPx()))
                drawLine(Amber, Offset(center.x, center.y - radius - 18.dp.toPx()), Offset(center.x, center.y - radius + 8.dp.toPx()), 4.dp.toPx())
                rotate(-heading) {
                    repeat(36) {
                        val theta = it * PI / 18
                        drawLine(Color.Gray, center + Offset((cos(theta) * radius).toFloat(), (sin(theta) * radius).toFloat()), center + Offset((cos(theta) * (radius - 10.dp.toPx())).toFloat(), (sin(theta) * (radius - 10.dp.toPx())).toFloat()), 2.dp.toPx())
                    }
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER; textSize = 22.dp.toPx() }
                    listOf("N", "E", "S", "W").forEachIndexed { index, text ->
                        val theta = index * PI / 2 - PI / 2
                        paint.color = if (index == 0) accent else foreground
                        drawContext.canvas.nativeCanvas.drawText(text, center.x + (cos(theta) * radius * .72).toFloat(), center.y + (sin(theta) * radius * .72).toFloat() + paint.textSize / 3, paint)
                    }
                    drawLine(Amber, center, Offset(center.x, center.y - radius * .5f), 5.dp.toPx())
                }
            }
            Text("Keep the phone flat, away from metal and magnets. Move it in a figure-eight to calibrate. Not for safety-critical navigation.")
        }
        ErrorText(error)
    }
}
