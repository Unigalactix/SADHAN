package app.sadhan.ui.games

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.Alignment
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.sadhan.ui.LocalToolVisible

@Composable
fun rememberGameRunning(): MutableState<Boolean> {
    val running = remember { mutableStateOf(false) }
    val lifecycle by LocalLifecycleOwner.current.lifecycle.currentStateFlow.collectAsState()
    val active = LocalToolVisible.current && lifecycle.isAtLeast(Lifecycle.State.RESUMED)
    LaunchedEffect(active) { if (!active) running.value = false }
    return running
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameActions(running: MutableState<Boolean>, ended: Boolean, restart: () -> Unit) {
    LaunchedEffect(ended) { if (ended) running.value = false }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(enabled = !ended, onClick = { running.value = !running.value }) { Text(if (running.value) "Pause" else "Start") }
        OutlinedButton(onClick = { running.value = false; restart() }) { Text("New game") }
    }
}

@Composable
fun GamePage(
    modifier: Modifier = Modifier,
    pause: (() -> Unit)? = null,
    help: String,
    status: @Composable () -> Unit,
    board: @Composable (Modifier) -> Unit,
    controls: @Composable () -> Unit = {},
    footer: @Composable () -> Unit,
) {
    var rules by remember { mutableStateOf(false) }
    val header: @Composable () -> Unit = {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { status() }
            IconButton(onClick = { pause?.invoke(); rules = true }) { Icon(Icons.Default.Info, "Game rules") }
        }
    }
    BoxWithConstraints(modifier.fillMaxSize().padding(16.dp)) {
        val controlsMaxHeight = maxHeight * .35f
        if (maxWidth >= 560.dp && (maxHeight < 560.dp || maxWidth > maxHeight)) {
            Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Box(Modifier.weight(1.35f).fillMaxHeight(), contentAlignment = Alignment.Center) { board(Modifier.fillMaxSize().clipToBounds()) }
                Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    header()
                    Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())) { controls() }
                    footer()
                }
            }
        } else {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                header()
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { board(Modifier.fillMaxSize().clipToBounds()) }
                Column(Modifier.fillMaxWidth().heightIn(max = controlsMaxHeight).verticalScroll(rememberScrollState())) { controls() }
                footer()
            }
        }
    }
    if (rules) AlertDialog(
        onDismissRequest = { rules = false }, title = { Text("How to play") },
        text = { Column(Modifier.verticalScroll(rememberScrollState())) { Text("$help\n\nSession only. Switching tabs pauses real-time games. Closing a tab erases its progress; no scores or game history are saved.") } },
        confirmButton = { TextButton(onClick = { rules = false }) { Text("Got it") } },
    )
}
