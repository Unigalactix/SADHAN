@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)

package app.sadhan

import android.app.Activity
import android.app.Instrumentation
import android.content.*
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.core.content.FileProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class DesignEvidenceTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun captureSixMasterViewsInBothThemes() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val output = File(context.getExternalFilesDir(null), "quality-evidence").apply { mkdirs() }
        val fixtures = File(context.cacheDir, "shared_images").apply { mkdirs() }
        val audio = File(fixtures, "Demo audio.wav").also { writeTestAudio(it) }
        val video = File(fixtures, "Original test video.mp4").also { writeTestVideo(it) }
        fun capture(name: String) {
            compose.waitForIdle()
            compose.waitUntil(10_000) {
                instrumentation.uiAutomation.rootInActiveWindow?.packageName?.toString() == context.packageName
            }
            instrumentation.uiAutomation.waitForIdle(300, 5_000)
            val image = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
            File(output, "$name.png").outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
            image.recycle()
        }
        fun pick(button: String, file: File) {
            val filter = IntentFilter(Intent.ACTION_OPEN_DOCUMENT).apply { addCategory(Intent.CATEGORY_OPENABLE); addDataType("*/*") }
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
            val monitor = instrumentation.addMonitor(filter, Instrumentation.ActivityResult(Activity.RESULT_OK, Intent().setData(uri)), true)
            try { compose.onNodeWithText(button).performClick() }
            finally { instrumentation.removeMonitor(monitor) }
        }
        try {
            for (mode in listOf("LIGHT", "DARK")) {
                context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit().clear().putBoolean("updates", false).putString("theme", mode).commit()
                val scenario = ActivityScenario.launch(MainActivity::class.java)
                try {
                    capture("home-${mode.lowercase()}")
                    openCatalogTool(compose, "Scratchpad")
                    compose.onNodeWithText("Temporary note").performTextInput("A temporary workspace example")
                    compose.onNodeWithContentDescription("Back").performClick()
                    openCatalogTool(compose, "Calculator")
                    compose.onNodeWithText("Expression").performTextInput("250×0.15")
                    compose.onNodeWithText("Calculate").performClick()
                    compose.waitUntil(10_000) { compose.onNodeWithText("250×0.15 = 37.5").isDisplayed() }
                    capture("calculator-${mode.lowercase()}")
                    compose.onNodeWithContentDescription("Workspace").performClick()
                    capture("workspace-${mode.lowercase()}")
                    compose.onNodeWithText("Done").performClick()
                    compose.onNodeWithContentDescription("Back").performClick()
                    openCatalogTool(compose, "Music Player")
                    pick("Choose audio", audio)
                    compose.waitUntil(15_000) { compose.onAllNodesWithText("Demo audio.wav").fetchSemanticsNodes().isNotEmpty() }
                    capture("music-${mode.lowercase()}")
                    compose.onNodeWithContentDescription("Back").performClick()
                    openCatalogTool(compose, "Video Player")
                    pick("Choose video", video)
                    compose.waitUntil(15_000) { compose.onAllNodesWithText("Original test video.mp4").fetchSemanticsNodes().isNotEmpty() }
                    fun findPlayer(view: View): PlayerView? = when (view) {
                        is PlayerView -> view.takeIf { it.player != null }
                        is ViewGroup -> (0 until view.childCount).firstNotNullOfOrNull { findPlayer(view.getChildAt(it)) }
                        else -> null
                    }
                    compose.waitUntil(15_000) {
                        var ready = false
                        scenario.onActivity {
                            val player = findPlayer(it.window.decorView)?.player
                            ready = player?.playbackState == Player.STATE_READY || player?.playbackState == Player.STATE_ENDED
                            if (ready) player?.pause()
                        }
                        ready
                    }
                    capture("video-${mode.lowercase()}")
                    compose.onNodeWithContentDescription("Back").performClick()
                    openCatalogTool(compose, "Lane Racer")
                    capture("game-${mode.lowercase()}")
                } finally { scenario.close() }
            }
        } finally { audio.delete(); video.delete() }
    }
}
