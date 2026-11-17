package app.sadhan.ui.tools

import android.os.SystemClock
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import app.sadhan.ui.ResultCard
import app.sadhan.ui.ToolColumn
import kotlinx.coroutines.delay
import java.util.Locale

fun elapsedLabel(millis: Long): String {
    val hundredths = millis / 10
    return String.format(Locale.ROOT, "%02d:%02d:%02d.%02d", hundredths / 360000, hundredths / 6000 % 60, hundredths / 100 % 60, hundredths % 100)
}

@Composable
fun StopwatchScreen() {
    var running by remember { mutableStateOf(false) }
    var accumulated by remember { mutableLongStateOf(0L) }
    var started by remember { mutableLongStateOf(0L) }
    var elapsed by remember { mutableLongStateOf(0L) }
    val laps = remember { mutableStateListOf<Long>() }
    LaunchedEffect(running) {
        while (running) {
            elapsed = accumulated + SystemClock.elapsedRealtime() - started
            delay(31)
        }
    }
    ToolColumn {
        ResultCard("Stopwatch", elapsedLabel(elapsed))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                if (running) {
                    accumulated += SystemClock.elapsedRealtime() - started
                    elapsed = accumulated
                } else started = SystemClock.elapsedRealtime()
                running = !running
            }) { Text(if (running) "Pause" else "Start") }
            OutlinedButton(onClick = {
                laps.add(accumulated + SystemClock.elapsedRealtime() - started)
            }, enabled = running && laps.size < 100) { Text("Lap") }
            OutlinedButton(onClick = { running = false; accumulated = 0; elapsed = 0; laps.clear() }) { Text("Reset") }
        }
        Text("Session only. Leaving this tool resets it.")
        laps.forEachIndexed { index, lap -> Text("Lap ${index + 1}   ${elapsedLabel(lap)}") }
    }
}
