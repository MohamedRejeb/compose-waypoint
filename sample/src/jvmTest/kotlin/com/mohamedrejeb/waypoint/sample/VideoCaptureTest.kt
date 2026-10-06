package com.mohamedrejeb.waypoint.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SkikoComposeUiTest
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runSkikoComposeUiTest
import androidx.compose.ui.unit.Density
import com.mohamedrejeb.waypoint.sample.lab.LabScreen
import com.mohamedrejeb.waypoint.sample.theme.SampleTheme
import java.io.File
import kotlin.test.Test
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Surface

private const val FramesDirEnv = "WAYPOINT_VIDEO_FRAMES"
private const val CaptureDensity = 2f
private const val WidthDp = 420
private const val HeightDp = 880
private const val FrameMillis = 16L
private const val TapLeadFrames = 12
private const val TapFadeFrames = 16
private const val TapRadiusDp = 20f
private const val TypingFrames = 4

/**
 * Renders the sample frame by frame on the test clock, for the intro video.
 * Does nothing unless WAYPOINT_VIDEO_FRAMES points at an output directory.
 */
@OptIn(ExperimentalTestApi::class)
class VideoCaptureTest {

    private class Tap(val frame: Int, val center: Offset)

    private class Recorder(private val test: SkikoComposeUiTest, private val dir: File) {
        private var frame = 0
        private val taps = mutableListOf<Tap>()

        fun hold(millis: Int) = frames((millis / FrameMillis).toInt())

        /** Lets time pass without recording it. */
        fun skip(millis: Int) = test.mainClock.advanceTimeBy(millis.toLong())

        fun frames(count: Int) = repeat(count) {
            test.mainClock.advanceTimeByFrame()
            save()
        }

        /** Shows a touch indicator on the node, then clicks it. */
        fun tap(node: SemanticsNodeInteraction) {
            val center = node.fetchSemanticsNode().boundsInWindow.center
            taps += Tap(frame + TapLeadFrames, center)
            frames(TapLeadFrames)
            node.performClick()
        }

        fun tap(text: String) = tap(test.onNodeWithText(text))

        fun type(node: SemanticsNodeInteraction, text: String) = text.forEach { char ->
            node.performTextInput(char.toString())
            frames(TypingFrames)
        }

        private fun save() {
            val bitmap = test.captureToImage().asSkiaBitmap()
            val surface = Surface.makeRasterN32Premul(bitmap.width, bitmap.height)
            surface.canvas.drawImage(Image.makeFromBitmap(bitmap), 0f, 0f)
            taps.forEach { drawTap(surface, it) }
            val png = surface.makeImageSnapshot().encodeToData(EncodedImageFormat.PNG) ?: error("encode failed")
            File(dir, "f%05d.png".format(frame)).writeBytes(png.bytes)
            frame++
        }

        private fun drawTap(surface: Surface, tap: Tap) {
            val t = frame - tap.frame
            if (t < -TapLeadFrames || t > TapFadeFrames) return
            val alpha = if (t < 0) (t + TapLeadFrames) / TapLeadFrames.toFloat() else 1f - t / TapFadeFrames.toFloat()
            val pressed = if (t in 0..TapFadeFrames) 0.82f else 1f
            val radius = TapRadiusDp * CaptureDensity * pressed
            val fill = Paint().apply { color = argb(alpha * 0.45f, 255, 255, 255) }
            val ring = Paint().apply {
                color = argb(alpha * 0.55f, 20, 20, 30)
                mode = PaintMode.STROKE
                strokeWidth = 1.5f * CaptureDensity
            }
            surface.canvas.drawCircle(tap.center.x, tap.center.y, radius, fill)
            surface.canvas.drawCircle(tap.center.x, tap.center.y, radius, ring)
        }

        private fun argb(alpha: Float, r: Int, g: Int, b: Int): Int =
            ((alpha.coerceIn(0f, 1f) * 255).toInt() shl 24) or (r shl 16) or (g shl 8) or b
    }

    private fun record(
        scene: String,
        content: @Composable () -> Unit,
        script: Recorder.(SkikoComposeUiTest) -> Unit,
    ) {
        val root = System.getenv(FramesDirEnv) ?: return
        val dir = File(root, scene).apply {
            deleteRecursively()
            mkdirs()
        }
        runSkikoComposeUiTest(
            size = Size(WidthDp * CaptureDensity, HeightDp * CaptureDensity),
            density = Density(CaptureDensity),
        ) {
            mainClock.autoAdvance = false
            setContent(content)
            Recorder(this, dir).script(this)
        }
    }

    /** Chapter 1: the spotlight moving between targets, then the theme switch. */
    @Test
    fun tour() = record("tour", { App(autoStartTour = true) }) { test ->
        hold(1000)
        repeat(4) {
            tap("Next")
            hold(1150)
        }
        tap(test.onNodeWithContentDescription("Switch to dark theme"))
        hold(1700)
    }

    /** Chapter 2: the user types inside the highlight and the tour follows. */
    @Test
    fun handsOn() = record("handsOn", { App(autoStartTour = false) }) { test ->
        skip(100)
        test.onNodeWithContentDescription("Play Plan a trip").performClick()
        skip(1200)
        hold(500)
        tap("Next")
        hold(700)
        val name = test.field("Trip name")
        tap(name)
        frames(8)
        type(name, "Summer in Japan")
        hold(1500)
        val destination = test.field("Destination")
        tap(destination)
        frames(8)
        type(destination, "Kyoto")
        hold(1500)
        tap("Balanced")
        hold(2900)
    }

    /** Chapter 3: canvas targets, auto-scroll and a step inside a sheet. */
    @Test
    fun anywhere() = record("anywhere", { App(autoStartTour = false) }) { test ->
        skip(100)
        test.onNodeWithContentDescription("Play Know your trip").performClick()
        skip(900)
        hold(1300)
        tap("Next")
        hold(1600)
        tap("Next")
        hold(1000)
        tap("Add stop")
        hold(2000)
    }

    /** The Lab: one tour restyled live. */
    @Test
    fun styles() = record("styles", { LabContent() }) {
        skip(100)
        hold(300)
        tap("Start tour")
        hold(900)
        listOf("Glow" to 1100, "Circle" to 1100, "Pulse" to 1400, "Border" to 1100, "Ripple" to 1600).forEach { (option, millis) ->
            tap(option)
            hold(millis)
        }
    }

    @Composable
    private fun LabContent() = SampleTheme {
        // App draws the screen background, the Lab on its own has none.
        Box(modifier = Modifier.fillMaxSize().background(SampleTheme.colors.background)) {
            LabScreen(onBack = {})
        }
    }
}
