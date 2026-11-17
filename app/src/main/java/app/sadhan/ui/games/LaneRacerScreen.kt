package app.sadhan.ui.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.sadhan.games.*
import app.sadhan.ui.ReportTabWork
import kotlinx.coroutines.delay

@Composable
fun LaneRacerScreen() {
    var game by remember { mutableStateOf(LaneRacerGame()) }
    val running = rememberGameRunning()
    ReportTabWork("race", if (running.value || game.distance > 0) "Your race progress will be erased." else null)
    val colors = MaterialTheme.colorScheme
    LaunchedEffect(running.value, game.ended) {
        while (running.value && !game.ended) { delay(50); game = game.step(.05f) }
    }
    GamePage(
        pause = { running.value = false },
        help = "Use Left and Right to dodge traffic. Speed increases with distance. A collision ends the run.",
        status = { Text(if (game.ended) "Collision · ${game.distance.toInt()}" else "Distance ${game.distance.toInt()}", style = MaterialTheme.typography.titleLarge) },
        board = { modifier -> Canvas(modifier) {
            val roadWidth = minOf(size.width, size.height * .7f)
            val left = (size.width - roadWidth) / 2
            val lane = roadWidth / 3
            val unit = size.height / 20
            drawRect(Color(0xFF102435), Offset(left, 0f), Size(roadWidth, size.height))
            for (separator in 1..2) repeat(12) {
                val y = ((it * 2f + game.distance % 2) * unit) - unit * 2
                drawRect(Color.White.copy(alpha = .4f), Offset(left + separator * lane - 1, y), Size(2f, unit))
            }
            fun car(column: Int, y: Float, color: Color) {
                val point = Offset(left + column * lane + lane * .2f, y * unit)
                drawRoundRect(color, point, Size(lane * .6f, unit * 1.5f), androidx.compose.ui.geometry.CornerRadius(8f))
                drawRect(Color(0xFF081525), point + Offset(lane * .08f, unit * .25f), Size(lane * .44f, unit * .35f))
            }
            game.cars.forEach { car(it.lane, it.y, colors.primary) }
            car(game.lane, 17f, colors.secondary)
        } },
        controls = {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(enabled = running.value, onClick = { game = game.steer(-1) }, modifier = androidx.compose.ui.Modifier.weight(1f)) { Text("Left") }
                Button(enabled = running.value, onClick = { game = game.steer(1) }, modifier = androidx.compose.ui.Modifier.weight(1f)) { Text("Right") }
            }
        },
        footer = { GameActions(running, game.ended) { game = LaneRacerGame() } },
    )
}
