package com.mohamedrejeb.waypoint.sample.lab

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.waypoint.core.HighlightStyle
import com.mohamedrejeb.waypoint.core.OverlayClickBehavior
import com.mohamedrejeb.waypoint.core.SpotlightEffect
import com.mohamedrejeb.waypoint.core.SpotlightPadding
import com.mohamedrejeb.waypoint.core.SpotlightShape
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LabConfigTest {

    private val accent = Color.Blue
    private val scrim = Color.Black

    private fun LabConfig.style() = toHighlightStyle(accent, scrim)

    @Test
    fun shapes() {
        assertEquals(SpotlightShape.RoundedRect(16.dp), LabConfig(shape = LabShape.Rounded).toSpotlightShape())
        assertEquals(SpotlightShape.Rect, LabConfig(shape = LabShape.Rect).toSpotlightShape())
        assertEquals(SpotlightShape.Circle, LabConfig(shape = LabShape.Circle).toSpotlightShape())
        assertEquals(SpotlightShape.Pill, LabConfig(shape = LabShape.Pill).toSpotlightShape())
    }

    @Test
    fun spotlightCarriesEveryOption() {
        val style = LabConfig(
            shape = LabShape.Pill,
            padding = 12f,
            scrimAlpha = 0.3f,
        ).style()
        assertIs<HighlightStyle.Spotlight>(style)
        assertEquals(SpotlightShape.Pill, style.shape)
        assertEquals(SpotlightPadding(all = 12.dp), style.padding)
        assertEquals(0.3f, style.overlayAlpha)
        assertEquals(scrim, style.overlayColor)
        assertEquals(SpotlightEffect.None, style.effect)
    }

    @Test
    fun spotlightEffects() {
        val glow = LabConfig(effect = LabEffect.Glow).style() as HighlightStyle.Spotlight
        assertEquals(accent, assertIs<SpotlightEffect.Glow>(glow.effect).color)
        val soft = LabConfig(effect = LabEffect.SoftEdge).style() as HighlightStyle.Spotlight
        assertIs<SpotlightEffect.SoftEdge>(soft.effect)
    }

    @Test
    fun otherHighlights() {
        val pulse = LabConfig(highlight = LabHighlight.Pulse, shape = LabShape.Circle, padding = 8f).style()
        assertEquals(
            HighlightStyle.Pulse(color = accent, shape = SpotlightShape.Circle, padding = SpotlightPadding(all = 8.dp)),
            pulse,
        )
        val border = LabConfig(highlight = LabHighlight.Border, shape = LabShape.Rect).style()
        assertEquals(
            HighlightStyle.Border(color = accent, shape = SpotlightShape.Rect, padding = SpotlightPadding(all = 4.dp)),
            border,
        )
        assertEquals(HighlightStyle.Ripple(color = accent), LabConfig(highlight = LabHighlight.Ripple).style())
        assertEquals(HighlightStyle.None, LabConfig(highlight = LabHighlight.None).style())
    }

    @Test
    fun blockOutside() {
        assertNull(LabConfig(block = LabBlock.Default).toBlockOutside())
        assertEquals(true, LabConfig(block = LabBlock.On).toBlockOutside())
        assertEquals(false, LabConfig(block = LabBlock.Off).toBlockOutside())
    }

    @Test
    fun overlayClick() {
        assertEquals(OverlayClickBehavior.Nothing, LabConfig(overlayClick = LabOverlayClick.Nothing).toOverlayClickBehavior())
        assertEquals(OverlayClickBehavior.NextStep, LabConfig(overlayClick = LabOverlayClick.NextStep).toOverlayClickBehavior())
        assertEquals(OverlayClickBehavior.Dismiss, LabConfig(overlayClick = LabOverlayClick.Dismiss).toOverlayClickBehavior())
    }

    @Test
    fun optionsFollowTheHighlight() {
        assertTrue(LabConfig(highlight = LabHighlight.Spotlight).supportsSpotlightOptions)
        assertFalse(LabConfig(highlight = LabHighlight.Pulse).supportsSpotlightOptions)
        assertTrue(LabConfig(highlight = LabHighlight.Border).supportsShape)
        assertFalse(LabConfig(highlight = LabHighlight.Ripple).supportsShape)
        assertFalse(LabConfig(highlight = LabHighlight.None).supportsShape)
    }
}
