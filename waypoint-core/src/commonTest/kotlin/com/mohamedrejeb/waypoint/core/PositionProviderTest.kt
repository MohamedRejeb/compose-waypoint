package com.mohamedrejeb.waypoint.core

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PositionProviderTest {

    // Standard test values
    private val windowSize = IntSize(1000, 800)
    private val tooltipSize = IntSize(200, 100)
    private val spacing = 12f
    private val margin = 16f

    // Target centered in the window — plenty of space on all sides
    private val centeredTarget = Rect(400f, 350f, 600f, 450f)

    // Target near top edge
    private val topEdgeTarget = Rect(400f, 10f, 600f, 60f)

    // Target near bottom edge
    private val bottomEdgeTarget = Rect(400f, 720f, 600f, 790f)

    // Target near left edge
    private val leftEdgeTarget = Rect(10f, 350f, 60f, 450f)

    // Target near right edge
    private val rightEdgeTarget = Rect(940f, 350f, 990f, 450f)

    private fun createProvider(
        targetBounds: Rect,
        placement: TooltipPlacement = TooltipPlacement.Auto,
    ) = WaypointPositionProvider(
        targetBounds = targetBounds,
        requestedPlacement = placement,
        spacingPx = spacing,
        screenMarginPx = margin,
    )

    private fun WaypointPositionProvider.calculate(
        layoutDirection: LayoutDirection = LayoutDirection.Ltr,
    ): IntOffset = calculatePosition(
        anchorBounds = IntRect.Zero, // not used by our provider
        windowSize = windowSize,
        layoutDirection = layoutDirection,
        popupContentSize = tooltipSize,
    )

    // -- Explicit Placement --

    @Test
    fun `bottom placement positions tooltip below target`() {
        val provider = createProvider(centeredTarget, TooltipPlacement.Bottom)

        val offset = provider.calculate()

        assertEquals(ResolvedPlacement.Bottom, provider.resolvedPlacement)
        // Y should be below the target bottom + spacing
        assertEquals((centeredTarget.bottom + spacing).toInt(), offset.y)
    }

    @Test
    fun `top placement positions tooltip above target`() {
        val provider = createProvider(centeredTarget, TooltipPlacement.Top)

        val offset = provider.calculate()

        assertEquals(ResolvedPlacement.Top, provider.resolvedPlacement)
        // Y should be above the target top - tooltip height - spacing
        val expectedY = (centeredTarget.top - tooltipSize.height - spacing).toInt()
        assertEquals(expectedY, offset.y)
    }

    @Test
    fun `end placement positions tooltip to the right in LTR`() {
        val provider = createProvider(centeredTarget, TooltipPlacement.End)

        val offset = provider.calculate(LayoutDirection.Ltr)

        assertEquals(ResolvedPlacement.End, provider.resolvedPlacement)
        // X should be to the right of target + spacing
        assertEquals((centeredTarget.right + spacing).toInt(), offset.x)
    }

    @Test
    fun `start placement positions tooltip to the left in LTR`() {
        val provider = createProvider(centeredTarget, TooltipPlacement.Start)

        val offset = provider.calculate(LayoutDirection.Ltr)

        assertEquals(ResolvedPlacement.Start, provider.resolvedPlacement)
        // X should be to the left of target - tooltip width - spacing
        val expectedX = (centeredTarget.left - tooltipSize.width - spacing).toInt()
        assertEquals(expectedX, offset.x)
    }

    // -- Auto Placement --

    @Test
    fun `auto placement chooses side with most space`() {
        // Centered target: spaceTop=350, spaceBottom=350, spaceStart=400, spaceEnd=400
        // End has 400px (tied with Start, but End is ranked first) → picks End
        val provider = createProvider(centeredTarget, TooltipPlacement.Auto)

        provider.calculate()

        assertEquals(ResolvedPlacement.End, provider.resolvedPlacement)
    }

    @Test
    fun `auto placement chooses bottom for top-edge target`() {
        val provider = createProvider(topEdgeTarget, TooltipPlacement.Auto)

        provider.calculate()

        // Target near top → most space below
        assertEquals(ResolvedPlacement.Bottom, provider.resolvedPlacement)
    }

    @Test
    fun `auto placement chooses top for bottom-edge target`() {
        val provider = createProvider(bottomEdgeTarget, TooltipPlacement.Auto)

        provider.calculate()

        // Target near bottom → most space above
        assertEquals(ResolvedPlacement.Top, provider.resolvedPlacement)
    }

    // -- Flip Logic --

    @Test
    fun `flip to bottom when no space above`() {
        val provider = createProvider(topEdgeTarget, TooltipPlacement.Top)

        provider.calculate()

        // No room above → flips to bottom
        assertEquals(ResolvedPlacement.Bottom, provider.resolvedPlacement)
    }

    @Test
    fun `flip to top when no space below`() {
        val provider = createProvider(bottomEdgeTarget, TooltipPlacement.Bottom)

        provider.calculate()

        // No room below → flips to top
        assertEquals(ResolvedPlacement.Top, provider.resolvedPlacement)
    }

    @Test
    fun `flip to end when no space at start`() {
        val provider = createProvider(leftEdgeTarget, TooltipPlacement.Start)

        provider.calculate()

        // No room at start (left in LTR) → flips to end
        assertEquals(ResolvedPlacement.End, provider.resolvedPlacement)
    }

    @Test
    fun `flip to start when no space at end`() {
        val provider = createProvider(rightEdgeTarget, TooltipPlacement.End)

        provider.calculate()

        // No room at end (right in LTR) → flips to start
        assertEquals(ResolvedPlacement.Start, provider.resolvedPlacement)
    }

    // -- Horizontal Centering --

    @Test
    fun `tooltip is centered on target horizontally for bottom placement`() {
        val provider = createProvider(centeredTarget, TooltipPlacement.Bottom)

        val offset = provider.calculate()

        val expectedX = (centeredTarget.center.x - tooltipSize.width / 2f).toInt()
        assertEquals(expectedX, offset.x)
    }

    @Test
    fun `tooltip is centered on target horizontally for top placement`() {
        val provider = createProvider(centeredTarget, TooltipPlacement.Top)

        val offset = provider.calculate()

        val expectedX = (centeredTarget.center.x - tooltipSize.width / 2f).toInt()
        assertEquals(expectedX, offset.x)
    }

    // -- Edge Clamping --

    @Test
    fun `clamp to left screen edge`() {
        // Target near left edge — tooltip would go off-screen if centered
        val target = Rect(10f, 400f, 50f, 450f)
        val provider = createProvider(target, TooltipPlacement.Bottom)

        val offset = provider.calculate()

        // X should not go below margin
        assertTrue(offset.x >= margin.toInt(), "Tooltip X (${offset.x}) should be >= margin ($margin)")
    }

    @Test
    fun `clamp to right screen edge`() {
        // Target near right edge — tooltip would go off-screen if centered
        val target = Rect(950f, 400f, 990f, 450f)
        val provider = createProvider(target, TooltipPlacement.Bottom)

        val offset = provider.calculate()

        // X + tooltip width should not exceed window width - margin
        val maxX = windowSize.width - tooltipSize.width - margin.toInt()
        assertTrue(offset.x <= maxX, "Tooltip right edge should not exceed screen - margin")
    }

    @Test
    fun `clamp to top screen edge for side placement`() {
        // Target near top — side-placed tooltip would go off-screen if vertically centered
        val target = Rect(400f, 5f, 500f, 30f)
        val provider = createProvider(target, TooltipPlacement.End)

        val offset = provider.calculate()

        assertTrue(offset.y >= margin.toInt(), "Tooltip Y (${offset.y}) should be >= margin ($margin)")
    }

    // -- Resolved Placement Updates --

    @Test
    fun `resolved placement updated after calculation`() {
        val provider = createProvider(centeredTarget, TooltipPlacement.Top)

        // Before calculation, default is Bottom
        assertEquals(ResolvedPlacement.Bottom, provider.resolvedPlacement)

        provider.calculate()

        assertEquals(ResolvedPlacement.Top, provider.resolvedPlacement)
    }

    // -- RTL Layout --

    @Test
    fun `RTL layout mirrors start to right side`() {
        val provider = createProvider(centeredTarget, TooltipPlacement.Start)

        val offset = provider.calculate(LayoutDirection.Rtl)

        assertEquals(ResolvedPlacement.Start, provider.resolvedPlacement)
        // In RTL, Start = right side, so tooltip goes to the right of target
        assertEquals((centeredTarget.right + spacing).toInt(), offset.x)
    }

    @Test
    fun `RTL layout mirrors end to left side`() {
        val provider = createProvider(centeredTarget, TooltipPlacement.End)

        val offset = provider.calculate(LayoutDirection.Rtl)

        assertEquals(ResolvedPlacement.End, provider.resolvedPlacement)
        // In RTL, End = left side, so tooltip goes to the left of target
        val expectedX = (centeredTarget.left - tooltipSize.width - spacing).toInt()
        assertEquals(expectedX, offset.x)
    }

    // -- Arrow Offsets --

    @Test
    fun `arrow horizontal offset tracks target center for bottom placement`() {
        val provider = createProvider(centeredTarget, TooltipPlacement.Bottom)

        val offset = provider.calculate()

        // Arrow should point to the center of the target
        val expectedArrowX = centeredTarget.center.x - offset.x.toFloat()
        assertEquals(expectedArrowX, provider.arrowHorizontalOffset, 0.1f)
    }

    @Test
    fun `arrow vertical offset tracks target center for end placement`() {
        val provider = createProvider(centeredTarget, TooltipPlacement.End)

        val offset = provider.calculate()

        // Arrow should point to the center of the target vertically
        val expectedArrowY = centeredTarget.center.y - offset.y.toFloat()
        assertEquals(expectedArrowY, provider.arrowVerticalOffset, 0.1f)
    }
}
