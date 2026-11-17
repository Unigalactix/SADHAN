package app.sadhan

import android.content.Context
import android.os.ParcelFileDescriptor
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.*
import org.junit.Assert.assertSame
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FoldPostureTest {
    @get:Rule val compose = createEmptyComposeRule()

    private fun shell(command: String): String =
        ParcelFileDescriptor.AutoCloseInputStream(InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command))
            .bufferedReader().use { it.readText() }

    @Test fun physicalFoldHalfOpenAndUnfoldKeepTheToolSession() {
        Assume.assumeTrue(shell("cmd device_state print-states").contains("HALF_OPENED"))
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit().clear()
            .putBoolean("updates", false).putString("theme", "DARK").commit()
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        try {
            openCatalogTool(compose, "Calculator")
            compose.onNodeWithText("Expression").performTextInput("99+1")
            var before: MainActivity? = null
            scenario.onActivity { before = it }
            listOf(1, 2, 3).forEach { state ->
                shell("cmd device_state state $state")
                shell("input keyevent 82")
                compose.waitUntil(20_000) { compose.onAllNodesWithText("99+1").fetchSemanticsNodes().isNotEmpty() }
                compose.onNodeWithText("99+1").assertExists()
                scenario.onActivity { assertSame("Fold changes must not recreate or erase the open tool", before, it) }
            }
            compose.onNodeWithText("Calculate").performClick()
            compose.waitUntil(10_000) { compose.onNodeWithText("99+1 = 100").isDisplayed() }
            val image = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
            image?.let {
                java.io.File(context.getExternalFilesDir(null), "fold-workspace-dark.png").outputStream().use { stream ->
                    it.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
                }
            }
        } finally {
            shell("cmd device_state state reset")
            scenario.close()
        }
    }
}
