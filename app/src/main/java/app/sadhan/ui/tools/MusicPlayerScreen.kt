package app.sadhan.ui.tools

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.sadhan.media.LibraryItem
import app.sadhan.media.documentName
import app.sadhan.media.rememberPlaybackSession
import app.sadhan.ui.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MusicPlayerScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val session = rememberPlaybackSession(keepPlayingInApp = true)
    ReportTabWork("music", if (session.queue.isNotEmpty()) "Playback will stop and the music queue will be erased." else null)
    val library = rememberMediaLibrary(video = false)
    var error by remember { mutableStateOf<String?>(null) }
    var tab by remember { mutableStateOf("Library") }
    var artwork by remember { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(session.metadata.artworkData) {
        val bytes = session.metadata.artworkData
        artwork = if (bytes == null || bytes.size > 5_000_000) null else withContext(Dispatchers.IO) {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
            var sample = 1
            while (bounds.outWidth / sample > 512 || bounds.outHeight / sample > 512) sample *= 2
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample })
        }
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) scope.launch {
            error = null
            try {
                val items = withContext(Dispatchers.IO) { uris.map { LibraryItem(it, documentName(context, it)) } }
                withContext(Dispatchers.Main.immediate) { session.load(items); tab = "Now playing" }
            } catch (e: IOException) { error = "Cannot read audio: ${e.message}" }
            catch (e: SecurityException) { error = "Audio access denied: ${e.message}" }
            catch (e: IllegalArgumentException) { error = e.message }
        }
    }
    ToolColumn {
        Button(onClick = { picker.launch(arrayOf("audio/*")) }) { Text("Choose audio") }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("Library", "Now playing", "Queue").forEach { section ->
                FilterChip(selected = tab == section, onClick = { tab = section }, label = { Text(if (section == "Queue") "Queue (${session.queue.size})" else section) })
            }
        }
        if (tab == "Now playing") {
        Surface(shape = MaterialTheme.shapes.extraLarge, tonalElevation = 0.dp) {
            Box(
                Modifier.fillMaxWidth().height(170.dp).background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.surfaceContainerLow))),
                contentAlignment = Alignment.Center,
            ) {
                val image = artwork
                if (image != null) Image(image.asImageBitmap(), "Embedded album artwork", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                else Icon(Icons.Default.MusicNote, null, Modifier.size(74.dp), tint = MaterialTheme.colorScheme.primary)
                Text(if (session.playing) "NOW PLAYING" else "LOCAL AUDIO", Modifier.align(Alignment.BottomCenter).background(MaterialTheme.colorScheme.surface.copy(alpha = .85f)).padding(12.dp), style = MaterialTheme.typography.labelLarge)
            }
        }
        Text("Music can continue while you use other open tool tabs. It pauses when the app is backgrounded; closing this tab erases the queue.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        PlaybackControls(session)
        session.metadata.artist?.let { Text(it.toString(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        if (session.queue.isNotEmpty()) {
            Choice("Playback speed", "${session.speed}×", listOf(.5f, .75f, 1f, 1.25f, 1.5f, 2f).map { "${it}×" }) {
                session.player.setPlaybackSpeed(it.removeSuffix("×").toFloat())
            }
        }
        }
        ErrorText(error)
        if (tab == "Library") MediaLibrary(video = false, state = library) { items, index -> session.load(items, index, true); tab = "Now playing" }
        if (tab == "Queue") {
            Text("Up next", style = MaterialTheme.typography.titleMedium)
            if (session.queue.isEmpty()) Text("Choose audio files or select a song from your library.")
            LibraryList(session.queue) { _, index -> session.player.seekTo(index, 0); session.player.play() }
        }
    }
}
