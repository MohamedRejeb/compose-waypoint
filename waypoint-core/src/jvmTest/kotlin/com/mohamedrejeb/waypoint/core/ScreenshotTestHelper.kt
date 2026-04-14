package com.mohamedrejeb.waypoint.core

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.abs

/**
 * Helper for golden-file screenshot testing.
 *
 * - First run (no golden exists): saves the image as the golden and passes.
 * - Subsequent runs: compares against the golden, fails if pixels differ beyond threshold.
 * - To re-record: delete the golden file and re-run.
 */
object ScreenshotTestHelper {

    private val goldenDir: File by lazy {
        val path = System.getProperty("golden.dir")
            ?: error("System property 'golden.dir' not set. Add to build.gradle.kts.")
        File(path).also { it.mkdirs() }
    }

    /**
     * Assert that [actual] matches the golden image named [name].png.
     *
     * @param name golden file name (without extension)
     * @param actual the captured ImageBitmap
     * @param maxPixelDiffPercent max percentage of pixels that can differ (0.0 - 100.0)
     * @param perChannelTolerance per-channel tolerance for each pixel (0-255 scale)
     */
    fun assertMatchesGolden(
        name: String,
        actual: ImageBitmap,
        maxPixelDiffPercent: Double = 0.5,
        perChannelTolerance: Int = 5,
    ) {
        val goldenFile = File(goldenDir, "$name.png")
        val actualBuffered = actual.toBufferedImage()

        if (!goldenFile.exists()) {
            // Record mode — save and pass
            ImageIO.write(actualBuffered, "png", goldenFile)
            println("Golden recorded: ${goldenFile.absolutePath} (${actualBuffered.width}x${actualBuffered.height})")
            return
        }

        // Verify mode — compare
        val golden = ImageIO.read(goldenFile)
            ?: error("Failed to read golden: ${goldenFile.absolutePath}")

        if (golden.width != actualBuffered.width || golden.height != actualBuffered.height) {
            // Size mismatch — save actual for debugging and fail
            val diffFile = File(goldenDir, "${name}_actual.png")
            ImageIO.write(actualBuffered, "png", diffFile)
            error(
                "Size mismatch for '$name': " +
                    "golden=${golden.width}x${golden.height}, " +
                    "actual=${actualBuffered.width}x${actualBuffered.height}. " +
                    "Actual saved to: ${diffFile.absolutePath}",
            )
        }

        val totalPixels = golden.width * golden.height
        var diffCount = 0

        for (y in 0 until golden.height) {
            for (x in 0 until golden.width) {
                if (!pixelsMatch(golden.getRGB(x, y), actualBuffered.getRGB(x, y), perChannelTolerance)) {
                    diffCount++
                }
            }
        }

        val diffPercent = (diffCount.toDouble() / totalPixels) * 100.0

        if (diffPercent > maxPixelDiffPercent) {
            val diffFile = File(goldenDir, "${name}_actual.png")
            ImageIO.write(actualBuffered, "png", diffFile)
            error(
                "Screenshot '$name' differs from golden: " +
                    "%.2f%% pixels differ (threshold: %.2f%%). ".format(diffPercent, maxPixelDiffPercent) +
                    "Actual saved to: ${diffFile.absolutePath}. " +
                    "Delete the golden to re-record.",
            )
        }
    }

    private fun pixelsMatch(rgb1: Int, rgb2: Int, tolerance: Int): Boolean {
        val a1 = (rgb1 shr 24) and 0xFF
        val r1 = (rgb1 shr 16) and 0xFF
        val g1 = (rgb1 shr 8) and 0xFF
        val b1 = rgb1 and 0xFF

        val a2 = (rgb2 shr 24) and 0xFF
        val r2 = (rgb2 shr 16) and 0xFF
        val g2 = (rgb2 shr 8) and 0xFF
        val b2 = rgb2 and 0xFF

        return abs(a1 - a2) <= tolerance &&
            abs(r1 - r2) <= tolerance &&
            abs(g1 - g2) <= tolerance &&
            abs(b1 - b2) <= tolerance
    }

    private fun ImageBitmap.toBufferedImage(): BufferedImage {
        val pixelMap = toPixelMap()
        val buffered = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val color = pixelMap[x, y]
                val a = (color.alpha * 255).toInt().coerceIn(0, 255)
                val r = (color.red * 255).toInt().coerceIn(0, 255)
                val g = (color.green * 255).toInt().coerceIn(0, 255)
                val b = (color.blue * 255).toInt().coerceIn(0, 255)
                buffered.setRGB(x, y, (a shl 24) or (r shl 16) or (g shl 8) or b)
            }
        }

        return buffered
    }
}
