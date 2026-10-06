# Persistence

Waypoint can remember which tours a user has already completed and skip them on subsequent sessions. You opt in by assigning a `tourId` and providing a `WaypointPersistence` implementation.

## The `WaypointPersistence` interface

```kotlin
public interface WaypointPersistence {
    public fun isCompleted(tourId: String): Boolean
    public fun markCompleted(tourId: String)
    public fun reset(tourId: String)
    public fun resetAll()
}
```

All four methods are **synchronous**. Waypoint calls `isCompleted` from a composition context (specifically during `start()` and via `WaypointState.hasCompleted`), so blocking on a disk or network lookup will jank the UI. For async storage, maintain an in-memory cache and refresh it off the main thread.

## Wiring it up

Pass `tourId` and `persistence` to `rememberWaypointState`. Without a `tourId`, persistence is a no-op, the storage calls happen but `hasCompleted` always returns `false`.

```kotlin
val tourState = rememberWaypointState(
    tourId = "onboarding_v2",
    persistence = AppPersistence,
) {
    step(Targets.Search) { title = "Search" }
    step(Targets.Add) { title = "Create" }
}
```

## Show-once behavior

Once wired, the flow is:

1. `state.start()` is called.
2. If `persistence.isCompleted(tourId)` returns `true`, `start()` returns immediately without showing the tour.
3. Otherwise the tour runs normally.
4. When the user reaches the last step and advances, Waypoint calls `persistence.markCompleted(tourId)` automatically.
5. Next session, `start()` skips the tour.

`state.hasCompleted` exposes the stored value so you can drive UI from it (for example, hide a "Start tour" button once complete).

```kotlin
if (!tourState.hasCompleted) {
    Button(onClick = { tourState.start() }) { Text("Take the tour") }
}
```

!!! note
    Cancelling a tour (Skip, Escape, `state.stop()`) does **not** mark it complete. Only reaching the last step does. If you want "show at most once regardless of outcome," call `state.markCompleted()` manually from your `onTourCancel` callback.

## Manual control

Two methods on `WaypointState` let you manage completion outside the automatic flow:

| Method | Effect |
|---|---|
| `state.markCompleted()` | Call `persistence.markCompleted(tourId)`. Useful for "don't ever show this again" buttons. |
| `state.resetCompletion()` | Call `persistence.reset(tourId)`. Useful for a "Restart tour" action. |

```kotlin
// Settings screen
TextButton(onClick = { tourState.resetCompletion() }) {
    Text("Replay onboarding on next launch")
}
```

Both are no-ops when `tourId` is null.

## Implementations per platform

### Android, SharedPreferences

```kotlin
class SharedPrefsPersistence(
    private val prefs: SharedPreferences,
) : WaypointPersistence {

    override fun isCompleted(tourId: String): Boolean =
        prefs.getBoolean("waypoint_tour_$tourId", false)

    override fun markCompleted(tourId: String) {
        prefs.edit().putBoolean("waypoint_tour_$tourId", true).apply()
    }

    override fun reset(tourId: String) {
        prefs.edit().remove("waypoint_tour_$tourId").apply()
    }

    override fun resetAll() {
        prefs.edit().apply {
            prefs.all.keys
                .filter { it.startsWith("waypoint_tour_") }
                .forEach { remove(it) }
        }.apply()
    }
}
```

### iOS, NSUserDefaults

```kotlin
// iosMain
class UserDefaultsPersistence : WaypointPersistence {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun isCompleted(tourId: String): Boolean =
        defaults.boolForKey("waypoint_tour_$tourId")

    override fun markCompleted(tourId: String) {
        defaults.setBool(true, forKey = "waypoint_tour_$tourId")
    }

    override fun reset(tourId: String) {
        defaults.removeObjectForKey("waypoint_tour_$tourId")
    }

    override fun resetAll() {
        val all = defaults.dictionaryRepresentation().keys.filterIsInstance<String>()
        all.filter { it.startsWith("waypoint_tour_") }
            .forEach { defaults.removeObjectForKey(it) }
    }
}
```

Wire platform implementations via `expect` / `actual`:

```kotlin
// commonMain
expect fun platformPersistence(): WaypointPersistence

// androidMain
actual fun platformPersistence(): WaypointPersistence =
    SharedPrefsPersistence(context.getSharedPreferences("waypoint", Context.MODE_PRIVATE))

// iosMain
actual fun platformPersistence(): WaypointPersistence = UserDefaultsPersistence()
```

### Desktop (JVM), `java.util.prefs`

```kotlin
// desktopMain
class DesktopPersistence : WaypointPersistence {
    private val prefs = java.util.prefs.Preferences.userRoot().node("waypoint")

    override fun isCompleted(tourId: String) = prefs.getBoolean(tourId, false)
    override fun markCompleted(tourId: String) { prefs.putBoolean(tourId, true); prefs.flush() }
    override fun reset(tourId: String) { prefs.remove(tourId); prefs.flush() }
    override fun resetAll() { prefs.clear(); prefs.flush() }
}
```

### Web, `localStorage`

```kotlin
// jsMain / wasmJsMain
class LocalStoragePersistence : WaypointPersistence {
    private val key = { tourId: String -> "waypoint_tour_$tourId" }

    override fun isCompleted(tourId: String): Boolean =
        window.localStorage.getItem(key(tourId)) == "1"

    override fun markCompleted(tourId: String) {
        window.localStorage.setItem(key(tourId), "1")
    }

    override fun reset(tourId: String) {
        window.localStorage.removeItem(key(tourId))
    }

    override fun resetAll() {
        val toRemove = (0 until window.localStorage.length)
            .mapNotNull { window.localStorage.key(it) }
            .filter { it.startsWith("waypoint_tour_") }
        toRemove.forEach { window.localStorage.removeItem(it) }
    }
}
```

### In-memory fallback

When you only need session-scoped show-once behavior (for example, during development or for a short-lived modal):

```kotlin
class InMemoryPersistence : WaypointPersistence {
    private val completed = mutableSetOf<String>()

    override fun isCompleted(tourId: String) = tourId in completed
    override fun markCompleted(tourId: String) { completed += tourId }
    override fun reset(tourId: String) { completed -= tourId }
    override fun resetAll() { completed.clear() }
}
```

## Async storage

If you store completion state in a database or over the network, cache it synchronously at app start, then update the backing store asynchronously:

```kotlin
class CachedPersistence(
    private val scope: CoroutineScope,
    private val backend: RemoteStore,
    initial: Set<String>,
) : WaypointPersistence {

    private val completed = initial.toMutableSet()

    override fun isCompleted(tourId: String) = tourId in completed

    override fun markCompleted(tourId: String) {
        completed += tourId
        scope.launch { backend.setCompleted(tourId, true) }
    }

    override fun reset(tourId: String) {
        completed -= tourId
        scope.launch { backend.setCompleted(tourId, false) }
    }

    override fun resetAll() {
        completed.clear()
        scope.launch { backend.clearAll() }
    }
}
```

Warm the cache once (for example, from `Application.onCreate` or your DI graph's init) before you construct the first `WaypointState` that depends on it.

## Cross-session tour sequences

If you have multiple tours that need to run in order across sessions (for example, onboarding, then a feature tour, then a checkout tour), use different `tourId`s and check `hasCompleted` to decide which to start next. For orchestrating a sequence within the same session, see [Tour Sequences](tour-sequences.md).

## See also

- [Analytics](analytics.md), fire events for completion and cancellation
- [WaypointState API](../api/waypoint-state.md), `tourId`, `persistence`, `hasCompleted`, `markCompleted`, `resetCompletion`
