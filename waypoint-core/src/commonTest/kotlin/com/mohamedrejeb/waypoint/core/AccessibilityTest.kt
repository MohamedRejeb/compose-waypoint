package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class AccessibilityTest {

    @Test
    fun `tooltip content has liveRegion semantics`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "A", title = "Step 1"),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText(
                        text = "Step 1",
                        modifier = Modifier.testTag("tooltip-text"),
                    )
                },
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(Modifier.size(50.dp).waypointTarget(state, "A"))
                }
            }
        }

        runOnIdle { state.start() }

        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Step 1").fetchSemanticsNodes().isNotEmpty()
        }

        val liveRegionNodes = onAllNodes(
            SemanticsMatcher.keyIsDefined(SemanticsProperties.LiveRegion),
        ).fetchSemanticsNodes()

        assertTrue(
            liveRegionNodes.isNotEmpty(),
            "Expected at least one node with LiveRegion semantics, but found none",
        )
    }
}
