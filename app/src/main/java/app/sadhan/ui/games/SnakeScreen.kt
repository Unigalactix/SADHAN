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
import androidx.compose.ui.unit.dp
import app.sadhan.games.*
import app.sadhan.ui.ResultCard
import app.sadhan.ui.ToolColumn
import kotlinx.coroutines.delay
import kotlin.math.abs

@Composable
fun SnakeScreen() {
    var game by remember { mutableStateOf(SnakeGame()) }
    val running = rememberGameRunning()
    var speed by remember { mutableFloatStateOf(3f) }
    val colors = MaterialTheme.colorScheme
    LaunchedEffect(running.value, game.ended, speed) {
        while (running.value && !game.ended) { delay((340 - speed * 45).toLong()); game = game.step() }
    }
    ToolColumn {
        ResultCard(if (game.ended) "Game over" else "Snake", "Score ${game.score}")
        Canvas(gameBoardModifier().pointerInput(Unit) {
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
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            OutlinedButton(onClick = { game = game.turn(Direction.UP) }) { Text("↑") }
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                OutlinedButton(onClick = { game = game.turn(Direction.LEFT) }) { Text("←") }
                OutlinedButton(onClick = { game = game.turn(Direction.DOWN) }) { Text("↓") }
                OutlinedButton(onClick = { game = game.turn(Direction.RIGHT) }) { Text("→") }
            }
        }
        Text("Speed ${speed.toInt()} · Swipe the board or use the arrows.")
        Slider(speed, { speed = it }, valueRange = 1f..5f, steps = 3)
        GameActions(running, game.ended) { game = SnakeGame() }
    }
}
