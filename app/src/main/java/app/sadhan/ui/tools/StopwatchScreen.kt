package app.sadhan.ui.tools

import android.os.SystemClock
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.Modifier
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
import app.sadhan.ui.InstrumentDial
import app.sadhan.ui.LocalToolVisible
import app.sadhan.ui.ReportTabWork
import kotlinx.coroutines.delay
import java.util.Locale

fun elapsedLabel(millis: Long): String {
    val hundredths = millis / 10
    return String.format(Locale.ROOT, "%02d:%02d:%02d.%02d", hundredths / 360000, hundredths / 6000 % 60, hundredths / 100 % 60, hundredths % 100)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StopwatchScreen() {
    var running by remember { mutableStateOf(false) }
    var accumulated by remember { mutableLongStateOf(0L) }
    var started by remember { mutableLongStateOf(0L) }
    var elapsed by remember { mutableLongStateOf(0L) }
    val laps = remember { mutableStateListOf<Long>() }
    ReportTabWork("stopwatch", if (running || elapsed > 0) "The stopwatch and session laps will be reset." else null)
    val visible = LocalToolVisible.current
    LaunchedEffect(running, visible) {
        while (running && visible) {
            elapsed = accumulated + SystemClock.elapsedRealtime() - started
            delay(31)
        }
    }
    ToolColumn {
        InstrumentDial((elapsed % 60_000) / 1000f, Modifier.fillMaxWidth().height(260.dp))
        ResultCard("Stopwatch", elapsedLabel(elapsed))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
        Text("Session only, up to 100 laps. Close this tab to reset it.")
        laps.forEachIndexed { index, lap -> Text("Lap ${index + 1}   ${elapsedLabel(lap)}") }
    }
}
