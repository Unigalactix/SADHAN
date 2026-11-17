package app.sadhan.ui.games

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import app.sadhan.games.RhythmTilesGame
import app.sadhan.media.GameTone
import app.sadhan.ui.ErrorText
import app.sadhan.ui.ReportTabWork
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RhythmTilesScreen() {
    var game by remember { mutableStateOf(RhythmTilesGame()) }
    val running = rememberGameRunning()
    var sound by remember { mutableStateOf(true) }
    var practice by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val tone = remember { GameTone(context) }
    val colors = MaterialTheme.colorScheme
    ReportTabWork("rhythm", if (running.value || game.score > 0 || game.remaining < 30f) "Your rhythm round will be erased." else null)
    fun tap(column: Int) {
        if (!running.value || game.ended) return
        val previous = game.score
        game = game.tap(column, 3)
        if (sound && game.score > previous) {
            try { tone.play(column) }
            catch (failure: IllegalStateException) { error = failure.message; tone.stop() }
            catch (failure: IllegalArgumentException) { error = "Sound unavailable: ${failure.message}"; tone.stop() }
            catch (failure: UnsupportedOperationException) { error = "Sound unavailable: ${failure.message}"; tone.stop() }
        }
    }
    DisposableEffect(tone) { onDispose { tone.stop() } }
    LaunchedEffect(running.value, game.ended, practice) {
        if (!running.value) tone.stop()
        while (running.value && !game.ended && !practice) { delay(100); game = game.step(.1f) }
    }
    GamePage(
        pause = { running.value = false },
        modifier = Modifier.onPreviewKeyEvent { event ->
            val column = when (event.key) { Key.One -> 0; Key.Two -> 1; Key.Three -> 2; Key.Four -> 3; else -> null }
            if (column != null && event.type == KeyEventType.KeyDown) { tap(column); true } else false
        },
        help = "Play the highlighted bottom-row column. A wrong column ends the round. Use touch, Tab/D-pad and activation, or keyboard keys 1–4. Practice removes the timer; changing modes starts a new round.",
        status = {
            Text(if (game.ended) "Round complete · ${game.score}" else "${game.score} tiles", style = MaterialTheme.typography.titleLarge)
            Text(if (practice) "Practice · No timer" else "${game.remaining.toInt()} seconds", color = colors.secondary)
        },
        board = { modifier ->
            Column(modifier) {
                repeat(4) { row ->
                    Row(Modifier.weight(1f).fillMaxWidth()) {
                        repeat(4) { column ->
                            val highlighted = game.rows[row] == column
                            val semantics = if (row == 3) Modifier.semantics {
                                contentDescription = "Column ${column + 1}, ${if (highlighted) "play tile" else "empty"}"
                                role = Role.Button
                            }.clickable(enabled = running.value && !game.ended) { tap(column) } else Modifier.clearAndSetSemantics {}
                            Surface(Modifier.weight(1f).fillMaxHeight().padding(2.dp).then(semantics),
                                color = if (highlighted) { if (row == 3) colors.primary else colors.secondary } else colors.surfaceContainerLow,
                                shape = MaterialTheme.shapes.small,
                            ) { Box(contentAlignment = Alignment.Center) { if (highlighted) Text("♪", color = if (row == 3) colors.onPrimary else colors.onSecondary) } }
                        }
                    }
                }
            }
        },
        controls = {
            Row(Modifier.fillMaxWidth()) {
                repeat(4) { column ->
                    OutlinedButton(enabled = running.value, onClick = { tap(column) }, contentPadding = PaddingValues(4.dp), modifier = Modifier.weight(1f).semantics {
                        contentDescription = "Play column ${column + 1}${if (game.rows.last() == column) ", highlighted" else ""}"
                    }) { Text((column + 1).toString()) }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Practice · starts a new round", Modifier.weight(1f))
                Switch(practice, { practice = it; running.value = false; game = RhythmTilesGame(); error = null },
                    modifier = Modifier.semantics { contentDescription = "Untimed practice" })
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Piano sound", Modifier.weight(1f)); Switch(sound, { sound = it; error = null; if (!it) tone.stop() },
                    modifier = Modifier.semantics { contentDescription = "Piano sound" })
            }
            ErrorText(error)
        },
        footer = { GameActions(running, game.ended) { game = RhythmTilesGame(); error = null } },
    )
}
