# Analytics

Waypoint emits five lifecycle events for every tour, so you can track how users engage with onboarding and feature tours. Implement `WaypointAnalytics` and hand it to `rememberWaypointState`, and Waypoint calls your implementation at the right moments.

## The `WaypointAnalytics` interface

```kotlin
public interface WaypointAnalytics {
    public fun onTourStarted(tourId: String?, totalSteps: Int) {}
    public fun onTourCompleted(tourId: String?, totalSteps: Int) {}
    public fun onTourCancelled(tourId: String?, stepIndex: Int, totalSteps: Int) {}
    public fun onStepViewed(tourId: String?, stepIndex: Int, targetKey: Any?) {}
    public fun onStepCompleted(tourId: String?, stepIndex: Int, targetKey: Any?) {}
}
```

Every method has a default no-op body, so you only need to override the ones you care about.

### Events

| Event | When it fires | Parameters |
|---|---|---|
| `onTourStarted` | `state.start()` accepts and the first step is entered. | `tourId`, `totalSteps` |
| `onTourCompleted` | User advances past the last step. | `tourId`, `totalSteps` |
| `onTourCancelled` | User skips, closes, or `state.stop()` is called mid-tour. | `tourId`, `stepIndex` (where they stopped), `totalSteps` |
| `onStepViewed` | A step becomes active (including the first step, and every transition). | `tourId`, `stepIndex`, `targetKey` |
| `onStepCompleted` | User advances off a step, or the tour completes on that step. | `tourId`, `stepIndex`, `targetKey` |

!!! note
    `onStepCompleted` also fires for the last step just before `onTourCompleted`. You'll see one completion event per step the user actually finished. If the user cancels mid-step, `onStepCompleted` is *not* called for that step, but `onTourCancelled` carries the `stepIndex` so you know where they stopped.

## Wiring it up

Pass `analytics` and `tourId` to `rememberWaypointState`. Without a `tourId`, events still fire but their `tourId` parameter is `null`, which makes them hard to attribute in a real analytics backend.

```kotlin
val tourState = rememberWaypointState(
    tourId = "onboarding_v2",
    analytics = ConsoleAnalytics,
) {
    step(Targets.Search) { title = "Search"; description = "Find anything." }
    step(Targets.Add) { title = "Create"; description = "Make something." }
    step(Targets.Profile) { title = "Profile"; description = "You." }
}
```

## Example: console analytics

A minimal implementation that prints every event, useful during development:

```kotlin
object ConsoleAnalytics : WaypointAnalytics {
    override fun onTourStarted(tourId: String?, totalSteps: Int) {
        println("[tour] started id=$tourId steps=$totalSteps")
    }

    override fun onTourCompleted(tourId: String?, totalSteps: Int) {
        println("[tour] completed id=$tourId steps=$totalSteps")
    }

    override fun onTourCancelled(tourId: String?, stepIndex: Int, totalSteps: Int) {
        println("[tour] cancelled id=$tourId at=$stepIndex/$totalSteps")
    }

    override fun onStepViewed(tourId: String?, stepIndex: Int, targetKey: Any?) {
        println("[tour] step viewed id=$tourId index=$stepIndex target=$targetKey")
    }

    override fun onStepCompleted(tourId: String?, stepIndex: Int, targetKey: Any?) {
        println("[tour] step completed id=$tourId index=$stepIndex target=$targetKey")
    }
}
```

## Example: Firebase

```kotlin
class FirebaseWaypointAnalytics(
    private val firebase: FirebaseAnalytics,
) : WaypointAnalytics {

    override fun onTourStarted(tourId: String?, totalSteps: Int) {
        firebase.logEvent("tour_started") {
            param("tour_id", tourId ?: "unknown")
            param("total_steps", totalSteps.toLong())
        }
    }

    override fun onTourCompleted(tourId: String?, totalSteps: Int) {
        firebase.logEvent("tour_completed") {
            param("tour_id", tourId ?: "unknown")
            param("total_steps", totalSteps.toLong())
        }
    }

    override fun onTourCancelled(tourId: String?, stepIndex: Int, totalSteps: Int) {
        firebase.logEvent("tour_cancelled") {
            param("tour_id", tourId ?: "unknown")
            param("step_index", stepIndex.toLong())
            param("total_steps", totalSteps.toLong())
            param("completion_pct", (stepIndex.toDouble() / totalSteps * 100).toLong())
        }
    }

    override fun onStepViewed(tourId: String?, stepIndex: Int, targetKey: Any?) {
        firebase.logEvent("tour_step_viewed") {
            param("tour_id", tourId ?: "unknown")
            param("step_index", stepIndex.toLong())
            param("step_key", targetKey?.toString() ?: "unknown")
        }
    }

    override fun onStepCompleted(tourId: String?, stepIndex: Int, targetKey: Any?) {
        firebase.logEvent("tour_step_completed") {
            param("tour_id", tourId ?: "unknown")
            param("step_index", stepIndex.toLong())
            param("step_key", targetKey?.toString() ?: "unknown")
        }
    }
}
```

## Example: Mixpanel / PostHog / custom

Any analytics backend with a "track event" function works. Map each callback to a single event with the relevant properties.

```kotlin
class MixpanelWaypointAnalytics(
    private val mixpanel: MixpanelAPI,
) : WaypointAnalytics {

    override fun onTourStarted(tourId: String?, totalSteps: Int) {
        mixpanel.track("Tour Started", buildJsonObject {
            put("tour_id", tourId)
            put("total_steps", totalSteps)
        })
    }
    // ... other methods
}
```

## Platform considerations

`WaypointAnalytics` is a plain Kotlin interface, so it's available on every Waypoint target (Android, iOS, Desktop, Web). The concrete backend you pick needs to support your target platforms, this is a per-backend concern, not a Waypoint concern.

A common pattern on KMP projects is to `expect`/`actual` the analytics implementation and have each platform wire up its own SDK:

```kotlin
// commonMain
expect fun platformAnalytics(): WaypointAnalytics

// androidMain
actual fun platformAnalytics(): WaypointAnalytics = FirebaseWaypointAnalytics(Firebase.analytics)

// iosMain
actual fun platformAnalytics(): WaypointAnalytics = AmplitudeWaypointAnalytics(...)
```

## Interaction with host callbacks

`WaypointHost` (and `WaypointMaterial3Host`) also accept `onTourComplete` and `onTourCancel` callbacks. These fire at the same moments as `onTourCompleted` / `onTourCancelled` on the analytics implementation.

| Use case | Prefer |
|---|---|
| Send an analytics event | `WaypointAnalytics` |
| Update UI state (hide a banner, show a toast) | Host callback |
| Both | Use both, they don't conflict |

```kotlin
WaypointMaterial3Host(
    state = tourState,
    onTourComplete = { showToast("Tour complete, welcome aboard.") },
    onTourCancel = { showBanner("Take the tour any time from Settings.") },
) {
    MyScreen()
}
```

## See also

- [Persistence](persistence.md), remember which tours a user has completed
- [WaypointState API](../api/waypoint-state.md), the `tourId` and `analytics` parameters
- [WaypointHost API](../api/waypoint-host.md), `onTourComplete` / `onTourCancel` callbacks
