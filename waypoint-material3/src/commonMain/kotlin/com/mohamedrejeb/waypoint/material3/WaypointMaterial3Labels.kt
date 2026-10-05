package com.mohamedrejeb.waypoint.material3

import androidx.compose.runtime.Immutable

/**
 * The texts shown by [WaypointMaterial3Tooltip]. Pass your own instance to
 * localize or reword them.
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
 * @param progress formats the progress text from the 1-based number of the
 *   current step and the total number of visible steps
 */
@Immutable
public class WaypointMaterial3Labels(
    public val skip: String = "Skip",
    public val next: String = "Next",
    public val back: String = "Back",
    public val finish: String = "Finish",
    public val progress: (current: Int, total: Int) -> String = { current, total -> "$current of $total" },
) {
    public companion object {
        /** English labels */
        public val Default: WaypointMaterial3Labels = WaypointMaterial3Labels()
    }
}
