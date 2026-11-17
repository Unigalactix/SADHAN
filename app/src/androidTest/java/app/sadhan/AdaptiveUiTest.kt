package app.sadhan

import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdaptiveUiTest {
    @get:Rule val compose = createEmptyComposeRule()
    private lateinit var scenario: ActivityScenario<MainActivity>
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before fun launch() {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit().clear().putBoolean("updates", false).commit()
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After fun close() { scenario.close() }

    private fun open(title: String) {
        openCatalogTool(compose, title)
    }

    private fun webView(): WebView {
        fun find(view: View): WebView? = when (view) {
            is WebView -> view
            is ViewGroup -> (0 until view.childCount).firstNotNullOfOrNull { find(view.getChildAt(it)) }
            else -> null
        }
        var result: WebView? = null
        compose.waitUntil(15_000) {
            scenario.onActivity { result = find(it.window.decorView) }
            result != null
        }
        return requireNotNull(result)
    }

    @Test fun lightDarkAndSystemThemesAreRealAndPersist() {
        selectPrimaryDestination(compose, "Settings")
        compose.onNodeWithText("System").assertIsSelected()
        compose.onNodeWithText("Dark").performClick()
        compose.waitForIdle()
        scenario.onActivity {
            val bars = androidx.core.view.WindowCompat.getInsetsController(it.window, it.window.decorView)
            assertFalse(bars.isAppearanceLightStatusBars)
            assertFalse(bars.isAppearanceLightNavigationBars)
        }
        val darkPixels = compose.onRoot().captureToImage().toPixelMap()
        val dark = darkPixels[1, 120].luminance()
        val title = compose.onNodeWithText("Make it yours").fetchSemanticsNode().boundsInRoot
        var foreground = 0f
        for (y in title.top.toInt().coerceAtLeast(0) until title.bottom.toInt().coerceAtMost(darkPixels.height)) {
            for (x in title.left.toInt().coerceAtLeast(0) until title.right.toInt().coerceAtMost(darkPixels.width)) foreground = maxOf(foreground, darkPixels[x, y].luminance())
        }
        assertTrue("Dark-mode default text must contrast with the navy surface", foreground > .6f)
        compose.onNodeWithText("Light").performClick()
        compose.waitForIdle()
        scenario.onActivity {
            val bars = androidx.core.view.WindowCompat.getInsetsController(it.window, it.window.decorView)
            assertTrue(bars.isAppearanceLightStatusBars)
            assertTrue(bars.isAppearanceLightNavigationBars)
        }
        val light = compose.onRoot().captureToImage().toPixelMap()[1, 120].luminance()
        assertTrue("Dark and light must actually change rendered surfaces", dark < .2f && light > .6f)
        scenario.recreate()
        compose.onNodeWithText("Light").assertIsSelected()
        compose.onNodeWithText("System").performClick()
        assertEquals("SYSTEM", context.getSharedPreferences("settings", Context.MODE_PRIVATE).getString("theme", null))
    }

    @Test fun unfoldedWorkspacePreservesVisibleInputsAndAddsCompanionTool() {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        fun shell(command: String): String = android.os.ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand(command)).bufferedReader().use { it.readText() }
        val original = Regex("(?:Physical|Override) size: (\\d+x\\d+)").findAll(shell("wm size")).last().groupValues[1]
        open("Calculator")
        compose.onNodeWithText("Expression").performTextInput("44+55")
        val hadCompanion = compose.onAllNodesWithText("COMPANION WORKSPACE").fetchSemanticsNodes().isNotEmpty()
        val density = context.resources.displayMetrics.density
        var before: MainActivity? = null
        scenario.onActivity { before = it }
        try {
            shell("wm size ${(800 * density).toInt()}x${(700 * density).toInt()}")
            compose.waitUntil(20_000) { compose.onAllNodesWithText("COMPANION WORKSPACE").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("44+55").assertExists()
            compose.onNodeWithText("Category").assertExists()
            scenario.onActivity { assertSame("Window resizing must not reset the active session", before, it) }
        } finally { shell("wm size $original") }
        compose.waitUntil(20_000) { compose.onAllNodesWithText("COMPANION WORKSPACE").fetchSemanticsNodes().isNotEmpty() == hadCompanion }
        compose.onNodeWithText("44+55").assertExists()
    }

    @Test fun privateBrowserTabsAreEphemeralAndStorageFeaturesAreDisabled() {
        open("Private Browser")
        compose.onNodeWithText("PRIVATE SESSION · Not anonymous").assertIsDisplayed()
        val view = webView()
        scenario.onActivity {
            assertEquals(WebSettings.LOAD_NO_CACHE, view.settings.cacheMode)
            assertFalse(view.settings.domStorageEnabled)
            assertFalse(view.settings.allowFileAccess)
            assertFalse(view.settings.allowContentAccess)
            assertFalse(CookieManager.getInstance().acceptCookie())
        }
        compose.onNodeWithContentDescription("New private tab").performClick()
        compose.onAllNodesWithContentDescription("Close private tab").assertCountEquals(2)
        compose.onNodeWithContentDescription("Back").performClick()
        open("Private Browser")
        compose.onAllNodesWithContentDescription("Close private tab").assertCountEquals(2)
        closeWorkspaceTool(compose, "Private Browser")
        open("Private Browser")
        compose.onAllNodesWithContentDescription("Close private tab").assertCountEquals(1)
        compose.onNodeWithText("HTTPS address or search").assertExists()
    }

    @Test fun youtubeSearchSelectionUsesOfficialEmbedWithoutApiKey() {
        open("YouTube")
        compose.onNodeWithText("Search YouTube").performClick()
        compose.onNodeWithText("Search query").performTextInput("SADHAN test")
        compose.onNodeWithText("Search", substring = false).performClick()
        val view = webView()
        scenario.onActivity {
            view.stopLoading()
            view.loadDataWithBaseURL(
                "https://m.youtube.com/results?search_query=SADHAN",
                "<html><body><a id='result' href='https://www.youtube.com/watch?v=M7lc1UVf-VE'>Result</a></body></html>",
                "text/html", "UTF-8", null,
            )
        }
        compose.waitUntil(15_000) {
            var ready = false
            scenario.onActivity {
                ready = view.progress == 100
                if (ready) view.evaluateJavascript("document.getElementById('result').click()", null)
            }
            compose.onAllNodesWithText("Selected video: M7lc1UVf-VE").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Back to results").assertExists()
        compose.onNodeWithText("BETA").assertExists()
    }
}
