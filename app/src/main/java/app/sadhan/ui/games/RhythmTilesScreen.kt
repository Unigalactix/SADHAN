package app.sadhan.ui.games

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import app.sadhan.games.RhythmTilesGame
import app.sadhan.ui.ErrorText
import app.sadhan.ui.ResultCard
import app.sadhan.ui.ToolColumn
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

@Composable
fun RhythmTilesScreen() {
    var game by remember { mutableStateOf(RhythmTilesGame()) }
    val running = rememberGameRunning()
    var sound by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val track = remember { mutableStateOf<AudioTrack?>(null) }
    val colors = MaterialTheme.colorScheme
    fun note() {
        if (!sound) return
        try {
            track.value?.release()
            track.value = null
            val frequency = listOf(261.63, 293.66, 329.63, 392.0, 440.0)[game.score % 5]
            val samples = ShortArray(4000) { sample ->
                val t = sample / 16000.0
                ((sin(2 * PI * frequency * t) + .25 * sin(4 * PI * frequency * t)) * exp(-12 * t) * 7000).toInt().toShort()
            }
            val audio = AudioTrack.Builder().setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(16000).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                .setTransferMode(AudioTrack.MODE_STATIC).setBufferSizeInBytes(samples.size * 2).build()
            track.value = audio
            check(audio.write(samples, 0, samples.size) == samples.size) { "Audio output rejected the generated note" }
            audio.play()
        } catch (e: IllegalArgumentException) { error = "Sound unavailable: ${e.message}" }
        catch (e: IllegalStateException) { error = "Sound unavailable: ${e.message}" }
        catch (e: UnsupportedOperationException) { error = "Sound unavailable: ${e.message}" }
    }
    DisposableEffect(Unit) { onDispose { track.value?.release() } }
    LaunchedEffect(running.value, game.ended) {
        if (!running.value) track.value?.pause()
        while (running.value && !game.ended) { delay(100); game = game.step(.1f) }
    }
    ToolColumn {
        ResultCard(if (game.ended) "Round complete" else "Rhythm Tiles", "${game.score} tiles · ${game.remaining.toInt()} seconds")
        Canvas(gameBoardModifier().pointerInput(running.value) {
            detectTapGestures { point ->
                if (running.value && !game.ended) {
                    val previous = game.score
                    game = game.tap((point.x / size.width * 4).toInt().coerceIn(0, 3), (point.y / size.height * 4).toInt().coerceIn(0, 3))
                    if (game.score > previous) note()
                }
            }
        }) {
            val width = size.width / 4
            val height = size.height / 4
            repeat(4) { row -> repeat(4) { column ->
                drawRect(if (game.rows[row] == column) { if (row == 3) colors.primary else colors.secondary } else colors.surfaceContainerLow,
                    Offset(column * width + 2, row * height + 2), Size(width - 4, height - 4))
            } }
        }
        Row { Text("Procedural piano tones", Modifier.weight(1f)); Switch(sound, { sound = it; if (!it) track.value?.pause() }) }
        Text("Tap the colored tile in the bottom row. A wrong tile ends the round. Thirty seconds, original generated notes, no downloaded music.")
        ErrorText(error)
        GameActions(running, game.ended) { game = RhythmTilesGame() }
    }
}
