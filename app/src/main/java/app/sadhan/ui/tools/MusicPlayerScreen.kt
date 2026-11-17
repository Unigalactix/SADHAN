package app.sadhan.ui.tools

import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.sadhan.media.documentName
import app.sadhan.ui.ErrorText
import app.sadhan.ui.ResultCard
import app.sadhan.ui.ToolColumn
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException

@Composable
fun MusicPlayerScreen() {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val audioManager = remember { context.getSystemService(AudioManager::class.java) }
    var player by remember { mutableStateOf<MediaPlayer?>(null) }
    var title by remember { mutableStateOf("Choose a local audio file") }
    var ready by remember { mutableStateOf(false) }
    var playing by remember { mutableStateOf(false) }
    var position by remember { mutableFloatStateOf(0f) }
    var duration by remember { mutableFloatStateOf(0f) }
    var error by remember { mutableStateOf<String?>(null) }
    val attributes = remember { AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build() }
    val focus = remember {
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).setAudioAttributes(attributes).setOnAudioFocusChangeListener { change ->
            if (change <= 0 && ready) { player?.pause(); playing = false }
        }.build()
    }
    fun release() {
        ready = false
        playing = false
        player?.release()
        player = null
        audioManager.abandonAudioFocusRequest(focus)
    }
    fun load(uri: Uri) {
        release()
        error = null
        position = 0f
        duration = 0f
        try {
            title = documentName(context, uri)
            val media = MediaPlayer()
            player = media
            media.setAudioAttributes(attributes)
            media.setOnPreparedListener {
                if (player === it) { duration = it.duration.toFloat().coerceAtLeast(0f); ready = true }
            }
            media.setOnCompletionListener { playing = false; position = duration }
            media.setOnErrorListener { _, what, extra ->
                error = "Cannot play this audio (code $what/$extra). Try a supported local file."
                release()
                true
            }
            media.setDataSource(context, uri)
            media.prepareAsync()
        } catch (e: IOException) { error = "Cannot open audio: ${e.message}"; release() }
        catch (e: SecurityException) { error = "Audio access denied: ${e.message}"; release() }
        catch (e: IllegalArgumentException) { error = "Unsupported audio: ${e.message}"; release() }
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> if (uri != null) load(uri) }
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP && ready) {
                player?.pause()
                playing = false
                audioManager.abandonAudioFocusRequest(focus)
            }
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer); release() }
    }
    LaunchedEffect(playing) {
        while (playing) { position = player?.currentPosition?.toFloat() ?: 0f; delay(200) }
    }
    ToolColumn {
        Text("On-device audio, no library scanning or history. Playback pauses in the background and stops when you leave.")
        Button(onClick = { picker.launch(arrayOf("audio/*")) }) { Text("Choose audio") }
        ResultCard("Local music player", title)
        ErrorText(error)
        if (ready) {
            Text("${elapsedLabel(position.toLong()).substringBefore('.')} / ${elapsedLabel(duration.toLong()).substringBefore('.')}")
            Slider(value = position.coerceIn(0f, duration.coerceAtLeast(1f)), onValueChange = { position = it; player?.seekTo(it.toInt()) }, valueRange = 0f..duration.coerceAtLeast(1f))
            Button(onClick = {
                if (playing) {
                    player?.pause()
                    playing = false
                    audioManager.abandonAudioFocusRequest(focus)
                } else if (audioManager.requestAudioFocus(focus) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    if (position >= duration) player?.seekTo(0)
                    player?.start()
                    playing = true
                } else error = "Audio is in use by another app. Try again."
            }) { Text(if (playing) "Pause" else "Play") }
            OutlinedButton(onClick = { player?.pause(); player?.seekTo(0); playing = false; position = 0f; audioManager.abandonAudioFocusRequest(focus) }) { Text("Restart") }
        } else if (player != null) Text("Preparing audio...")
    }
}
