@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)

package app.sadhan.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.Player
import app.sadhan.media.PlaybackSession
import app.sadhan.ui.tools.elapsedLabel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlaybackControls(session: PlaybackSession) {
    var dragging by remember { mutableStateOf(false) }
    var seek by remember { mutableFloatStateOf(0f) }
    val available = session.queue.isNotEmpty()
    Text(session.current?.title ?: "Select a local file", style = MaterialTheme.typography.titleMedium)
    if (session.buffering) LinearProgressIndicator(Modifier.fillMaxWidth())
    Text("${elapsedLabel(session.position).substringBefore('.')} / ${elapsedLabel(session.duration).substringBefore('.')}", style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum"), color = MaterialTheme.colorScheme.secondary)
    Slider(
        value = if (dragging) seek else session.position.toFloat().coerceIn(0f, session.duration.toFloat().coerceAtLeast(1f)),
        onValueChange = { dragging = true; seek = it },
        onValueChangeFinished = { session.player.seekTo(seek.toLong()); session.refreshPosition(); dragging = false },
        valueRange = 0f..session.duration.toFloat().coerceAtLeast(1f), enabled = session.duration > 0,
    )
    FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        IconButton(onClick = { session.player.seekBack() }, enabled = available) { Icon(Icons.Default.Replay10, "Back 10 seconds") }
        IconButton(onClick = { session.player.seekToPreviousMediaItem() }, enabled = available && session.player.hasPreviousMediaItem()) { Icon(Icons.Default.SkipPrevious, "Previous") }
        FilledIconButton(onClick = session::playPause, enabled = available, modifier = Modifier.size(60.dp)) {
            Icon(if (session.wantsPlay) Icons.Default.Pause else Icons.Default.PlayArrow, if (session.wantsPlay) "Pause" else "Play")
        }
        IconButton(onClick = { session.player.seekToNextMediaItem() }, enabled = available && session.player.hasNextMediaItem()) { Icon(Icons.Default.SkipNext, "Next") }
        IconButton(onClick = { session.player.seekForward() }, enabled = available) { Icon(Icons.Default.Forward10, "Forward 10 seconds") }
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        FilterChip(selected = session.shuffle, onClick = { session.player.shuffleModeEnabled = !session.shuffle }, label = { Text(if (session.shuffle) "Shuffle on" else "Shuffle off") })
        val repeat = when (session.repeatMode) { Player.REPEAT_MODE_ALL -> "All"; Player.REPEAT_MODE_ONE -> "One"; else -> "Off" }
        FilterChip(selected = session.repeatMode != Player.REPEAT_MODE_OFF, onClick = session::cycleRepeat, label = { Text("Repeat: $repeat") })
    }
    ErrorText(session.error)
}

@Composable
fun TrackDialog(session: PlaybackSession, type: Int, dismiss: () -> Unit) {
    val options = session.trackOptions(type)
    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text(if (type == C.TRACK_TYPE_AUDIO) "Audio track" else "Subtitles") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (options.isEmpty()) Text("This file has no selectable ${if (type == C.TRACK_TYPE_AUDIO) "audio" else "subtitle"} tracks.")
                TextButton(onClick = { session.selectTrack(type, null); dismiss() }) { Text("Automatic") }
                if (type == C.TRACK_TYPE_TEXT) TextButton(onClick = { session.selectTrack(type, null, true); dismiss() }) { Text("Off") }
                options.forEach { track ->
                    TextButton(enabled = track.supported, onClick = { session.selectTrack(type, track); dismiss() }) {
                        Text("${if (track.selected) "✓ " else ""}${track.label}${if (!track.supported) " (unsupported)" else ""}")
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = dismiss) { Text("Close") } },
    )
}
