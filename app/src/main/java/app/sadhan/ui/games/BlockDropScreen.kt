package app.sadhan.ui.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import app.sadhan.games.*
import app.sadhan.ui.ResultCard
import app.sadhan.ui.ToolColumn
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BlockDropScreen() {
    var game by remember { mutableStateOf(BlockDropGame.new()) }
    val running = rememberGameRunning()
    val colors = listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary, androidx.compose.ui.graphics.Color(0xFFA68CE8), androidx.compose.ui.graphics.Color(0xFF62B892), androidx.compose.ui.graphics.Color(0xFFD66B86), androidx.compose.ui.graphics.Color(0xFF718BD2), androidx.compose.ui.graphics.Color(0xFFD4B76B))
    val background = MaterialTheme.colorScheme.surfaceContainerLow
    LaunchedEffect(running.value, game.ended, game.level) {
        while (running.value && !game.ended) { delay((700L - (game.level - 1) * 55).coerceAtLeast(100)); game = game.step() }
    }
    ToolColumn {
        ResultCard(if (game.ended) "Game over" else "Block Drop", "${game.score} points · ${game.lines} lines · Level ${game.level}")
        Canvas(gameBoardModifier()) {
            val cell = minOf(size.width / 10, size.height / 18)
            val origin = center - Offset(cell * 5, cell * 9)
            drawRect(background, origin, Size(cell * 10, cell * 18))
            fun block(x: Int, y: Int, type: Int) = drawRect(colors[(type - 1) % colors.size], origin + Offset(x * cell + 1, y * cell + 1), Size(cell - 2, cell - 2))
            game.board.forEachIndexed { index, value -> if (value > 0) block(index % 10, index / 10, value) }
            if (!game.ended) game.piece.cells.forEach { block(game.piece.x + it.x, game.piece.y + it.y, game.piece.type + 1) }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(enabled = running.value, onClick = { game = game.move(-1) }) { Text("←") }
            OutlinedButton(enabled = running.value, onClick = { game = game.rotate() }) { Text("Rotate") }
            OutlinedButton(enabled = running.value, onClick = { game = game.move(1) }) { Text("→") }
            OutlinedButton(enabled = running.value, onClick = { game = game.step() }) { Text("↓") }
            Button(enabled = running.value, onClick = { game = game.drop() }) { Text("Drop") }
        }
        Text("Next: ${listOf("Bar", "Square", "Tee", "Zig", "Zag", "Hook", "Corner")[game.next]}. Clear full rows. Original 10 × 18 falling-block game with seven-piece bags and wall kicks.")
        GameActions(running, game.ended) { game = BlockDropGame.new() }
    }
}
