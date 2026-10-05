package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates

/**
 * Central state holder for a Waypoint tour.
 *
 * Create via [rememberWaypointState]. Manages step progression, target coordinate
 * tracking, auto-scrolling, and tour lifecycle.
 *
 * Compose exactly one primary [WaypointHost] per state at a time; it drives
 * the step lifecycle (gates, advanceOn, scrolling, end callbacks). Any number
 * of [WaypointOverlayHost]s may share the state. If the host that owns the
 * current step's target leaves the composition mid-step (a dialog closed by
 * the user), the tour stays active with nothing shown; call [stop] or [next]
 * from that code path.
 *
 * @param K the type of the target key (typically an enum)
 */
@Stable
public class WaypointState<K>(
    /** Immutable list of steps in this tour */
    public val steps: List<WaypointStep<K>>,
    /** Optional tour identifier for analytics */
    public val tourId: String? = null,
    /** Optional analytics tracker */
    public val analytics: WaypointAnalytics? = null,
    /** Optional persistence for remembering tour completion */
    public val persistence: WaypointPersistence? = null,
) {
    /** Index of the current step, or -1 if the tour is not active */
    public var currentStepIndex: Int by mutableStateOf(-1)
        private set

    /** Whether the tour is currently active (showing steps) */
    public var isActive: Boolean by mutableStateOf(false)
        private set

    /** Whether the tour is paused */
    public var isPaused: Boolean by mutableStateOf(false)
        private set

    /** Whether the current step's beforeShow gate has completed (true when it has none) */
    internal var isStepReady: Boolean by mutableStateOf(true)
        private set

    /**
     * True while a pending beforeShow gate keeps the current step's highlight
     * and tooltip hidden. Set up front when the step has nothing to show yet,
     * otherwise by the primary host once the gate turns out to suspend, so a
     * gate that returns immediately never hides an already-visible target.
     */
    internal var isStepHeld: Boolean by mutableStateOf(false)
        private set

    /**
     * Counts every entry into a step (navigation, start, restore), including
     * re-entering the same index. Step effects key on it so a `stop()` and
     * `start()` in one frame, or a `goTo` back to the current index, relaunch
     * them, and work from an earlier visit can tell it is stale.
     */
    internal var stepGeneration: Int by mutableStateOf(0)
        private set

    /** True when the current step was entered through [previous], the user's Back navigation. */
    internal var enteredBackward: Boolean by mutableStateOf(false)
        private set

    /** True when the current step is the one a tour run began on (start or restore), not reached by navigation. */
    internal var isFirstStepOfRun: Boolean by mutableStateOf(false)
        private set

    /**
     * True once the current step's highlight or tooltip has actually been
     * composed during this visit. Lets a pending cover tell "not laid out yet"
     * from "scrolled away after being shown".
     */
    internal var hasShownCurrentStep: Boolean by mutableStateOf(false)
        private set

    /**
     * Whether the current step's [WaypointStep.advanceOn] is armed for this
     * visit: the step has one and was not entered through [previous].
     */
    internal val isTriggerArmed: Boolean
        get() = currentStep?.advanceOn != null && !enteredBackward

    /**
     * Whether the current step is on screen: the tour is active and not paused,
     * the step's [WaypointStep.beforeShow] gate has completed, and the step has
     * something to show (its target's bounds are registered, or it has no
     * target). Backed by snapshot state, so it can be observed from composition
     * or `snapshotFlow`. Apps can use it to block their own UI while a step is
     * pending.
     */
    public val isStepVisible: Boolean
        get() {
            val step = currentStep ?: return false
            if (!isActive || isPaused || !isStepReady) return false
            return step.targetKey == null || boundsOf(step.targetKey) != null
        }

    /**
     * How the most recent tour run ended, or null if this state instance has
     * never ended a tour. Updated on every completion or cancellation,
     * including ones triggered by direct [stop] calls from app code.
     */
    public var lastEndReason: WaypointEndReason? by mutableStateOf(null)
        private set

    /**
     * Monotonic counter incremented every time a tour run ends. The primary
     * host observes this to fire onTourComplete/onTourCancel exactly once per
     * end event, no matter which host or code path triggered it.
     */
    internal var endEventCount: Int by mutableStateOf(0)
        private set

    /** The current step, or null if the tour is not active */
    public val currentStep: WaypointStep<K>?
        get() = if (currentStepIndex in steps.indices) steps[currentStepIndex] else null

    /**
     * Registered target coordinates, keyed by target key. Bounds are expressed in the
     * coordinate space of the host that owns the target (see [targetHostIds]).
     */
    internal val targetCoordinates = mutableStateMapOf<K, Rect>()

    /**
     * BringIntoViewRequesters for auto-scrolling targets into view. Snapshot
     * state, so the host can wait for a target that registers after its step
     * was entered.
     */
    internal val bringIntoViewRequesters = mutableStateMapOf<K, BringIntoViewRequester>()

    /**
     * LayoutCoordinates for each registered host, keyed by the host's unique id.
     * A single tour can span multiple hosts (main screen + Dialog + Sheet); each
     * host resolves its own targets against its own coordinates.
     */
    internal val hostCoordinatesMap = mutableStateMapOf<Any, LayoutCoordinates>()

    /** Which host each registered target belongs to. */
    internal val targetHostIds = mutableStateMapOf<K, Any>()

    /** The host id that owns the current step's target, or null if unregistered. */
    internal val currentTargetHostId: Any?
        get() = currentStep?.targetKey?.let { targetHostIds[it] }

    /** Hides the current step while its gate is still running. */
    internal fun holdStep() {
        isStepHeld = true
    }

    /** Marks the current step's gate as completed and reveals the step. */
    internal fun markStepReady() {
        isStepReady = true
        isStepHeld = false
    }

    /** Records that the current step's highlight or tooltip has been composed. */
    internal fun noteStepShown() {
        hasShownCurrentStep = true
    }

    private fun boundsOf(key: K?): Rect? = if (key == null) null else targetCoordinates[key]

    /**
     * Restores state after process death or configuration change.
     * Called by the [Saver] in [rememberWaypointState].
     *
     * If the saved step index no longer exists (the step list changed across
     * an app update or process death), the stale session is dropped and the
     * state stays inactive instead of restoring a tour with no current step.
     */
    internal fun restoreState(savedStepIndex: Int, savedIsActive: Boolean, savedIsPaused: Boolean) {
        if (savedIsActive && savedStepIndex !in steps.indices) return
        currentStepIndex = savedStepIndex
        isActive = savedIsActive
        isPaused = savedIsPaused
        enteredBackward = false
        isFirstStepOfRun = true
        enterStep()
    }

    /** Whether this tour has been completed (requires tourId and persistence) */
    public val hasCompleted: Boolean
        get() {
            val id = tourId ?: return false
            return persistence?.isCompleted(id) ?: false
        }

    /**
     * The bounds of the current step's target, in the local coordinates of the
     * host that owns the target (the [WaypointHost], or the [WaypointOverlayHost]
     * the target is inside of). Null when the tour is inactive, the target is
     * not laid out or is scrolled out of view, or the step has no target.
     */
    public val currentTargetBounds: Rect?
        get() = boundsOf(currentStep?.targetKey)

    // Navigation

    /** Start the tour from the first visible step */
    public fun start() {
        if (isActive) return
        if (hasCompleted) return
        val firstIndex = resolveNextVisibleStep(fromIndex = -1, direction = 1) ?: return
        isActive = true
        isPaused = false
        analytics?.onTourStarted(tourId, steps.size)
        transitionTo(firstIndex)
    }

    /** Advance to the next visible step, or complete the tour if on the last */
    public fun next() {
        if (!isActive || isPaused) return
        val nextIndex = resolveNextVisibleStep(fromIndex = currentStepIndex, direction = 1)
        if (nextIndex != null) {
            transitionTo(nextIndex)
        } else {
            complete()
        }
    }

    /** Go back to the previous visible step */
    public fun previous() {
        if (!isActive || isPaused) return
        val prevIndex = resolveNextVisibleStep(fromIndex = currentStepIndex, direction = -1)
        if (prevIndex != null) {
            transitionTo(prevIndex, viaBack = true)
        }
    }

    /** Jump to a specific step by index. Does nothing if that step is already the current one. */
    public fun goToStep(index: Int) {
        if (!isActive || isPaused) return
        if (index !in steps.indices || index == currentStepIndex) return
        if (steps[index].showIf?.invoke() == false) return
        transitionTo(index)
    }

    /** Jump to a specific step by target key */
    public fun goTo(key: K) {
        val index = steps.indexOfFirst { it.targetKey == key }
        if (index >= 0) goToStep(index)
    }

    /** Stop/cancel the tour */
    public fun stop() {
        if (!isActive && !isPaused) return
        val cancelledAtIndex = currentStepIndex
        val exitingStep = currentStep
        isActive = false
        isPaused = false
        currentStepIndex = -1
        markStepReady()
        exitingStep?.onExit?.invoke()
        analytics?.onTourCancelled(tourId, cancelledAtIndex, steps.size)
        lastEndReason = WaypointEndReason.Cancelled
        endEventCount++
    }

    /** Pause the tour (hide overlay, preserve state) */
    public fun pause() {
        if (!isActive || isPaused) return
        isPaused = true
    }

    /** Resume the tour from where it was paused */
    public fun resume() {
        if (!isPaused) return
        isPaused = false
    }

    // Auto-scroll

    /**
     * The [BringIntoViewRequester] of the current step's target, or null while
     * the target is not in the composition or the step has no target.
     */
    internal val currentBringIntoViewRequester: BringIntoViewRequester?
        get() = currentStep?.targetKey?.let { bringIntoViewRequesters[it] }

    // Host registration (called by WaypointHost / WaypointOverlayHost)

    internal fun registerHost(hostId: Any, coords: LayoutCoordinates) {
        hostCoordinatesMap[hostId] = coords
    }

    internal fun unregisterHost(hostId: Any) {
        hostCoordinatesMap.remove(hostId)
        // Clean up any targets still associated with this host in case the host
        // was disposed before its children's DisposableEffect cleanup ran.
        val orphaned = targetHostIds.entries
            .filter { it.value == hostId }
            .map { it.key }
        orphaned.forEach { key ->
            targetCoordinates.remove(key)
            targetHostIds.remove(key)
        }
    }

    // Target registration (called by Modifier.waypointTarget)

    internal fun registerTarget(key: K, hostId: Any, bounds: Rect) {
        targetCoordinates[key] = bounds
        targetHostIds[key] = hostId
    }

    internal fun registerBringIntoViewRequester(key: K, requester: BringIntoViewRequester) {
        bringIntoViewRequesters[key] = requester
    }

    /**
     * Clears the bounds for a target while keeping its host association intact.
     * Use when a target scrolls out of view but its composable is still in the
     * composition, the next onGloballyPositioned callback will re-register the
     * bounds against the same host.
     */
    internal fun clearTargetBoundsKeepingHost(key: K) {
        targetCoordinates.remove(key)
    }

    internal fun unregisterTarget(key: K) {
        targetCoordinates.remove(key)
        targetHostIds.remove(key)
        bringIntoViewRequesters.remove(key)
    }

    /**
     * Manually registers bounds for a target that has no wrappable composable,
     * for example a shape drawn inside a Canvas or a region of an editor
     * viewport. Bounds must be expressed in the coordinate space of the host
     * identified by [hostId], typically obtained via
     * `LocalWaypointHostId.current` inside the composable that owns the drawing.
     *
     * Call this from a [androidx.compose.runtime.SideEffect] or
     * [androidx.compose.runtime.LaunchedEffect] driven by whatever changes the
     * target's position (shape state, pan/zoom transforms, etc.). Pair every
     * [setTargetBounds] call with [clearTargetBounds] when the target is
     * removed so the tour doesn't point at stale coordinates.
     *
     * @param key the target key identifying this target in the step list
     * @param hostId the id of the host whose coordinate space [bounds] is in
     * @param bounds the bounds of the target in host-local coordinates
     */
    @ExperimentalWaypointApi
    public fun setTargetBounds(key: K, hostId: Any, bounds: Rect) {
        targetCoordinates[key] = bounds
        targetHostIds[key] = hostId
    }

    /**
     * Registers bounds for a target when you have them in some source composable's
     * local coordinate space rather than in the host's. Computes the host-local
     * equivalent using the source's [LayoutCoordinates] and calls [setTargetBounds].
     *
     * Useful for targets drawn inside a Canvas or other composable where you know
     * the target's position in the source's local space (for example, a shape's
     * rect in canvas coordinates) and want to register without attaching a
     * modifier. Capture the source composable's coordinates via
     * `Modifier.onGloballyPositioned { sourceCoords = it }` and read
     * `LocalWaypointHostId.current` to obtain the [hostId].
     *
     * Translates by converting the rect's top-left and bottom-right corners via
     * `hostCoords.localPositionOf(sourceCoords, ...)`, which handles pure
     * translation and uniform scaling. Rotation between source and host is not
     * preserved, the resulting rect is axis-aligned in host space.
     *
     * Returns true if the registration happened, false if the host or source
     * coordinates are detached, the host id is not registered, or the coordinates
     * are in unrelated hierarchies.
     *
     * @param key the target key
     * @param hostId the id of the host whose coordinate space you want to register against
     * @param sourceCoords the [LayoutCoordinates] of the composable whose local space [localBounds] is in
     * @param localBounds bounds in [sourceCoords]'s local coordinate space
     */
    @ExperimentalWaypointApi
    public fun setTargetBoundsFromLocal(
        key: K,
        hostId: Any,
        sourceCoords: LayoutCoordinates,
        localBounds: Rect,
    ): Boolean {
        val hostCoords = hostCoordinatesMap[hostId] ?: return false
        if (!hostCoords.isAttached || !sourceCoords.isAttached) return false
        val topLeft = try {
            hostCoords.localPositionOf(sourceCoords, Offset(localBounds.left, localBounds.top))
        } catch (_: IllegalArgumentException) {
            return false
        }
        val bottomRight = try {
            hostCoords.localPositionOf(sourceCoords, Offset(localBounds.right, localBounds.bottom))
        } catch (_: IllegalArgumentException) {
            return false
        }
        val hostRect = Rect(
            left = minOf(topLeft.x, bottomRight.x),
            top = minOf(topLeft.y, bottomRight.y),
            right = maxOf(topLeft.x, bottomRight.x),
            bottom = maxOf(topLeft.y, bottomRight.y),
        )
        setTargetBounds(key, hostId, hostRect)
        return true
    }

    /**
     * Clears bounds and host association for a manually-registered target
     * previously passed to [setTargetBounds]. Call when the target is removed
     * or is no longer meaningful.
     *
     * Unlike [unregisterTarget], this does not touch any
     * [androidx.compose.foundation.relocation.BringIntoViewRequester] for the
     * key, that requester is tied to the `Modifier.waypointTarget` lifecycle
     * only. Manual callers can safely re-register the same [key] via
     * [setTargetBounds] at any time.
     *
     * @param key the target key to clear
     */
    @ExperimentalWaypointApi
    public fun clearTargetBounds(key: K) {
        targetCoordinates.remove(key)
        targetHostIds.remove(key)
    }

    // Internal

    private fun complete() {
        val exitingStep = currentStep
        val exitingIndex = currentStepIndex
        exitingStep?.onExit?.invoke()
        analytics?.onStepCompleted(tourId, exitingIndex, exitingStep?.targetKey)
        isActive = false
        isPaused = false
        currentStepIndex = -1
        markStepReady()
        analytics?.onTourCompleted(tourId, steps.size)
        val id = tourId
        if (id != null) persistence?.markCompleted(id)
        lastEndReason = WaypointEndReason.Completed
        endEventCount++
    }

    /** Force-marks this tour as completed in persistence */
    public fun markCompleted() {
        val id = tourId ?: return
        persistence?.markCompleted(id)
    }

    /** Resets the completion state so the tour can be shown again */
    public fun resetCompletion() {
        val id = tourId ?: return
        persistence?.reset(id)
    }

    private fun transitionTo(newIndex: Int, viaBack: Boolean = false) {
        val exitingStep = currentStep
        val exitingIndex = currentStepIndex
        exitingStep?.onExit?.invoke()
        if (exitingIndex >= 0) {
            analytics?.onStepCompleted(tourId, exitingIndex, exitingStep?.targetKey)
        }
        currentStepIndex = newIndex
        enteredBackward = viaBack
        isFirstStepOfRun = exitingIndex < 0
        val enteringStep = currentStep
        enteringStep?.onEnter?.invoke()
        analytics?.onStepViewed(tourId, newIndex, enteringStep?.targetKey)
        enterStep()
    }

    /**
     * Bookkeeping for the step that just became current: a new generation for
     * the step effects, nothing shown yet, and the gate flags. A step with a gate
     * is not ready until the primary host has run it. It is hidden up front
     * only when it has nothing to show yet anyway (target not registered) or
     * has no target; for an already laid out target the host hides it only if
     * the gate actually suspends, so a gate that returns immediately causes no
     * hidden frame.
     */
    private fun enterStep() {
        stepGeneration++
        hasShownCurrentStep = false
        val hasGate = currentStep?.beforeShow != null
        isStepReady = !hasGate
        isStepHeld = hasGate && currentTargetBounds == null
    }

    /**
     * Starting from [fromIndex], search in [direction] (+1 or -1) for the next
     * step whose [WaypointStep.showIf] condition passes (or is null).
     */
    private fun resolveNextVisibleStep(fromIndex: Int, direction: Int): Int? {
        var i = fromIndex + direction
        while (i in steps.indices) {
            if (steps[i].showIf?.invoke() != false) return i
            i += direction
        }
        return null
    }

    // Visible-step queries (used to build StepScope)

    /** Number of steps whose showIf currently passes. */
    internal fun visibleStepCount(): Int = steps.count { it.showIf?.invoke() != false }

    /**
     * 1-based position of the step at [index] among currently-visible steps.
     * The step at [index] itself always counts as visible (it is being shown).
     */
    internal fun visibleStepNumber(index: Int): Int =
        steps.take(index).count { it.showIf?.invoke() != false } + 1

    /** True when a visible step exists after [index]. */
    internal fun hasVisibleStepAfter(index: Int): Boolean =
        resolveNextVisibleStep(index, direction = 1) != null

    /** True when a visible step exists before [index]. */
    internal fun hasVisibleStepBefore(index: Int): Boolean =
        resolveNextVisibleStep(index, direction = -1) != null
}
