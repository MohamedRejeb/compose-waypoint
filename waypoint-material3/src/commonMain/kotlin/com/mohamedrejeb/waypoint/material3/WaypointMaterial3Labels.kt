package com.mohamedrejeb.waypoint.material3

import androidx.compose.runtime.Immutable

/**
 * The texts shown by [WaypointMaterial3Tooltip] and
 * [WaypointMaterial3HintTooltip]. Pass your own instance to localize or
 * reword them.
 *
 * ```kotlin
 * val labels = WaypointMaterial3Labels(
 *     skip = "Passer",
 *     next = "Suivant",
 *     back = "Retour",
 *     finish = "Terminer",
 *     progress = { current, total -> "$current sur $total" },
 * )
 * ```
 *
 * @param skip label of the button that cancels the tour
 * @param next label of the button that advances to the next step
 * @param back label of the button that returns to the previous step
 * @param finish label that replaces [next] on the last step
 * @param gotIt label of the hint tooltip's dismiss button
 * @param close content description of the hint tooltip's close icon
 * @param progress formats the progress text from the 1-based number of the
 *   current step and the total number of visible steps
 *
 * Two instances are equal when their texts are equal and they share the same
 * [progress] function instance. When you build one inline in a composable with
 * a custom [progress], `remember` it (or keep the function in a top-level val)
 * so the tooltip can skip recomposition.
 */
@Immutable
public class WaypointMaterial3Labels(
    public val skip: String = "Skip",
    public val next: String = "Next",
    public val back: String = "Back",
    public val finish: String = "Finish",
    public val gotIt: String = "Got it",
    public val close: String = "Close",
    public val progress: (current: Int, total: Int) -> String = { current, total -> "$current of $total" },
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WaypointMaterial3Labels) return false
        return skip == other.skip &&
            next == other.next &&
            back == other.back &&
            finish == other.finish &&
            gotIt == other.gotIt &&
            close == other.close &&
            progress == other.progress
    }

    override fun hashCode(): Int {
        var result = skip.hashCode()
        result = 31 * result + next.hashCode()
        result = 31 * result + back.hashCode()
        result = 31 * result + finish.hashCode()
        result = 31 * result + gotIt.hashCode()
        result = 31 * result + close.hashCode()
        result = 31 * result + progress.hashCode()
        return result
    }

    public companion object {
        /** English labels */
        public val Default: WaypointMaterial3Labels = WaypointMaterial3Labels()
    }
}
