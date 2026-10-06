package com.mohamedrejeb.waypoint.sample.kit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.waypoint.sample.theme.SampleTheme

enum class KitButtonStyle { Filled, Soft }

internal const val DisabledAlpha = 0.4f

/** Pill button. Filled is the one primary action of a screen, Soft is everything else. */
@Composable
fun KitButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: KitButtonStyle = KitButtonStyle.Filled,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    val colors = SampleTheme.colors
    val container = if (style == KitButtonStyle.Filled) colors.accent else colors.surface
    val content = if (style == KitButtonStyle.Filled) colors.onAccent else colors.ink
    Row(
        modifier = modifier
            .alpha(if (enabled) 1f else DisabledAlpha)
            .clip(CircleShape)
            .background(container)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .heightIn(min = 48.dp)
            .padding(horizontal = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(18.dp),
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = content,
        )
    }
}

/** Round icon button on a soft surface. */
@Composable
fun KitIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    container: Color = SampleTheme.colors.surface,
) {
    val colors = SampleTheme.colors
    Box(
        modifier = modifier
            .alpha(if (enabled) 1f else DisabledAlpha)
            .size(40.dp)
            .clip(CircleShape)
            .background(container)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = colors.ink,
            modifier = Modifier.size(20.dp),
        )
    }
}
