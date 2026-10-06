package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * [StepScope] and [HintScope] carry what a tooltip needs to render itself:
 * the texts and the resolved placement.
 */
@OptIn(ExperimentalTestApi::class)
class StepScopeContentUiTest {

    /** Shows [step] and hands the scope its tooltip content received to [block]. */
    private fun runScopeTest(
        step: WaypointStep<String>,
        viaStepContent: Boolean = false,
        block: (StepScope?) -> Unit,
    ) = runComposeUiTest {
        var captured: StepScope? = null
        val capture: @Composable (StepScope) -> Unit = { scope ->
            captured = scope
            BasicText("tip")
        }
        val shownStep = if (viaStepContent) step.copy(content = capture) else step
        val state = WaypointState(steps = listOf(shownStep))
        setContent {
            WaypointHost(
                state = state,
                tooltipContent = if (viaStepContent) { _ -> BasicText("host-tip") } else capture,
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(60.dp).waypointTarget(state, "target"))
                }
            }
        }
        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("tip").fetchSemanticsNodes().isNotEmpty()
        }
        waitForIdle()

        block(captured)
    }

    private fun runPlacementTest(requested: TooltipPlacement, resolved: ResolvedPlacement) =
        runScopeTest(WaypointStep(targetKey = "target", placement = requested)) { scope ->
            assertEquals(resolved, scope?.placement)
        }

    @Test
    fun `scope exposes the step title and description`() = runScopeTest(
        WaypointStep(targetKey = "target", title = "Title", description = "Text"),
    ) { scope ->
        assertEquals("Title", scope?.title)
        assertEquals("Text", scope?.description)
    }

    @Test
    fun `scope texts are null when the step has none`() = runScopeTest(
        WaypointStep(targetKey = "target"),
    ) { scope ->
        assertNotNull(scope)
        assertNull(scope.title)
        assertNull(scope.description)
    }

    @Test
    fun `scope placement is Top for a tooltip above the target`() =
        runPlacementTest(TooltipPlacement.Top, ResolvedPlacement.Top)

    @Test
    fun `scope placement is Bottom for a tooltip below the target`() =
        runPlacementTest(TooltipPlacement.Bottom, ResolvedPlacement.Bottom)

    @Test
    fun `scope placement is Start for a tooltip before the target`() =
        runPlacementTest(TooltipPlacement.Start, ResolvedPlacement.Start)

    @Test
    fun `scope placement is End for a tooltip after the target`() =
        runPlacementTest(TooltipPlacement.End, ResolvedPlacement.End)

    @Test
    fun `step content receives the same scope data`() = runScopeTest(
        WaypointStep(targetKey = "target", title = "Title", placement = TooltipPlacement.Top),
        viaStepContent = true,
    ) { scope ->
        assertEquals("Title", scope?.title)
        assertEquals(ResolvedPlacement.Top, scope?.placement)
    }

    @Test
    fun `hint scope exposes the resolved placement`() = runComposeUiTest {
        var captured: HintScope? = null
        val state = WaypointHintState(
            hints = listOf(WaypointHint(key = "hint", title = "Hint", placement = TooltipPlacement.Top)),
        )
        setContent {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                WaypointHint(
                    state = state,
                    key = "hint",
                    tooltipContent = { scope ->
                        captured = scope
                        BasicText("hint-tip")
                    },
                ) {
                    Box(Modifier.size(60.dp))
                }
            }
        }
        runOnIdle { state.open("hint") }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("hint-tip").fetchSemanticsNodes().isNotEmpty()
        }
        waitForIdle()

        assertEquals(ResolvedPlacement.Top, captured?.placement)
        assertEquals("Hint", captured?.title)
    }
}
