package app.sadhan.ui.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.sadhan.games.*
import app.sadhan.ui.ResultCard
import app.sadhan.ui.ToolColumn

@Composable
fun PixelRunnerScreen() {
    var game by remember { mutableStateOf(PixelRunnerGame()) }
    val running = rememberGameRunning()
    val left = remember { MutableInteractionSource() }
    val right = remember { MutableInteractionSource() }
    val pressedLeft by left.collectIsPressedAsState()
    val pressedRight by right.collectIsPressedAsState()
    var jumping by remember { mutableStateOf(false) }
    val direction by rememberUpdatedState((if (pressedRight) 1 else 0) - if (pressedLeft) 1 else 0)
    val colors = MaterialTheme.colorScheme
    LaunchedEffect(running.value, game.ended, game.won) {
        var previous = withFrameNanos { it }
        while (running.value && !game.ended && !game.won) {
            val time = withFrameNanos { it }
            game = game.step((time - previous) / 1_000_000_000f, direction, jumping)
            jumping = false
            previous = time
        }
    }
    ToolColumn {
        ResultCard(when { game.won -> "Level complete!"; game.ended -> "Game over"; else -> "Pixel Runner" }, "${game.lives} lives · ${game.stars.size}/${runnerStars.size} stars")
        Canvas(gameBoardModifier()) {
            val unit = minOf(size.width / 16, size.height / 11)
            val origin = center - Offset(unit * 8, unit * 5.5f)
            val camera = (game.x - 6).coerceIn(0f, 46f)
            fun point(x: Float, y: Float) = origin + Offset((x - camera) * unit, y * unit)
            drawRect(Color(0xFF0B243C), origin, Size(unit * 16, unit * 11))
            runnerPlatforms.forEach { drawRect(colors.secondary.copy(alpha = .7f), point(it.x, it.y), Size(it.width * unit, unit * .45f)) }
            runnerStars.forEachIndexed { index, star -> if (index !in game.stars) drawCircle(colors.primary, unit * .22f, point(star.x.toFloat(), star.y.toFloat())) }
            drawRect(colors.primary, point(game.x, game.y), Size(unit * .7f, unit * .9f))
            drawRect(Color.White, point(game.x + .45f, game.y + .15f), Size(unit * .12f, unit * .12f))
            drawRect(colors.secondary, point(59f, 5f), Size(unit * .12f, unit * 4))
            drawRect(colors.primary, point(59f, 5f), Size(unit, unit * .65f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(enabled = running.value, onClick = {}, interactionSource = left) { Text("Hold ←") }
            OutlinedButton(enabled = running.value, onClick = {}, interactionSource = right) { Text("Hold →") }
            Button(enabled = running.value, onClick = { jumping = true }) { Text("Jump") }
        }
        Text("Hold a direction, jump gaps, collect stars, and reach the flag. Original procedural platformer; no borrowed game characters or artwork.")
        GameActions(running, game.ended || game.won) { game = PixelRunnerGame() }
    }
}
