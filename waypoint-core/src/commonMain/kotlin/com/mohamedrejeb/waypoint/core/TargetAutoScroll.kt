package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.unit.toSize
import kotlin.math.max

/** Slack for comparing laid out positions, which are rounded to whole pixels. */
private const val VisibilityTolerancePx = 1f

/**
 * Where a target is laid out, for working out how far to scroll it.
 *
 * @param coordinates the layout that carries the target modifier
 * @param localBounds the target's rectangle inside that layout when it is
 *   only a part of it (a shape on a canvas), null when it is the whole layout
 */
internal class TargetLayout(
    val coordinates: LayoutCoordinates,
    val localBounds: (() -> Rect)? = null,
) {
    fun bounds(): Rect = localBounds?.invoke() ?: Rect(Offset.Zero, coordinates.size.toSize())
}

/**
 * Scrolls the target laid out at [target] into view, to the middle of the
 * scroll container around it rather than to the nearest edge, so there is
 * room for the tooltip on either side.
 *
 * A target that is already fully visible is not moved. When the target's
 * layout is not known, or centering would leave it partly hidden (nested
 * containers that disagree, for instance), this falls back to the plain
 * "nearest edge" scroll.
 */
internal suspend fun BringIntoViewRequester.bringTargetIntoView(target: TargetLayout?) {
    val layout = target?.coordinates
    if (layout == null || !layout.isAttached) {
        bringIntoView()
        return
    }
    val bounds = target.bounds()
    if (layout.isFullyVisible(bounds)) return

    bringIntoView(centeringRect(bounds, layout.scrollViewportSize()))
    if (layout.isAttached && !layout.isFullyVisible(bounds)) bringIntoView(bounds)
}

/**
 * The rectangle to bring into view so that [target] ends up in the middle of
 * a [viewport]: the target grown equally on both sides until it is as large
 * as the viewport. On an axis where the target is larger than the viewport it
 * is left as is.
 */
internal fun centeringRect(target: Rect, viewport: Size): Rect {
    val extraX = max(0f, (viewport.width - target.width) / 2f)
    val extraY = max(0f, (viewport.height - target.height) / 2f)
    return Rect(
        left = target.left - extraX,
        top = target.top - extraY,
        right = target.right + extraX,
        bottom = target.bottom + extraY,
    )
}

/**
 * The size of the window the target scrolls in. Walking outwards, the first
 * layout that is smaller than what it wraps on an axis is the viewport of a
 * scroll container on that axis. An axis with no such layout uses the root.
 */
private fun LayoutCoordinates.scrollViewportSize(): Size {
    var width: Int? = null
    var height: Int? = null
    var child = this
    var parent = parentCoordinates
    while (parent != null && (width == null || height == null)) {
        if (width == null && child.size.width > parent.size.width) width = parent.size.width
        if (height == null && child.size.height > parent.size.height) height = parent.size.height
        child = parent
        parent = parent.parentCoordinates
    }
    val root = findRootCoordinates().size
    return Size(
        width = (width ?: root.width).toFloat(),
        height = (height ?: root.height).toFloat(),
    )
}

/**
 * Whether no part of [bounds], a rectangle inside this layout, is clipped
 * away by the layout's ancestors or the window.
 */
private fun LayoutCoordinates.isFullyVisible(bounds: Rect): Boolean {
    val root = findRootCoordinates()
    val visible = root.localBoundingBoxOf(this, clipBounds = true)
    val topLeft = root.localPositionOf(this, bounds.topLeft)
    val bottomRight = root.localPositionOf(this, bounds.bottomRight)
    return topLeft.x >= visible.left - VisibilityTolerancePx &&
        topLeft.y >= visible.top - VisibilityTolerancePx &&
        bottomRight.x <= visible.right + VisibilityTolerancePx &&
        bottomRight.y <= visible.bottom + VisibilityTolerancePx
}
