package app.sadhan.ui.tools

import android.media.Ringtone
import android.media.RingtoneManager
import android.os.SystemClock
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.sadhan.ui.ErrorText
import app.sadhan.ui.ResultCard
import app.sadhan.ui.ToolColumn
import app.sadhan.ui.theme.Amber
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToLong
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimerScreen() {
    val context = LocalContext.current
    val view = LocalView.current
    val owner = LocalLifecycleOwner.current
    var duration by remember { mutableLongStateOf(300_000) }
    var remaining by remember { mutableLongStateOf(duration) }
    var deadline by remember { mutableLongStateOf(0) }
    var running by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val tone = remember { mutableStateOf<Ringtone?>(null) }
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) tone.value?.stop()
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer); tone.value?.stop() }
    }
    LaunchedEffect(running) {
        while (running) {
            remaining = (deadline - SystemClock.elapsedRealtime()).coerceAtLeast(0)
            if (remaining == 0L) {
                running = false
                finished = true
                if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    try {
                        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                        val ringtone = RingtoneManager.getRingtone(context, uri)
                        if (ringtone == null) error = "No alarm sound is available"
                        else { tone.value = ringtone; ringtone.play() }
                    } catch (e: SecurityException) { error = "Cannot play alarm: ${e.message}" }
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                }
            } else delay(40)
        }
    }
    LaunchedEffect(finished) { if (finished) { delay(10_000); tone.value?.stop() } }
    fun setTime(millis: Long) {
        duration = millis.coerceIn(1000, 3_600_000)
        remaining = duration
        finished = false
        tone.value?.stop()
    }
    ToolColumn {
        ResultCard(if (finished) "Time is up" else "Timer", elapsedLabel(remaining).substringBefore('.'))
        Text("Drag the needle: one revolution is 60 minutes. Runs only while this tool is open; no background alarm.")
        Canvas(
            Modifier.fillMaxWidth().height(280.dp)
                .semantics { contentDescription = "Timer dial, drag to set up to sixty minutes" }
                .pointerInput(running) {
                    if (!running) detectDragGestures { change, _ ->
                        change.consume()
                        val angle = (atan2(change.position.y - size.height / 2f, change.position.x - size.width / 2f) + PI / 2 + 2 * PI) % (2 * PI)
                        setTime((angle / (2 * PI) * 3600).roundToLong() * 1000)
                    }
                }
                .pointerInput(running) {
                    if (!running) detectTapGestures { position ->
                        val angle = (atan2(position.y - size.height / 2f, position.x - size.width / 2f) + PI / 2 + 2 * PI) % (2 * PI)
                        setTime((angle / (2 * PI) * 3600).roundToLong() * 1000)
                    }
                }
        ) {
            val radius = size.minDimension / 2 - 18.dp.toPx()
            drawCircle(Color.DarkGray, radius, style = Stroke(8.dp.toPx()))
            val fraction = if (running) remaining.toFloat() / duration else duration.toFloat() / 3_600_000
            drawArc(Amber, -90f, fraction * 360f, false, topLeft = center - Offset(radius, radius), size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2), style = Stroke(8.dp.toPx(), cap = StrokeCap.Round))
            repeat(60) { tick ->
                val theta = tick * PI / 30 - PI / 2
                val inner = radius - if (tick % 5 == 0) 20.dp.toPx() else 10.dp.toPx()
                drawLine(Color.Gray, center + Offset((cos(theta) * inner).toFloat(), (sin(theta) * inner).toFloat()), center + Offset((cos(theta) * (radius - 6)).toFloat(), (sin(theta) * (radius - 6)).toFloat()), 2.dp.toPx())
            }
            val theta = (if (running) remaining else duration) / 3_600_000.0 * 2 * PI - PI / 2
            drawLine(Amber, center, center + Offset((cos(theta) * radius * .75).toFloat(), (sin(theta) * radius * .75).toFloat()), 4.dp.toPx(), StrokeCap.Round)
            drawCircle(Amber, 7.dp.toPx())
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(1, 5, 10, 15, 30, 60).forEach { minutes ->
                OutlinedButton(onClick = { setTime(minutes * 60_000L) }, enabled = !running) { Text("${minutes}m") }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                tone.value?.stop()
                if (running) remaining = (deadline - SystemClock.elapsedRealtime()).coerceAtLeast(0)
                else {
                    if (remaining == 0L) remaining = duration
                    deadline = SystemClock.elapsedRealtime() + remaining
                    finished = false
                }
                running = !running
            }) { Text(if (running) "Pause" else "Start") }
            OutlinedButton(onClick = { running = false; setTime(duration) }) { Text(if (finished) "Dismiss / reset" else "Reset") }
        }
        ErrorText(error)
    }
}
