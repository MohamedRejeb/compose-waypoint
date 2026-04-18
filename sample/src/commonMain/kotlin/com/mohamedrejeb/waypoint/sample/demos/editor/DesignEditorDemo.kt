package com.mohamedrejeb.waypoint.sample.demos.editor

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.mohamedrejeb.waypoint.core.ExperimentalWaypointApi
import com.mohamedrejeb.waypoint.core.HighlightStyle
import com.mohamedrejeb.waypoint.core.SpotlightShape
import com.mohamedrejeb.waypoint.core.TargetInteraction
import com.mohamedrejeb.waypoint.core.TooltipPlacement
import com.mohamedrejeb.waypoint.core.WaypointTrigger
import com.mohamedrejeb.waypoint.core.rememberWaypointState
import com.mohamedrejeb.waypoint.material3.WaypointMaterial3Host
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

internal enum class EditorTarget {
    CreateFab,
    RectangleTool,
    ResizeHandle,
}

@Serializable
internal sealed interface InternalRoute : NavKey {
    @Serializable data object Projects : InternalRoute
    @Serializable data object Editor : InternalRoute
}

// Canvas-local default shape rect, dropped when the Rectangle tool is tapped.
private val DefaultShapeRect = Rect(left = 120f, top = 100f, right = 280f, bottom = 220f)

@OptIn(ExperimentalWaypointApi::class)
@Composable
fun DesignEditorDemo(onBack: () -> Unit) {
    val backStack = remember { NavBackStack(InternalRoute.Projects as InternalRoute) }
    var shape by remember { mutableStateOf<Rect?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val state = rememberWaypointState {
        step(EditorTarget.CreateFab) {
            title = "Start your first design"
            description = "Tap here to open a new design canvas."
            placement = TooltipPlacement.Top
            interaction = TargetInteraction.ClickToAdvance
            highlightStyle = HighlightStyle.Spotlight(
                shape = SpotlightShape.RoundedRect(16.dp),
            )
        }
        step(EditorTarget.RectangleTool) {
            title = "Add a shape"
            description = "Tap the Rectangle tool to drop a shape on the canvas."
            placement = TooltipPlacement.End
            highlightStyle = HighlightStyle.None
            // Editor-screen steps use the top StepsHeader instead of a popup
            // tooltip. An empty content composable suppresses the default one.
            content { }
            // The CreateFab click advances step 1 (ClickToAdvance), but the
            // overlay intercepts the tap so the FAB's own onClick never fires.
            // beforeShow performs the nav so the tour keeps flowing.
            beforeShow {
                if (backStack.lastOrNull() != InternalRoute.Editor) {
                    backStack.add(InternalRoute.Editor)
                }
            }
        }
        step(EditorTarget.ResizeHandle) {
            title = "Drag to resize"
            description = "Drag this handle to resize the shape."
            placement = TooltipPlacement.End
            advanceOn = WaypointTrigger.Default
            highlightStyle = HighlightStyle.Custom { _, animatedBounds ->
                DragGestureHighlight(animatedBounds = animatedBounds)
            }
            content { }
        }
    }

    WaypointMaterial3Host(
        state = state,
        onTourComplete = { showSuccessDialog = true },
    ) {
        NavDisplay(
            backStack = backStack,
            entryProvider = entryProvider {
                entry<InternalRoute.Projects> {
                    ProjectsScreen(
                        state = state,
                        onCreateClick = { backStack.add(InternalRoute.Editor) },
                        onBack = onBack,
                        onStartTour = { state.start() },
                    )
                }
                entry<InternalRoute.Editor> {
                    EditorScreen(
                        state = state,
                        shape = shape,
                        onRectangleToolClick = {
                            if (shape == null) shape = DefaultShapeRect
                            if (state.currentStep?.targetKey == EditorTarget.RectangleTool) {
                                state.next()
                            }
                        },
                        onShapeChange = { shape = it },
                        onShapeResizeEnd = {
                            if (state.currentStep?.targetKey == EditorTarget.ResizeHandle) {
                                scope.launch {
                                    delay(500)
                                    if (state.currentStep?.targetKey == EditorTarget.ResizeHandle) {
                                        state.next()
                                    }
                                }
                            }
                        },
                        onBack = { backStack.removeLastOrNull() },
                    )
                }
            },
        )

        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = { showSuccessDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                    )
                },
                title = { Text("Tour complete") },
                text = {
                    Text(
                        "You resized your first shape. Jump back to projects to start another design.",
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            shape = null
                            while (backStack.size > 1) backStack.removeLastOrNull()
                            showSuccessDialog = false
                        },
                    ) {
                        Text("Back to projects")
                    }
                },
            )
        }
    }
}
