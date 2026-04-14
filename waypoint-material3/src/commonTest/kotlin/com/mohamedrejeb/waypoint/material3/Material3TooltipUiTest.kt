package com.mohamedrejeb.waypoint.material3

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.mohamedrejeb.waypoint.core.ResolvedPlacement
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class Material3TooltipUiTest {

    private fun firstStepScope(
        totalSteps: Int = 3,
        onNext: () -> Unit = {},
        onSkip: () -> Unit = {},
    ) = TestStepScope(
        currentStepIndex = 0,
        totalSteps = totalSteps,
        isFirstStep = true,
        isLastStep = totalSteps == 1,
        onNext = onNext,
        onSkip = onSkip,
    )

    private fun middleStepScope(
        index: Int = 1,
        totalSteps: Int = 3,
        onNext: () -> Unit = {},
        onPrevious: () -> Unit = {},
        onSkip: () -> Unit = {},
    ) = TestStepScope(
        currentStepIndex = index,
        totalSteps = totalSteps,
        isFirstStep = false,
        isLastStep = false,
        onNext = onNext,
        onPrevious = onPrevious,
        onSkip = onSkip,
    )

    private fun lastStepScope(
        totalSteps: Int = 3,
        onNext: () -> Unit = {},
        onPrevious: () -> Unit = {},
        onSkip: () -> Unit = {},
    ) = TestStepScope(
        currentStepIndex = totalSteps - 1,
        totalSteps = totalSteps,
        isFirstStep = totalSteps == 1,
        isLastStep = true,
        onNext = onNext,
        onPrevious = onPrevious,
        onSkip = onSkip,
    )

    // -- Content Rendering --

    @Test
    fun `shows title text`() = runComposeUiTest {
        setContent {
            MaterialTheme {
                WaypointMaterial3Tooltip(
                    stepScope = firstStepScope(),
                    resolvedPlacement = ResolvedPlacement.Bottom,
                    title = "Welcome",
                    description = null,
                )
            }
        }

        onNodeWithText("Welcome").assertIsDisplayed()
    }

    @Test
    fun `shows description text`() = runComposeUiTest {
        setContent {
            MaterialTheme {
                WaypointMaterial3Tooltip(
                    stepScope = firstStepScope(),
                    resolvedPlacement = ResolvedPlacement.Bottom,
                    title = null,
                    description = "This is a description",
                )
            }
        }

        onNodeWithText("This is a description").assertIsDisplayed()
    }

    @Test
    fun `shows both title and description`() = runComposeUiTest {
        setContent {
            MaterialTheme {
                WaypointMaterial3Tooltip(
                    stepScope = firstStepScope(),
                    resolvedPlacement = ResolvedPlacement.Bottom,
                    title = "Title",
                    description = "Description",
                )
            }
        }

        onNodeWithText("Title").assertIsDisplayed()
        onNodeWithText("Description").assertIsDisplayed()
    }

    // -- Progress Indicator --

    @Test
    fun `shows progress indicator`() = runComposeUiTest {
        setContent {
            MaterialTheme {
                WaypointMaterial3Tooltip(
                    stepScope = firstStepScope(totalSteps = 5),
                    resolvedPlacement = ResolvedPlacement.Bottom,
                    title = "Step",
                    description = null,
                    showProgress = true,
                )
            }
        }

        onNodeWithText("1 of 5").assertIsDisplayed()
    }

    @Test
    fun `hides progress indicator when disabled`() = runComposeUiTest {
        setContent {
            MaterialTheme {
                WaypointMaterial3Tooltip(
                    stepScope = firstStepScope(),
                    resolvedPlacement = ResolvedPlacement.Bottom,
                    title = "Step",
                    description = null,
                    showProgress = false,
                )
            }
        }

        onNodeWithText("1 of 3").assertDoesNotExist()
    }

    @Test
    fun `progress updates for middle step`() = runComposeUiTest {
        setContent {
            MaterialTheme {
                WaypointMaterial3Tooltip(
                    stepScope = middleStepScope(index = 2, totalSteps = 4),
                    resolvedPlacement = ResolvedPlacement.Bottom,
                    title = "Middle",
                    description = null,
                )
            }
        }

        onNodeWithText("3 of 4").assertIsDisplayed()
    }

    // -- Button Visibility --

    @Test
    fun `hides back button on first step`() = runComposeUiTest {
        setContent {
            MaterialTheme {
                WaypointMaterial3Tooltip(
                    stepScope = firstStepScope(),
                    resolvedPlacement = ResolvedPlacement.Bottom,
                    title = "First",
                    description = null,
                )
            }
        }

        onNodeWithText("Back").assertDoesNotExist()
        onNodeWithText("Next").assertIsDisplayed()
        onNodeWithText("Skip").assertIsDisplayed()
    }

    @Test
    fun `shows back button on second step`() = runComposeUiTest {
        setContent {
            MaterialTheme {
                WaypointMaterial3Tooltip(
                    stepScope = middleStepScope(),
                    resolvedPlacement = ResolvedPlacement.Bottom,
                    title = "Middle",
                    description = null,
                )
            }
        }

        onNodeWithText("Back").assertIsDisplayed()
        onNodeWithText("Next").assertIsDisplayed()
    }

    @Test
    fun `shows Finish text on last step`() = runComposeUiTest {
        setContent {
            MaterialTheme {
                WaypointMaterial3Tooltip(
                    stepScope = lastStepScope(),
                    resolvedPlacement = ResolvedPlacement.Bottom,
                    title = "Last",
                    description = null,
                )
            }
        }

        onNodeWithText("Finish").assertIsDisplayed()
        onNodeWithText("Next").assertDoesNotExist()
    }

    @Test
    fun `skip button is always visible`() = runComposeUiTest {
        // Check on last step too
        setContent {
            MaterialTheme {
                WaypointMaterial3Tooltip(
                    stepScope = lastStepScope(),
                    resolvedPlacement = ResolvedPlacement.Bottom,
                    title = "Last",
                    description = null,
                )
            }
        }

        onNodeWithText("Skip").assertIsDisplayed()
    }

    // -- Custom Button Text --

    @Test
    fun `uses custom button text`() = runComposeUiTest {
        setContent {
            MaterialTheme {
                WaypointMaterial3Tooltip(
                    stepScope = lastStepScope(),
                    resolvedPlacement = ResolvedPlacement.Bottom,
                    title = "Done",
                    description = null,
                    skipText = "Dismiss",
                    backText = "Previous",
                    finishText = "Got it!",
                )
            }
        }

        onNodeWithText("Dismiss").assertIsDisplayed()
        onNodeWithText("Previous").assertIsDisplayed()
        onNodeWithText("Got it!").assertIsDisplayed()
    }

    // -- Button Actions --

    @Test
    fun `next button triggers onNext`() = runComposeUiTest {
        var nextCalled = false
        setContent {
            MaterialTheme {
                WaypointMaterial3Tooltip(
                    stepScope = firstStepScope(onNext = { nextCalled = true }),
                    resolvedPlacement = ResolvedPlacement.Bottom,
                    title = "Test",
                    description = null,
                )
            }
        }

        onNodeWithText("Next").performClick()

        assertTrue(nextCalled)
    }

    @Test
    fun `skip button triggers onSkip`() = runComposeUiTest {
        var skipCalled = false
        setContent {
            MaterialTheme {
                WaypointMaterial3Tooltip(
                    stepScope = firstStepScope(onSkip = { skipCalled = true }),
                    resolvedPlacement = ResolvedPlacement.Bottom,
                    title = "Test",
                    description = null,
                )
            }
        }

        onNodeWithText("Skip").performClick()

        assertTrue(skipCalled)
    }

    @Test
    fun `back button triggers onPrevious`() = runComposeUiTest {
        var previousCalled = false
        setContent {
            MaterialTheme {
                WaypointMaterial3Tooltip(
                    stepScope = middleStepScope(onPrevious = { previousCalled = true }),
                    resolvedPlacement = ResolvedPlacement.Bottom,
                    title = "Test",
                    description = null,
                )
            }
        }

        onNodeWithText("Back").performClick()

        assertTrue(previousCalled)
    }

    @Test
    fun `finish button triggers onNext`() = runComposeUiTest {
        var nextCalled = false
        setContent {
            MaterialTheme {
                WaypointMaterial3Tooltip(
                    stepScope = lastStepScope(onNext = { nextCalled = true }),
                    resolvedPlacement = ResolvedPlacement.Bottom,
                    title = "Test",
                    description = null,
                )
            }
        }

        onNodeWithText("Finish").performClick()

        assertTrue(nextCalled)
    }
}
