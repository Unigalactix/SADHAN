@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)

package app.sadhan.media

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.delay
import app.sadhan.ui.LocalAppLifecycleOwner

data class LibraryItem(val uri: Uri, val title: String, val detail: String = "", val duration: Long = 0, val mime: String = "")
data class PlaybackTrack(val group: Tracks.Group, val index: Int, val label: String, val supported: Boolean, val selected: Boolean)

@androidx.annotation.OptIn(UnstableApi::class)
class PlaybackSession(context: Context) {
    val player = ExoPlayer.Builder(context.applicationContext)
        .setSeekBackIncrementMs(10_000).setSeekForwardIncrementMs(10_000).build().apply {
            setAudioAttributes(AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).build(), true)
            setHandleAudioBecomingNoisy(true)
        }
    var queue by mutableStateOf<List<LibraryItem>>(emptyList())
        private set
    var index by mutableIntStateOf(0)
        private set
    var playing by mutableStateOf(false)
        private set
    var wantsPlay by mutableStateOf(false)
        private set
    var ready by mutableStateOf(false)
        private set
    var buffering by mutableStateOf(false)
        private set
    var position by mutableLongStateOf(0)
        private set
    var duration by mutableLongStateOf(0)
        private set
    var repeatMode by mutableIntStateOf(Player.REPEAT_MODE_OFF)
        private set
    var shuffle by mutableStateOf(false)
        private set
    var speed by mutableFloatStateOf(1f)
        private set
    var tracks by mutableStateOf(Tracks.EMPTY)
        private set
    var metadata by mutableStateOf(MediaMetadata.EMPTY)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var closed = false
        private set
    val current get() = queue.getOrNull(index)

    init {
        player.addListener(object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                playing = player.isPlaying
                wantsPlay = player.playWhenReady && player.playbackState != Player.STATE_ENDED && player.playerError == null
                ready = player.playbackState == Player.STATE_READY || player.playbackState == Player.STATE_ENDED
                buffering = player.playbackState == Player.STATE_BUFFERING
                index = player.currentMediaItemIndex.coerceAtLeast(0)
                repeatMode = player.repeatMode
                shuffle = player.shuffleModeEnabled
                speed = player.playbackParameters.speed
                refreshPosition()
            }
            override fun onTracksChanged(value: Tracks) { tracks = value }
            override fun onMediaMetadataChanged(value: MediaMetadata) { metadata = value }
            override fun onPlayerError(failure: PlaybackException) {
                error = "Cannot play this file (${failure.errorCodeName}). Its codec, file access or format may be unsupported."
            }
        })
    }

    fun load(items: List<LibraryItem>, selected: Int = 0, autoplay: Boolean = false) {
        require(items.isNotEmpty() && selected in items.indices) { "Select at least one valid file" }
        require(items.all { it.uri.scheme in listOf("content", "file") }) { "Only local files are supported" }
        error = null
        queue = items
        player.setMediaItems(items.map { MediaItem.Builder().setUri(it.uri).setMediaMetadata(MediaMetadata.Builder().setTitle(it.title).build()).build() }, selected, 0)
        player.prepare()
        player.playWhenReady = autoplay
    }

    fun playPause() {
        error = null
        if (player.playbackState == Player.STATE_ENDED) {
            player.seekTo(0)
            player.play()
        } else if (player.playWhenReady) player.pause() else {
            if (player.playbackState == Player.STATE_IDLE) player.prepare()
            player.play()
        }
    }

    fun refreshPosition() {
        if (!closed) {
            position = player.currentPosition.coerceAtLeast(0)
            duration = player.duration.takeIf { it != C.TIME_UNSET && it > 0 } ?: 0
        }
    }

    fun cycleRepeat() {
        player.repeatMode = when (player.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    fun trackOptions(type: Int): List<PlaybackTrack> = tracks.groups.filter { it.type == type }.flatMap { group ->
        (0 until group.length).map { track ->
            val format = group.getTrackFormat(track)
            val name = listOfNotNull(
                format.label, format.language?.takeIf { it != "und" },
                format.sampleMimeType?.substringAfter('/'),
                format.channelCount.takeIf { it > 0 }?.let { "$it ch" },
            ).distinct().joinToString(" · ").ifBlank { "Track ${track + 1}" }
            PlaybackTrack(group, track, name, group.isTrackSupported(track), group.isTrackSelected(track))
        }
    }

    fun selectTrack(type: Int, track: PlaybackTrack?, disabled: Boolean = false) {
        val builder = player.trackSelectionParameters.buildUpon().clearOverridesOfType(type).setTrackTypeDisabled(type, disabled)
        if (track != null) builder.setOverrideForType(TrackSelectionOverride(track.group.mediaTrackGroup, track.index))
        player.trackSelectionParameters = builder.build()
    }

    fun attachSubtitle(uri: Uri, name: String) {
        require(uri.scheme in listOf("content", "file")) { "Choose a local subtitle file" }
        val mime = when (name.substringAfterLast('.', "").lowercase()) {
            "srt" -> MimeTypes.APPLICATION_SUBRIP
            "vtt" -> MimeTypes.TEXT_VTT
            "ssa", "ass" -> MimeTypes.TEXT_SSA
            "ttml", "xml" -> MimeTypes.APPLICATION_TTML
            else -> throw IllegalArgumentException("Choose an SRT, VTT, SSA, ASS or TTML subtitle file")
        }
        val item = player.currentMediaItem ?: throw IllegalArgumentException("Open a video first")
        val subtitle = MediaItem.SubtitleConfiguration.Builder(uri).setMimeType(mime).setLanguage("und")
            .setLabel(name).setSelectionFlags(C.SELECTION_FLAG_DEFAULT).build()
        val time = player.currentPosition
        val autoplay = player.playWhenReady
        val index = player.currentMediaItemIndex
        val items = (0 until player.mediaItemCount).map { current ->
            if (current == index) item.buildUpon().setSubtitleConfigurations(listOf(subtitle)).build() else player.getMediaItemAt(current)
        }
        player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false).setSelectUndeterminedTextLanguage(true).build()
        player.setMediaItems(items, index, time)
        player.prepare()
        player.playWhenReady = autoplay
    }

    fun close() {
        if (!closed) { closed = true; player.release(); queue = emptyList() }
    }
}

@Composable
fun rememberPlaybackSession(keepPlayingInApp: Boolean = false): PlaybackSession {
    val context = LocalContext.current
    val owner = if (keepPlayingInApp) LocalAppLifecycleOwner.current ?: LocalLifecycleOwner.current else LocalLifecycleOwner.current
    val session = remember { PlaybackSession(context) }
    DisposableEffect(session, owner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) session.player.pause() }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer); session.close() }
    }
    LaunchedEffect(session) { while (!session.closed) { session.refreshPosition(); delay(200) } }
    return session
}
