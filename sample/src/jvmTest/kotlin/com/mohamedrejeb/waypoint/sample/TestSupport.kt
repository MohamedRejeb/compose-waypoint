package com.mohamedrejeb.waypoint.sample

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import kotlin.test.assertEquals

private const val ShownTimeoutMillis = 5_000L

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.assertShown(text: String) {
    waitUntil(timeoutMillis = ShownTimeoutMillis) {
        onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
    }
}

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.assertNotShown(text: String) {
    waitForIdle()
    assertEquals(0, onAllNodesWithText(text).fetchSemanticsNodes().size, "\"$text\" should not be shown")
}

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.field(label: String): SemanticsNodeInteraction =
    onNode(hasSetTextAction() and hasText(label))

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.tap(text: String) {
    assertShown(text)
    val node = onNodeWithText(text)
    // A click on a node scrolled out of view lands nowhere, so bring it in first.
    try {
        node.performScrollTo()
    } catch (_: AssertionError) {
        // Not inside a scrollable container, nothing to scroll.
    }
    node.performClick()
}
