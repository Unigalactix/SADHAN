package app.sadhan.ui.tools

import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.sadhan.ui.ErrorText
import app.sadhan.ui.ResultCard
import app.sadhan.ui.SensorSession
import app.sadhan.ui.ToolColumn
import app.sadhan.ui.theme.Amber
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.sqrt

@Composable
fun LevelScreen() {
    val context = LocalContext.current
    val manager = remember { context.getSystemService(SensorManager::class.java) }
    val sensors = remember { listOfNotNull(manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)) }
    var x by remember { mutableFloatStateOf(0f) }
    var y by remember { mutableFloatStateOf(0f) }
    var z by remember { mutableFloatStateOf(9.81f) }
    var error by remember { mutableStateOf<String?>(null) }
    if (sensors.isNotEmpty()) SensorSession(manager, sensors, { error = it }) { event ->
        x = .8f * x + .2f * event.values[0]
        y = .8f * y + .2f * event.values[1]
        z = .8f * z + .2f * event.values[2]
    }
    ToolColumn {
        if (sensors.isEmpty()) ResultCard("Bubble Level", "Accelerometer unavailable on this device")
        else {
            val tiltX = Math.toDegrees(atan2(x.toDouble(), sqrt((y * y + z * z).toDouble())))
            val tiltY = Math.toDegrees(atan2(y.toDouble(), sqrt((x * x + z * z).toDouble())))
            ResultCard("Tilt", String.format(Locale.getDefault(), "X %.1f°    Y %.1f°", tiltX, tiltY))
            Canvas(Modifier.fillMaxWidth().height(300.dp)) {
                val radius = size.minDimension / 2 - 20.dp.toPx()
                drawCircle(Color.DarkGray, radius, style = Stroke(3.dp.toPx()))
                drawCircle(Color.Gray, radius * .2f, style = Stroke(2.dp.toPx()))
                drawLine(Color.Gray, Offset(center.x - radius, center.y), Offset(center.x + radius, center.y))
                drawLine(Color.Gray, Offset(center.x, center.y - radius), Offset(center.x, center.y + radius))
                var offset = Offset(-x / 9.81f, y / 9.81f)
                if (offset.getDistance() > 1f) offset /= offset.getDistance()
                drawCircle(Amber, radius * .13f, center + offset * radius * .85f)
            }
            Text("Lay the phone flat on the surface. Sensor accuracy varies by device; not a calibrated measuring instrument.")
        }
        ErrorText(error)
    }
}
