package com.mohamedrejeb.waypoint.core

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BlockerRectsTest {

    private val size = Size(100f, 100f)
    private val area = Rect(0f, 0f, 100f, 100f)

    /**
     * Checks the two properties that matter for touch blocking: blockers never
     * overlap a hole or each other, and together with the holes they cover the
     * whole area (checked by area and by sampling points).
     */
    private fun assertExactCover(holes: List<Rect>, rects: List<Rect>) {
        rects.forEach { rect ->
            assertTrue(rect.width > 0f && rect.height > 0f, "empty blocker $rect")
            assertEquals(rect, rect.intersect(area), "blocker $rect leaves the area")
            holes.forEach { hole ->
                assertFalse(rect.overlaps(hole), "blocker $rect overlaps hole $hole")
            }
        }
        for (i in rects.indices) {
            for (j in i + 1 until rects.size) {
                assertFalse(rects[i].overlaps(rects[j]), "blockers ${rects[i]} and ${rects[j]} overlap")
            }
        }
        for (x in 0 until 100) {
            for (y in 0 until 100) {
                val point = Offset(x + 0.5f, y + 0.5f)
                val inHole = holes.any { it.contains(point) }
                val inBlocker = rects.any { it.contains(point) }
                assertEquals(!inHole, inBlocker, "point $point: inHole=$inHole inBlocker=$inBlocker")
            }
        }
    }

    @Test
    fun `no holes yields one rect covering everything`() {
        assertEquals(listOf(area), blockerRects(size, emptyList()))
    }

    @Test
    fun `empty size yields nothing`() {
        assertEquals(emptyList(), blockerRects(Size.Zero, listOf(Rect(1f, 1f, 2f, 2f))))
        assertEquals(emptyList(), blockerRects(Size(0f, 50f), emptyList()))
    }

    @Test
    fun `single centered hole yields four rects in reading order`() {
        val hole = Rect(40f, 30f, 60f, 70f)

        val rects = blockerRects(size, listOf(hole))

        assertEquals(
            listOf(
                Rect(0f, 0f, 100f, 30f),
                Rect(0f, 30f, 40f, 70f),
                Rect(60f, 30f, 100f, 70f),
                Rect(0f, 70f, 100f, 100f),
            ),
            rects,
        )
        assertExactCover(listOf(hole), rects)
    }

    @Test
    fun `hole touching the edges drops the empty sides`() {
        val hole = Rect(0f, 0f, 30f, 100f)

        val rects = blockerRects(size, listOf(hole))

        assertEquals(listOf(Rect(30f, 0f, 100f, 100f)), rects)
    }

    @Test
    fun `hole extending past the area is clipped`() {
        val hole = Rect(80f, -20f, 150f, 20f)

        val rects = blockerRects(size, listOf(hole))

        assertEquals(
            listOf(
                Rect(0f, 0f, 80f, 20f),
                Rect(0f, 20f, 100f, 100f),
            ),
            rects,
        )
    }

    @Test
    fun `hole covering everything yields nothing`() {
        assertEquals(emptyList(), blockerRects(size, listOf(Rect(-5f, -5f, 200f, 200f))))
    }

    @Test
    fun `hole outside the area and empty holes are ignored`() {
        val rects = blockerRects(
            size,
            listOf(Rect(200f, 200f, 300f, 300f), Rect(50f, 50f, 50f, 80f), Rect.Zero),
        )

        assertEquals(listOf(area), rects)
    }

    @Test
    fun `two holes side by side split the shared band three ways`() {
        val holes = listOf(Rect(10f, 40f, 30f, 60f), Rect(70f, 40f, 90f, 60f))

        val rects = blockerRects(size, holes)

        assertEquals(
            listOf(
                Rect(0f, 0f, 100f, 40f),
                Rect(0f, 40f, 10f, 60f),
                Rect(30f, 40f, 70f, 60f),
                Rect(90f, 40f, 100f, 60f),
                Rect(0f, 60f, 100f, 100f),
            ),
            rects,
        )
        assertExactCover(holes, rects)
    }

    @Test
    fun `vertically staggered holes are cut into bands at every hole edge`() {
        val holes = listOf(Rect(10f, 10f, 40f, 50f), Rect(60f, 30f, 90f, 80f))

        val rects = blockerRects(size, holes)

        assertExactCover(holes, rects)
    }

    @Test
    fun `overlapping holes are merged`() {
        val holes = listOf(Rect(20f, 20f, 60f, 60f), Rect(40f, 40f, 80f, 80f))

        val rects = blockerRects(size, holes)

        assertExactCover(holes, rects)
    }

    @Test
    fun `hole order does not matter`() {
        val a = Rect(10f, 10f, 40f, 50f)
        val b = Rect(60f, 30f, 90f, 80f)

        assertEquals(blockerRects(size, listOf(a, b)), blockerRects(size, listOf(b, a)))
    }

    @Test
    fun `nested hole changes nothing`() {
        val outer = Rect(20f, 20f, 80f, 80f)
        val inner = Rect(40f, 40f, 60f, 60f)

        val rects = blockerRects(size, listOf(outer, inner))

        assertExactCover(listOf(outer), rects)
    }
}
