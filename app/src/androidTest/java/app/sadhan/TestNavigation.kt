package app.sadhan

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import app.sadhan.ui.toolCatalog
import app.sadhan.ui.parentDestination

fun selectPrimaryDestination(compose: ComposeTestRule, label: String) {
    val target = hasText(label) and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)
    compose.onAllNodes(target)[0].performClick()
}

fun openCatalogTool(compose: ComposeTestRule, title: String) {
    val destination = when (toolCatalog.first { it.title == title }.parentDestination()) { "media" -> "Media"; "games" -> "Games"; else -> "Tools" }
    if (compose.onAllNodesWithTag("tool-list").fetchSemanticsNodes().isEmpty()) {
        compose.onNodeWithContentDescription("Back").performClick()
    }
    selectPrimaryDestination(compose, destination)
    compose.waitUntil(10_000) { compose.onAllNodesWithTag("tool-list").fetchSemanticsNodes().size == 1 }
    compose.onNodeWithTag("tool-list").performScrollToNode(hasText(title))
    val target = hasText(title) and hasAnyAncestor(hasTestTag("tool-list"))
    compose.waitUntil(10_000) { compose.onAllNodes(target).fetchSemanticsNodes().size == 1 && compose.onNode(target).isDisplayed() }
    compose.onNode(target).performClick()
}

fun closeWorkspaceTool(compose: ComposeTestRule, title: String, confirm: Boolean = true) {
    if (compose.onAllNodesWithContentDescription("Close $title tab").fetchSemanticsNodes().isEmpty()) {
        compose.onNodeWithContentDescription("Workspace").performClick()
    }
    compose.onNodeWithContentDescription("Close $title tab").performScrollTo().performClick()
    if (compose.onAllNodesWithText("Close this tab?").fetchSemanticsNodes().isNotEmpty()) {
        compose.onNodeWithText(if (confirm) "Close tab" else "Keep open").performClick()
    }
    if (compose.onAllNodesWithText("Your workspace").fetchSemanticsNodes().isNotEmpty()) compose.onNodeWithText("Done").performClick()
}
