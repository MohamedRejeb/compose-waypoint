package com.mohamedrejeb.waypoint.sample.demos.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CropSquare
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.waypoint.core.ExperimentalWaypointApi
import com.mohamedrejeb.waypoint.core.LocalWaypointHostId
import com.mohamedrejeb.waypoint.core.WaypointState
import com.mohamedrejeb.waypoint.core.waypointCanvasTarget
import com.mohamedrejeb.waypoint.core.waypointTarget

private val HandleSizeDp = 20.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditorScreen(
    state: WaypointState<EditorTarget>,
    shape: Rect?,
    onRectangleToolClick: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editor") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            ToolPalette(
                state = state,
                onRectangleToolClick = onRectangleToolClick,
            )
            EditorCanvas(
                state = state,
                shape = shape,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(12.dp),
            )
        }
    }
}

@Composable
private fun ToolPalette(
    state: WaypointState<EditorTarget>,
    onRectangleToolClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(72.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 12.dp, horizontal = 8.dp),
    ) {
        Text(
            text = "Tools",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .waypointTarget(state, EditorTarget.RectangleTool),
        ) {
            IconButton(
                onClick = onRectangleToolClick,
                modifier = Modifier.fillMaxSize(),
            ) {
                Icon(
                    imageVector = Icons.Rounded.CropSquare,
                    contentDescription = "Rectangle tool",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Rect",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}

@OptIn(ExperimentalWaypointApi::class)
@Composable
private fun EditorCanvas(
    state: WaypointState<EditorTarget>,
    shape: Rect?,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val handleSizePx = with(density) { HandleSizeDp.toPx() }

    val outlineColor = MaterialTheme.colorScheme.primary
    val fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
    val handleFill = MaterialTheme.colorScheme.primary
    val handleStroke = MaterialTheme.colorScheme.onPrimary

    // Canvas-local rect for the resize handle: a small square pinned to the
    // right-middle edge of the shape.
    fun handleRect(s: Rect): Rect {
        val half = handleSizePx / 2f
        return Rect(
            left = s.right - half,
            top = s.center.y - half,
            right = s.right + half,
            bottom = s.center.y + half,
        )
    }

    val hostId = LocalWaypointHostId.current
    var canvasCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val currentShape by rememberUpdatedState(shape)

    // Demonstrates the raw manual API: capture the Canvas's LayoutCoordinates
    // and the current host id, then translate the canvas-local handle rect to
    // host space via setTargetBoundsFromLocal on every shape change.
    LaunchedEffect(hostId, canvasCoords) {
        snapshotFlow { currentShape }.collect { current ->
            val id = hostId ?: return@collect
            val coords = canvasCoords ?: return@collect
            if (current == null) {
                state.clearTargetBounds(EditorTarget.ResizeHandle)
            } else {
                state.setTargetBoundsFromLocal(
                    key = EditorTarget.ResizeHandle,
                    hostId = id,
                    sourceCoords = coords,
                    localBounds = handleRect(current),
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            state.clearTargetBounds(EditorTarget.ResizeHandle)
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White),
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                // Register the shape rect as a canvas target. The lambda reads
                // the current shape state and waypointCanvasTarget re-registers
                // reactively whenever it changes.
                .waypointCanvasTarget(state, EditorTarget.ShapeOnCanvas) {
                    shape ?: Rect.Zero
                }
                .onGloballyPositioned { canvasCoords = it },
        ) {
            // Light grid background
            val step = 32f
            var x = 0f
            while (x < size.width) {
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f,
                )
                x += step
            }
            var y = 0f
            while (y < size.height) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f,
                )
                y += step
            }

            // Shape
            if (shape != null) {
                drawRect(
                    color = fillColor,
                    topLeft = Offset(shape.left, shape.top),
                    size = Size(shape.width, shape.height),
                )
                drawRect(
                    color = outlineColor,
                    topLeft = Offset(shape.left, shape.top),
                    size = Size(shape.width, shape.height),
                    style = Stroke(width = 2f),
                )

                // Resize handle: filled square on the right-middle edge.
                val handle = handleRect(shape)
                drawRect(
                    color = handleFill,
                    topLeft = Offset(handle.left, handle.top),
                    size = Size(handle.width, handle.height),
                )
                drawRect(
                    color = handleStroke,
                    topLeft = Offset(handle.left, handle.top),
                    size = Size(handle.width, handle.height),
                    style = Stroke(width = 1.5f),
                )
            }
        }
    }
}
