package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.unit.toSize
import kotlin.math.max

/** Slack for comparing laid out sizes, which are rounded to whole pixels. */
private const val VisibilityTolerancePx = 1f

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
internal suspend fun BringIntoViewRequester.bringTargetIntoView(target: LayoutCoordinates?) {
    if (target == null || !target.isAttached) {
        bringIntoView()
        return
    }
    if (target.isFullyVisible()) return

    bringIntoView(centeringRect(target.size.toSize(), target.scrollViewportSize()))
    if (target.isAttached && !target.isFullyVisible()) bringIntoView()
}

/**
 * The rectangle to bring into view, in the target's own coordinates, so that
 * a target of size [target] ends up in the middle of a [viewport]: the target
 * grown equally on both sides until it is as large as the viewport. On an
 * axis where the target is larger than the viewport it is left as is.
 */
internal fun centeringRect(target: Size, viewport: Size): Rect {
    val extraX = max(0f, (viewport.width - target.width) / 2f)
    val extraY = max(0f, (viewport.height - target.height) / 2f)
    return Rect(
        left = -extraX,
        top = -extraY,
        right = target.width + extraX,
        bottom = target.height + extraY,
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

/** Whether no part of this layout is clipped away by its ancestors or the window. */
private fun LayoutCoordinates.isFullyVisible(): Boolean {
    val visible = findRootCoordinates().localBoundingBoxOf(this, clipBounds = true)
    return visible.width >= size.width - VisibilityTolerancePx &&
        visible.height >= size.height - VisibilityTolerancePx
}
