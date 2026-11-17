package app.sadhan.ui.tools

import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.sadhan.media.documentName
import app.sadhan.ui.ErrorText
import java.io.IOException

@Composable
fun VideoPlayerScreen() {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    var title by remember { mutableStateOf("Choose a local video") }
    var error by remember { mutableStateOf<String?>(null) }
    val video = remember {
        VideoView(context).apply {
            setMediaController(MediaController(context).also { it.setAnchorView(this) })
            setOnErrorListener { _, what, extra ->
                error = "Cannot play this video (code $what/$extra). Codec support depends on your device."
                true
            }
        }
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try {
                error = null
                title = documentName(context, uri)
                video.setOnPreparedListener {
                    if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) video.start()
                }
                video.setVideoURI(uri)
            } catch (e: SecurityException) { error = "Video access denied: ${e.message}" }
            catch (e: IllegalArgumentException) { error = "Unsupported video: ${e.message}" }
            catch (e: IOException) { error = "Cannot read video: ${e.message}" }
        }
    }
    DisposableEffect(owner, video) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) video.pause() }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer); video.stopPlayback() }
    }
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Private local playback. Tap the video for play, pause and seeking. Pauses in the background; no history is saved.")
        Button(onClick = { picker.launch(arrayOf("video/*")) }) { Text("Choose video") }
        Text(title)
        ErrorText(error)
        AndroidView(factory = { video }, modifier = Modifier.fillMaxWidth().weight(1f))
    }
}
