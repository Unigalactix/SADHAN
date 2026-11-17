package app.sadhan.ui.games

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalConfiguration
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

@Composable
fun gameBoardModifier(): Modifier = Modifier.fillMaxWidth().height(if (LocalConfiguration.current.screenHeightDp < 500) 210.dp else 340.dp).clipToBounds()

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameActions(running: MutableState<Boolean>, ended: Boolean, restart: () -> Unit) {
    LaunchedEffect(ended) { if (ended) running.value = false }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(enabled = !ended, onClick = { running.value = !running.value }) { Text(if (running.value) "Pause" else "Start") }
        OutlinedButton(onClick = { running.value = false; restart() }) { Text("New game") }
    }
    Text("Session only. Switching tabs pauses the game; closing its tab erases progress.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}
