@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)

package app.sadhan.ui.tools

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.C
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import app.sadhan.media.LibraryItem
import app.sadhan.media.PlaybackSession
import app.sadhan.media.documentName
import app.sadhan.media.rememberPlaybackSession
import app.sadhan.ui.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VideoPlayerScreen(fullscreen: Boolean = false, onFullscreen: (Boolean) -> Unit = {}) {
    val context = LocalContext.current
    val activity = context as android.app.Activity
    val owner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val session = rememberPlaybackSession()
    var ratio by remember { mutableStateOf("Fit") }
    var subtitleSize by remember { mutableFloatStateOf(1f) }
    var volume by remember { mutableFloatStateOf(1f) }
    var error by remember { mutableStateOf<String?>(null) }
    var dialog by remember { mutableStateOf<String?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) scope.launch {
            try {
                val items = withContext(Dispatchers.IO) { uris.map { LibraryItem(it, documentName(context, it)) } }
                withContext(Dispatchers.Main.immediate) {
                    error = null
                    session.load(items, autoplay = owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
                }
            } catch (e: IOException) { error = "Cannot read video: ${e.message}" }
            catch (e: SecurityException) { error = "Video access denied: ${e.message}" }
            catch (e: IllegalArgumentException) { error = e.message }
        }
    }
    val subtitlePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            try {
                val name = withContext(Dispatchers.IO) { documentName(context, uri) }
                withContext(Dispatchers.Main.immediate) { session.attachSubtitle(uri, name); error = null }
            } catch (e: IOException) { error = "Cannot read subtitle: ${e.message}" }
            catch (e: SecurityException) { error = "Subtitle access denied: ${e.message}" }
            catch (e: IllegalArgumentException) { error = e.message }
        }
    }
    DisposableEffect(fullscreen) {
        val controller = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        if (fullscreen) controller.hide(WindowInsetsCompat.Type.systemBars())
        onDispose { controller.show(WindowInsetsCompat.Type.systemBars()) }
    }
    DisposableEffect(Unit) { onDispose { onFullscreen(false) } }
    BackHandler(fullscreen) { onFullscreen(false) }
    val surface: @Composable (Modifier) -> Unit = { modifier -> VideoSurface(session, ratio, subtitleSize, modifier) }
    if (fullscreen) {
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            surface(Modifier.fillMaxSize())
            IconButton(onClick = { onFullscreen(false) }, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
                Icon(Icons.Default.FullscreenExit, "Exit fullscreen", tint = Color.White)
            }
            Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Color.Black.copy(alpha = .8f)).padding(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = session::playPause) { Text(if (session.playing) "Pause" else "Play") }
                    Text("${elapsedLabel(session.position).substringBefore('.')} / ${elapsedLabel(session.duration).substringBefore('.')}", color = Color.White)
                    TextButton(onClick = { session.player.seekBack() }) { Text("-10s") }
                    TextButton(onClick = { session.player.seekForward() }) { Text("+10s") }
                }
            }
        }
    } else ToolColumn {
        surface(Modifier.fillMaxWidth().aspectRatio(if (ratio == "4:3") 4f / 3 else 16f / 9))
        Button(onClick = { picker.launch(arrayOf("video/*")) }) { Text("Choose video") }
        PlaybackControls(session)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(enabled = session.queue.isNotEmpty(), onClick = { dialog = "audio" }) { Text("Audio track") }
            OutlinedButton(enabled = session.queue.isNotEmpty(), onClick = { dialog = "subtitles" }) { Text("Subtitles") }
            OutlinedButton(enabled = session.queue.isNotEmpty(), onClick = { subtitlePicker.launch(arrayOf("*/*")) }) { Text("Load subtitle file") }
            OutlinedButton(onClick = { dialog = "options" }) { Text("Playback options") }
            OutlinedButton(enabled = session.queue.isNotEmpty(), onClick = { onFullscreen(true) }) { Text("Fullscreen") }
        }
        ErrorText(error)
        Text("Local files only. Codec and embedded-track support depend on the file and device. No viewing history or background playback.", style = MaterialTheme.typography.bodySmall)
        MediaLibrary(video = true) { items, index -> session.load(items, index, true) }
        if (session.queue.size > 1) {
            Text("Video queue", style = MaterialTheme.typography.titleMedium)
            LibraryList(session.queue) { _, index -> session.player.seekTo(index, 0); session.player.play() }
        }
    }
    when (dialog) {
        "audio" -> TrackDialog(session, C.TRACK_TYPE_AUDIO) { dialog = null }
        "subtitles" -> TrackDialog(session, C.TRACK_TYPE_TEXT) { dialog = null }
        "options" -> AlertDialog(
            onDismissRequest = { dialog = null }, title = { Text("Playback options") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Choice("Screen ratio", ratio, listOf("Fit", "Fill", "Zoom", "16:9", "4:3")) { ratio = it }
                    Choice("Speed", "${session.speed}×", listOf(.25f, .5f, .75f, 1f, 1.25f, 1.5f, 2f, 3f).map { "${it}×" }) { session.player.setPlaybackSpeed(it.removeSuffix("×").toFloat()) }
                    Text("Volume ${(volume * 100).toInt()}%")
                    Slider(volume, { volume = it; session.player.volume = it }, valueRange = 0f..1f)
                    Choice("Subtitle size", when (subtitleSize) { .8f -> "Small"; 1.3f -> "Large"; else -> "Normal" }, listOf("Small", "Normal", "Large")) {
                        subtitleSize = when (it) { "Small" -> .8f; "Large" -> 1.3f; else -> 1f }
                    }
                }
            }, confirmButton = { TextButton(onClick = { dialog = null }) { Text("Done") } },
        )
    }
}

@Composable
private fun VideoSurface(session: PlaybackSession, ratio: String, subtitleSize: Float, modifier: Modifier) {
    AndroidView(
        factory = { context -> PlayerView(context).apply { useController = false; setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING) } },
        modifier = modifier.background(Color.Black),
        update = {
            it.player = session.player
            it.resizeMode = when (ratio) {
                "Fill", "16:9", "4:3" -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                "Zoom" -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
            }
            it.subtitleView?.setFractionalTextSize(.0533f * subtitleSize)
        },
        onRelease = { it.player = null },
    )
}
