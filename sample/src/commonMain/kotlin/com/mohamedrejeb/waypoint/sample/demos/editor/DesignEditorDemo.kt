package com.mohamedrejeb.waypoint.sample.demos.editor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import kotlinx.serialization.Serializable

internal enum class EditorTarget {
    CreateFab,
    RectangleTool,
    ShapeOnCanvas,
    ResizeHandle,
}

@Serializable
internal sealed interface InternalRoute : NavKey {
    @Serializable data object Projects : InternalRoute
    @Serializable data object Editor : InternalRoute
}

// Canvas-local default shape rect, used both when the Rectangle tool is tapped
// and when the tour advances into the shape step on its behalf.
private val DefaultShapeRect = Rect(left = 120f, top = 100f, right = 280f, bottom = 220f)

@OptIn(ExperimentalWaypointApi::class)
@Composable
fun DesignEditorDemo(onBack: () -> Unit) {
    val backStack = remember { NavBackStack(InternalRoute.Projects as InternalRoute) }
    var shape by remember { mutableStateOf<Rect?>(null) }

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
            interaction = TargetInteraction.ClickToAdvance
            highlightStyle = HighlightStyle.Spotlight(
                shape = SpotlightShape.RoundedRect(12.dp),
            )
            // The CreateFab click advances step 1 (ClickToAdvance), but the
            // overlay intercepts the tap so the FAB's own onClick never fires.
            // beforeShow performs the nav so the tour keeps flowing.
            beforeShow {
                if (backStack.lastOrNull() != InternalRoute.Editor) {
                    backStack.add(InternalRoute.Editor)
                }
            }
        }
        step(EditorTarget.ShapeOnCanvas) {
            title = "Your shape is here"
            description = "Nice, let's resize it next."
            placement = TooltipPlacement.Bottom
            advanceOn = WaypointTrigger.Default
            highlightStyle = HighlightStyle.Spotlight(
                shape = SpotlightShape.RoundedRect(4.dp),
            )
            // Same reasoning as step 2: the tool tap is intercepted, so the
            // shape is created here on tour advance.
            onEnter {
                if (shape == null) shape = DefaultShapeRect
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
        }
    }

    WaypointMaterial3Host(state = state) {
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
                        },
                        onBack = { backStack.removeLastOrNull() },
                    )
                }
            },
        )
    }
}
