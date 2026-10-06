package com.mohamedrejeb.waypoint.sample.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class SampleColorsTest {

    private fun contrast(a: Color, b: Color): Float {
        val lighter = maxOf(a.luminance(), b.luminance())
        val darker = minOf(a.luminance(), b.luminance())
        return (lighter + 0.05f) / (darker + 0.05f)
    }

    @Test
    fun themesDifferWhereItMatters() {
        assertNotEquals(LightSampleColors.background, DarkSampleColors.background)
        assertNotEquals(LightSampleColors.accent, DarkSampleColors.accent)
        assertNotEquals(LightSampleColors.tooltipContainer, DarkSampleColors.tooltipContainer)
        assertFalse(LightSampleColors.isDark)
        assertTrue(DarkSampleColors.isDark)
    }

    @Test
    fun accentIsReadableOnBackground() {
        listOf(LightSampleColors, DarkSampleColors).forEach { colors ->
            assertTrue(contrast(colors.accent, colors.background) >= 3f)
            assertTrue(contrast(colors.onAccent, colors.accent) >= 4.5f)
            assertTrue(contrast(colors.tooltipContent, colors.tooltipContainer) >= 4.5f)
            assertTrue(contrast(colors.tooltipActionContent, colors.tooltipActionContainer) >= 4.5f)
        }
    }
}
