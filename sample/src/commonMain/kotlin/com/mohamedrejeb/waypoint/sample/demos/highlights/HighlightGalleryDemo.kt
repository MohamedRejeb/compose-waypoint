package com.mohamedrejeb.waypoint.sample.demos.highlights

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.waypoint.core.HighlightStyle
import com.mohamedrejeb.waypoint.core.OverlayClickBehavior
import com.mohamedrejeb.waypoint.core.SpotlightPadding
import com.mohamedrejeb.waypoint.core.SpotlightShape
import com.mohamedrejeb.waypoint.core.rememberWaypointState
import com.mohamedrejeb.waypoint.core.waypointTarget
import com.mohamedrejeb.waypoint.material3.WaypointMaterial3Host
import com.mohamedrejeb.waypoint.sample.components.DemoScaffold

private val VioletPrimary = Color(0xFF7C3AED)
private val TealSecondary = Color(0xFF14B8A6)
private val AmberTertiary = Color(0xFFF59E0B)

private data class HighlightCardData(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconColor: Color,
    val highlightStyle: HighlightStyle,
    val tooltipTitle: String,
    val tooltipDescription: String,
)

private val highlightCards = listOf(
    HighlightCardData(
        title = "Spotlight (Circle)",
        description = "Classic circle cutout with dimmed overlay",
        icon = Icons.Filled.Circle,
        iconColor = Color(0xFF3B82F6),
        highlightStyle = HighlightStyle.Spotlight(shape = SpotlightShape.Circle),
        tooltipTitle = "Circle Spotlight",
        tooltipDescription = "A circular cutout reveals the target element.",
    ),
    HighlightCardData(
        title = "Spotlight (RoundedRect)",
        description = "Rounded rectangle with custom padding",
        icon = Icons.Filled.Star,
        iconColor = Color(0xFF8B5CF6),
        highlightStyle = HighlightStyle.Spotlight(
            shape = SpotlightShape.RoundedRect(12.dp),
            padding = SpotlightPadding(8.dp),
        ),
        tooltipTitle = "Rounded Rectangle",
        tooltipDescription = "A rounded rectangle cutout with extra padding.",
    ),
    HighlightCardData(
        title = "Spotlight (Pill)",
        description = "Capsule shape that hugs the target",
        icon = Icons.Filled.FavoriteBorder,
        iconColor = Color(0xFFEC4899),
        highlightStyle = HighlightStyle.Spotlight(shape = SpotlightShape.Pill),
        tooltipTitle = "Pill Spotlight",
        tooltipDescription = "A capsule-shaped cutout hugging the target.",
    ),
    HighlightCardData(
        title = "Pulse",
        description = "Animated breathing pulse around target",
        icon = Icons.Filled.LightMode,
        iconColor = TealSecondary,
        highlightStyle = HighlightStyle.Pulse(
            color = TealSecondary,
            shape = SpotlightShape.Circle,
            filled = true,
        ),
        tooltipTitle = "Pulse Highlight",
        tooltipDescription = "An animated breathing pulse draws attention.",
    ),
    HighlightCardData(
        title = "Border",
        description = "Static colored border outline",
        icon = Icons.Filled.RadioButtonUnchecked,
        iconColor = VioletPrimary,
        highlightStyle = HighlightStyle.Border(
            color = VioletPrimary,
            borderWidth = 3.dp,
            shape = SpotlightShape.RoundedRect(8.dp),
        ),
        tooltipTitle = "Border Highlight",
        tooltipDescription = "A static colored border outlines the target.",
    ),
    HighlightCardData(
        title = "Ripple",
        description = "Expanding concentric rings",
        icon = Icons.Filled.Waves,
        iconColor = AmberTertiary,
        highlightStyle = HighlightStyle.Ripple(
            color = AmberTertiary,
            ringCount = 3,
        ),
        tooltipTitle = "Ripple Highlight",
        tooltipDescription = "Concentric rings expand outward from the target.",
    ),
    HighlightCardData(
        title = "None",
        description = "Tooltip only, no visual highlight",
        icon = Icons.Filled.VisibilityOff,
        iconColor = Color(0xFF6B7280),
        highlightStyle = HighlightStyle.None,
        tooltipTitle = "No Highlight",
        tooltipDescription = "Only the tooltip is shown, with no visual highlight.",
    ),
    HighlightCardData(
        title = "Custom",
        description = "Fully custom composable",
        icon = Icons.Filled.AutoAwesome,
        iconColor = Color(0xFF7C3AED),
        highlightStyle = HighlightStyle.Custom { _, animatedBounds ->
            val infiniteTransition = rememberInfiniteTransition()
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
            )
            Canvas(Modifier.fillMaxSize()) {
                drawRoundRect(
                    color = Color(0xFF7C3AED).copy(alpha = alpha),
                    topLeft = animatedBounds.topLeft - Offset(8f, 8f),
                    size = Size(animatedBounds.width + 16f, animatedBounds.height + 16f),
                    cornerRadius = CornerRadius(16f),
                    style = Stroke(width = 4f),
                )
            }
        },
        tooltipTitle = "Custom Highlight",
        tooltipDescription = "A fully custom animated glowing ring.",
    ),
)

private enum class GalleryTarget { Target }

@Composable
fun HighlightGalleryDemo(onBack: () -> Unit) {
    DemoScaffold(
        title = "Highlight Styles",
        description = "Visual showcase of every highlight style and shape",
        onBack = onBack,
        onStartTour = {},
        fabVisible = false,
    ) { padding ->
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(280.dp),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalItemSpacing = 12.dp,
            modifier = Modifier.fillMaxSize(),
        ) {
            items(highlightCards) { cardData ->
                HighlightCard(cardData)
            }
        }
    }
}

@Composable
private fun HighlightCard(data: HighlightCardData) {
    val state = rememberWaypointState {
        step(GalleryTarget.Target) {
            title = data.tooltipTitle
            description = data.tooltipDescription
            highlightStyle = data.highlightStyle
        }
    }

    WaypointMaterial3Host(
        state = state,
        overlayClickBehavior = OverlayClickBehavior.Dismiss,
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    text = data.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = data.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .waypointTarget(state, GalleryTarget.Target),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(data.iconColor.copy(alpha = 0.12f)),
                    ) {
                        Icon(
                            imageVector = data.icon,
                            contentDescription = null,
                            tint = data.iconColor,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                FilledTonalButton(
                    onClick = { state.start() },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Try It")
                }
            }
        }
    }
}
