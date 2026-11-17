@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)

package app.sadhan

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.view.View
import android.view.ViewGroup
import androidx.media3.ui.PlayerView
import androidx.media3.common.C
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.core.content.FileProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.lifecycle.Lifecycle
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

@RunWith(AndroidJUnit4::class)
class LocalMediaTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private lateinit var scenario: ActivityScenario<MainActivity>
    private val files = mutableListOf<File>()

    @Before fun launch() {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit().clear().putBoolean("updates", false).commit()
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After fun close() {
        scenario.close()
        files.forEach { it.delete() }
    }

    private fun fixture(name: String): File = File(context.cacheDir, "shared_images/$name").also {
        it.parentFile?.mkdirs()
        files.add(it)
    }

    private fun chooseFile(tool: String, button: String, file: File) {
        compose.onNodeWithTag("tool-list").performScrollToNode(hasText(tool))
        compose.onNode(hasText(tool) and hasAnyAncestor(hasTestTag("tool-list"))).performClick()
        val filter = IntentFilter(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            addDataType("*/*")
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
        val monitor = instrumentation.addMonitor(filter, Instrumentation.ActivityResult(Activity.RESULT_OK, Intent().setData(uri)), true)
        try {
            compose.onNodeWithText(button).performClick()
            compose.waitForIdle()
            assertEquals(1, monitor.hits)
        } finally { instrumentation.removeMonitor(monitor) }
    }

    @Test fun musicPlaysChosenLocalAudioAndAdvances() {
        val file = fixture("test-audio.wav")
        val samples = 16000 * 4
        val data = ByteBuffer.allocate(44 + samples * 2).order(ByteOrder.LITTLE_ENDIAN).apply {
            put("RIFF".toByteArray()); putInt(36 + samples * 2); put("WAVEfmt ".toByteArray())
            putInt(16); putShort(1); putShort(1); putInt(16000); putInt(32000)
            putShort(2); putShort(16); put("data".toByteArray()); putInt(samples * 2)
            repeat(samples) { putShort(0) }
        }.array()
        file.writeBytes(data)
        chooseFile("Music Player", "Choose audio", file)
        compose.waitUntil(20_000) { compose.onAllNodes(hasContentDescription("Play") and isEnabled()).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("Play").performScrollTo().performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithContentDescription("Pause").fetchSemanticsNodes().isNotEmpty() }
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("00:00:01 / 00:00:04").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithContentDescription("Pause").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Play").assertExists()
        compose.onNodeWithText("Shuffle off").performScrollTo().performClick()
        compose.onNodeWithText("Shuffle on").assertExists()
        compose.onNodeWithText("Repeat: Off").performScrollTo().performClick()
        compose.onNodeWithText("Repeat: All").assertExists()
        compose.onNodeWithText("Repeat: All").performClick()
        compose.onNodeWithText("Repeat: One").assertExists()
    }

    @Test fun videoPlaysChosenLocalFileAndReleasesOnExit() {
        val file = fixture("test-video.mp4")
        makeVideo(file)
        chooseFile("Video Player", "Choose video", file)
        var video: PlayerView? = null
        fun find(view: View): PlayerView? = when (view) {
            is PlayerView -> view
            is ViewGroup -> (0 until view.childCount).firstNotNullOfOrNull { find(view.getChildAt(it)) }
            else -> null
        }
        compose.waitUntil(20_000) {
            var playing = false
            scenario.onActivity { activity ->
                video = find(activity.window.decorView)
                playing = video?.player?.isPlaying == true && (video?.player?.duration ?: 0) > 0
            }
            playing
        }
        scenario.moveToState(Lifecycle.State.CREATED)
        scenario.onActivity { assertFalse(requireNotNull(video?.player).isPlaying) }
        scenario.moveToState(Lifecycle.State.RESUMED)
        compose.onNodeWithText("Playback options").performScrollTo().performClick()
        compose.onNodeWithText("Fit").performClick()
        compose.onNodeWithText("Zoom").performClick()
        compose.onNodeWithText("Done").performClick()
        val subtitle = fixture("test-captions.srt").apply { writeText("1\n00:00:00,000 --> 00:00:01,900\nSADHAN subtitle test\n") }
        val filter = IntentFilter(Intent.ACTION_OPEN_DOCUMENT).apply { addCategory(Intent.CATEGORY_OPENABLE); addDataType("*/*") }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", subtitle)
        val monitor = instrumentation.addMonitor(filter, Instrumentation.ActivityResult(Activity.RESULT_OK, Intent().setData(uri)), true)
        try { compose.onNodeWithText("Load subtitle file").performScrollTo().performClick() }
        finally { instrumentation.removeMonitor(monitor) }
        compose.waitUntil(20_000) {
            var found = false
            scenario.onActivity { found = video?.player?.currentTracks?.groups?.any { it.type == C.TRACK_TYPE_TEXT } == true }
            found
        }
        compose.onNodeWithText("Subtitles").performScrollTo().performClick()
        compose.onNodeWithText("Off").performClick()
        compose.onNodeWithText("Fullscreen").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Exit fullscreen").assertIsDisplayed().performClick()
        compose.onNodeWithContentDescription("Close Video Player tab").performClick()
        compose.runOnIdle { assertNull(requireNotNull(video).player) }
    }

    private fun makeVideo(file: File) {
        val encoder = MediaCodec.createEncoderByType("video/avc")
        val muxer = MediaMuxer(file.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        var muxing = false
        try {
            val format = MediaFormat.createVideoFormat("video/avc", 64, 64).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
                setInteger(MediaFormat.KEY_BIT_RATE, 64_000)
                setInteger(MediaFormat.KEY_FRAME_RATE, 15)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            }
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            encoder.start()
            var frame = 0
            var track = -1
            val info = MediaCodec.BufferInfo()
            val deadline = System.currentTimeMillis() + 20_000
            var done = false
            while (!done && System.currentTimeMillis() < deadline) {
                if (frame <= 30) {
                    val input = encoder.dequeueInputBuffer(10_000)
                    if (input >= 0) {
                        if (frame == 30) encoder.queueInputBuffer(input, 0, 0, frame * 1_000_000L / 15, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        else {
                            val pixels = ByteArray(64 * 64 * 3 / 2) { if (it < 64 * 64) 80 else 128.toByte() }
                            encoder.getInputBuffer(input)!!.put(pixels)
                            encoder.queueInputBuffer(input, 0, pixels.size, frame * 1_000_000L / 15, 0)
                        }
                        frame++
                    }
                }
                val output = encoder.dequeueOutputBuffer(info, 10_000)
                if (output == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    track = muxer.addTrack(encoder.outputFormat)
                    muxer.start()
                    muxing = true
                } else if (output >= 0) {
                    if (info.size > 0 && info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG == 0) {
                        muxer.writeSampleData(track, requireNotNull(encoder.getOutputBuffer(output)), info)
                    }
                    done = info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0
                    encoder.releaseOutputBuffer(output, false)
                }
            }
            check(done) { "Synthetic test video encoding timed out" }
        } finally {
            encoder.release()
            if (muxing) muxer.stop()
            muxer.release()
        }
    }
}
