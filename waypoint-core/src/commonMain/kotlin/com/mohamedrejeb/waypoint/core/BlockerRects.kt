package com.mohamedrejeb.waypoint.core

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size

/**
 * Decomposes the area of [size] that lies outside all [holes] into
 * non-overlapping rectangles.
 *
 * The area is cut into horizontal bands at every hole's top and bottom edge,
 * and each band is then split around the holes that cross it. Holes are
 * clipped to the area first, holes that overlap each other are merged, and
 * empty holes are ignored. The result is ordered top to bottom, then left to
 * right.
 */
internal fun blockerRects(size: Size, holes: List<Rect>): List<Rect> {
    if (size.width <= 0f || size.height <= 0f) return emptyList()
    val area = Rect(0f, 0f, size.width, size.height)
    val clippedHoles = holes
        .map { it.intersect(area) }
        .filter { it.width > 0f && it.height > 0f }
    if (clippedHoles.isEmpty()) return listOf(area)

    val bandEdges = (clippedHoles.flatMap { listOf(it.top, it.bottom) } + listOf(0f, size.height))
        .distinct()
        .sorted()
    return bandEdges.zipWithNext().flatMap { (top, bottom) ->
        bandRects(top = top, bottom = bottom, width = size.width, holes = clippedHoles)
    }
}

/**
 * Rectangles covering the band between [top] and [bottom] except where a hole
 * crosses it. Band edges sit on hole edges, so a hole that touches the band
 * spans its full height.
 */
private fun bandRects(top: Float, bottom: Float, width: Float, holes: List<Rect>): List<Rect> {
    val crossing = holes
        .filter { it.top < bottom && it.bottom > top }
        .sortedBy { it.left }
    return buildList {
        var x = 0f
        for (hole in crossing) {
            if (hole.left > x) add(Rect(x, top, hole.left, bottom))
            x = maxOf(x, hole.right)
        }
        if (x < width) add(Rect(x, top, width, bottom))
    }
}
