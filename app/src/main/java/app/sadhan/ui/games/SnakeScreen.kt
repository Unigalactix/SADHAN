package app.sadhan.ui.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.sadhan.games.*
import app.sadhan.ui.ReportTabWork
import kotlinx.coroutines.delay
import kotlin.math.abs

@Composable
fun SnakeScreen() {
    var game by remember { mutableStateOf(SnakeGame()) }
    val running = rememberGameRunning()
    ReportTabWork("snake", if (running.value || game != SnakeGame()) "Your Snake progress will be erased." else null)
    var speed by remember { mutableFloatStateOf(3f) }
    val colors = MaterialTheme.colorScheme
    LaunchedEffect(running.value, game.ended, speed) {
        while (running.value && !game.ended) { delay((340 - speed * 45).toLong()); game = game.step() }
    }
    GamePage(
        pause = { running.value = false },
        help = "Eat the orange food. Avoid walls and your own body. Swipe or use the directional controls. Speed ranges from 1 to 5.",
        status = {
            Text(if (game.ended) "Game over" else "Score ${game.score}", style = MaterialTheme.typography.titleLarge)
            Text("Length ${game.body.size} · Speed ${speed.toInt()}", color = colors.secondary)
        },
        board = { modifier ->
            Canvas(modifier.semantics { contentDescription = "Snake board, length ${game.body.size}, score ${game.score}" }.pointerInput(Unit) {
                var movement = Offset.Zero
                detectDragGestures(onDragStart = { movement = Offset.Zero }, onDragEnd = {
                    game = game.turn(if (abs(movement.x) > abs(movement.y)) { if (movement.x > 0) Direction.RIGHT else Direction.LEFT } else if (movement.y > 0) Direction.DOWN else Direction.UP)
                }) { change, amount -> change.consume(); movement += amount }
            }) {
                val cell = size.minDimension / 20
                val origin = center - Offset(cell * 10, cell * 10)
                drawRect(colors.surfaceContainerLow, origin, Size(cell * 20, cell * 20))
                game.body.forEachIndexed { index, point -> drawRect(if (index == 0) colors.secondary else colors.secondary.copy(alpha = .55f), origin + Offset(point.x * cell + 1, point.y * cell + 1), Size(cell - 2, cell - 2)) }
                drawCircle(colors.primary, cell * .38f, origin + Offset((game.food.x + .5f) * cell, (game.food.y + .5f) * cell))
            }
        },
        controls = {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Left" to Direction.LEFT, "Up" to Direction.UP, "Down" to Direction.DOWN, "Right" to Direction.RIGHT).forEach { (label, direction) ->
                        FilledTonalButton(onClick = { game = game.turn(direction) }, contentPadding = PaddingValues(8.dp), modifier = Modifier.weight(1f)) { Text(label) }
                    }
                }
                Slider(speed, { speed = it }, valueRange = 1f..5f, steps = 3)
            }
        },
        footer = { GameActions(running, game.ended) { game = SnakeGame() } },
    )
}
