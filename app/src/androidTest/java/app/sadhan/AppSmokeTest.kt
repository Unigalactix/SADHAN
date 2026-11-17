package app.sadhan

import android.content.Context
import android.content.ContentUris
import android.graphics.BitmapFactory
import android.provider.MediaStore
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.sadhan.ui.toolCatalog
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertEquals
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer

@RunWith(AndroidJUnit4::class)
class AppSmokeTest {
    @get:Rule val compose = createEmptyComposeRule()
    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before fun launch() {
        ApplicationProvider.getApplicationContext<Context>().getSharedPreferences("settings", Context.MODE_PRIVATE)
            .edit().clear().putBoolean("updates", false).commit()
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After fun close() { scenario.close() }

    private fun openTool(title: String) {
        openCatalogTool(compose, title)
    }

    @Test fun everyToolOpensAndInternetGatesWork() {
        selectPrimaryDestination(compose, "Settings")
        compose.onNodeWithContentDescription("Internet").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Back").performClick()
        toolCatalog.forEach { tool ->
            openTool(tool.title)
            compose.onAllNodesWithText(tool.title)[0].assertIsDisplayed()
            if (tool.route in listOf("weather", "currency", "youtube", "browser")) {
                compose.onNodeWithText("Turned off in Settings").performScrollTo().assertIsDisplayed()
            }
            closeWorkspaceTool(compose, tool.title)
        }
    }

    @Test fun toolTabsRetainStateUntilClosedAndCalculatorEvaluates() {
        openTool("Scratchpad")
        compose.onNodeWithText("Temporary note").performTextInput("private temporary note")
        compose.onNodeWithContentDescription("Back").performClick()
        openTool("Scratchpad")
        compose.onNodeWithText("private temporary note").assertExists()
        closeWorkspaceTool(compose, "Scratchpad", confirm = false)
        compose.onNodeWithText("private temporary note").assertExists()
        closeWorkspaceTool(compose, "Scratchpad")
        openTool("Scratchpad")
        compose.onNodeWithText("private temporary note").assertDoesNotExist()
        closeWorkspaceTool(compose, "Scratchpad")
        openTool("Calculator")
        compose.onNodeWithText("Expression").performTextInput("2*(3+4)")
        compose.onNodeWithText("Calculate").performClick()
        compose.waitUntil(5_000) { compose.onNodeWithText("2*(3+4) = 14").isDisplayed() }
        compose.onNodeWithText("2*(3+4) = 14").assertIsDisplayed()
    }

    @Test fun developerPageAndBetaLabellingAreAccessible() {
        compose.onNodeWithContentDescription("About").performClick()
        compose.onNodeWithText("About developer").performScrollTo().performClick()
        compose.onNodeWithText("Rajesh Kodaganti (Rugved)").assertIsDisplayed()
        compose.onNodeWithText("Visit portfolio").assertHasClickAction()
        compose.onNodeWithText("Connect on LinkedIn").assertHasClickAction()
        compose.onNodeWithContentDescription("Back").performClick()
        compose.onNodeWithContentDescription("Back").performClick()
        openTool("YouTube")
        compose.onNodeWithText("BETA").assertIsDisplayed()
        compose.onNodeWithText("Beta — playback can be unstable. Uses YouTube's official embed.").assertIsDisplayed()
    }

    @Test fun internetSettingSurvivesActivityRecreation() {
        selectPrimaryDestination(compose, "Settings")
        compose.onNodeWithContentDescription("Internet").performScrollTo().performClick()
        scenario.recreate()
        compose.onNodeWithContentDescription("Back").performClick()
        openTool("Weather")
        compose.onNodeWithText("Turned off in Settings").assertIsDisplayed()
    }

    @Test fun qrMakerExportsAnActuallyDecodableImage() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val content = "SADHAN private QR test"
        openTool("QR Maker")
        compose.onNodeWithText("QR content").performTextInput(content)
        compose.onNodeWithText("Make QR").performClick()
        compose.waitUntil(20_000) {
            compose.onAllNodesWithContentDescription("Generated QR code").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Save to Pictures").performScrollTo().performClick()
        var saved: android.net.Uri? = null
        try {
            compose.waitUntil(20_000) {
                context.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    arrayOf(MediaStore.Images.Media._ID),
                    "${MediaStore.Images.Media.RELATIVE_PATH} = ? AND ${MediaStore.Images.Media.IS_PENDING} = 0",
                    arrayOf("Pictures/SADHAN/"), "${MediaStore.Images.Media._ID} DESC",
                )?.use { cursor ->
                    if (cursor.moveToFirst()) saved = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cursor.getLong(0))
                }
                saved != null
            }
            val image = context.contentResolver.openInputStream(requireNotNull(saved)).use { BitmapFactory.decodeStream(it) }
            val pixels = IntArray(image.width * image.height)
            image.getPixels(pixels, 0, image.width, 0, 0, image.width, image.height)
            val decoded = MultiFormatReader().decode(BinaryBitmap(HybridBinarizer(RGBLuminanceSource(image.width, image.height, pixels))))
            assertEquals(content, decoded.text)
            image.recycle()
        } finally { saved?.let { context.contentResolver.delete(it, null, null) } }
    }
}
