package app.sadhan

import android.content.Context
import android.os.ParcelFileDescriptor
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.ui.input.key.Key
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
@OptIn(ExperimentalTestApi::class)
class QualityRegressionTest {
    @get:Rule val compose = createEmptyComposeRule()
    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before fun launch() {
        ApplicationProvider.getApplicationContext<Context>().getSharedPreferences("settings", Context.MODE_PRIVATE)
            .edit().clear().putBoolean("updates", false).commit()
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After fun close() { scenario.close() }

    @Test fun synonymsAndSeparateDestinationsKeepCatalogsFocused() {
        compose.onNodeWithText("Find a tool").performTextInput("loan")
        compose.onNodeWithText("EMI Calculator").assertIsDisplayed()
        compose.onNodeWithText("Find a tool").performTextClearance()
        selectPrimaryDestination(compose, "Media")
        compose.onNodeWithText("Music Player").assertIsDisplayed()
        compose.onNodeWithText("Snake").assertDoesNotExist()
        selectPrimaryDestination(compose, "Games")
        compose.onNodeWithText("Snake").assertIsDisplayed()
        compose.onNodeWithText("Music Player").assertDoesNotExist()
    }

    @Test fun rhythmPracticeHasAccessibleControlsAndKeyboardActions() {
        openCatalogTool(compose, "Rhythm Tiles")
        compose.onNodeWithText("Practice · starts a new round").performScrollTo()
        compose.onNodeWithContentDescription("Untimed practice").performClick()
        compose.onNodeWithText("Start").performClick()
        compose.waitUntil(5_000) { compose.onAllNodesWithText("Pause").fetchSemanticsNodes().isNotEmpty() }
        compose.waitUntil(10_000) {
            var focused = false
            scenario.onActivity { focused = it.hasWindowFocus() }
            focused
        }
        scenario.onActivity { it.window.decorView.requestFocusFromTouch() }
        compose.waitForIdle()
        val highlighted = hasContentDescription("highlighted", substring = true)
        val button = compose.onNode(highlighted)
        button.performScrollTo().assertIsEnabled().assertHasClickAction()
        val label = button.fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.ContentDescription].single()
        val column = Regex("column ([1-4])").find(label)!!.groupValues[1].toInt()
        button.performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.RequestFocus) { assertTrue(it()) }
        button.assertIsFocused()
        button.performKeyInput { pressKey(listOf(Key.One, Key.Two, Key.Three, Key.Four)[column - 1]) }
        compose.waitUntil(5_000) { compose.onAllNodesWithText("1 tiles").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Practice · No timer").assertExists()
        closeWorkspaceTool(compose, "Rhythm Tiles", confirm = false)
        compose.onNodeWithText("1 tiles").assertExists()
    }

    @Test fun stoppedBrowserRendererOffersReplacementAndDisablesOldCommands() {
        openCatalogTool(compose, "Private Browser")
        fun find(view: View): WebView? = when (view) {
            is WebView -> view
            is ViewGroup -> (0 until view.childCount).firstNotNullOfOrNull { find(view.getChildAt(it)) }
            else -> null
        }
        var view: WebView? = null
        compose.waitUntil(15_000) {
            scenario.onActivity { view = find(it.window.decorView) }
            view != null
        }
        val original = requireNotNull(view)
        scenario.onActivity { original.loadDataWithBaseURL("https://example.test/", "<html><title>Fixture</title><body>Private renderer fixture</body></html>", "text/html", "UTF-8", null) }
        compose.waitUntil(15_000) {
            var ready = false
            scenario.onActivity { ready = original.progress == 100 && original.webViewRenderProcess != null }
            ready
        }
        scenario.onActivity { assertTrue(requireNotNull(original.webViewRenderProcess).terminate()) }
        compose.waitUntil(15_000) { compose.onAllNodesWithText("Open replacement tab").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("Reload page").assertIsNotEnabled()
        compose.onNodeWithContentDescription("Browser back").assertIsNotEnabled()
        compose.onNodeWithText("Open replacement tab").performClick()
        compose.waitUntil(15_000) {
            scenario.onActivity { view = find(it.window.decorView) }
            view != null && view !== original
        }
        compose.onNodeWithText("Open replacement tab").assertDoesNotExist()
    }

    @Test fun shortLandscapeGamesKeepBoardAndPrimaryControlsInThePane() {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        fun shell(command: String): String = ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand(command)).bufferedReader().use { it.readText() }
        val original = Regex("(?:Physical|Override) size: (\\d+x\\d+)").findAll(shell("wm size")).last().groupValues[1]
        val density = ApplicationProvider.getApplicationContext<Context>().resources.displayMetrics.density
        try {
            shell("wm size ${(900 * density).toInt()}x${(400 * density).toInt()}")
            openCatalogTool(compose, "Lane Racer")
            compose.onNodeWithText("Start").assertIsDisplayed()
            compose.onNodeWithText("Left").assertIsDisplayed()
            compose.onNodeWithText("Right").assertIsDisplayed()
            compose.onNodeWithContentDescription("Game rules").assertIsDisplayed()
        } finally { shell("wm size $original") }
    }
}
