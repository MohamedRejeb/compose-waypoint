# Beacons

`WaypointBeacon` is a small pulse or dot indicator that wraps content to draw attention to it. Beacons run independently from tours and from hint state; if all you need is a flashing dot next to a button, a beacon is the minimum tool.

## Core API

```kotlin
@Composable
public fun WaypointBeacon(
    visible: Boolean = true,
    style: BeaconStyle = BeaconStyle.Pulse(),
    alignment: Alignment = Alignment.TopEnd,
    offset: DpOffset = DpOffset.Zero,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
)
```

| Parameter | Default | Description |
|-----------|---------|-------------|
| `visible` | `true` | Drives `AnimatedVisibility`, fades in/out. |
| `style` | `Pulse()` | `BeaconStyle.Pulse` or `BeaconStyle.Dot`. |
| `alignment` | `TopEnd` | Where the beacon sits relative to `content`. |
| `offset` | `Zero` | Extra offset after alignment. |
| `onClick` | `null` | Optional tap handler on the beacon itself. |
| `modifier` | `Modifier` | Modifier applied to the outer `Box`. |
| `content` | required | The UI element being decorated. |

Content renders normally underneath; the beacon draws on top, aligned inside the same `Box`.

## Beacon styles

`BeaconStyle` is a sealed interface with two variants.

### Pulse

Animated expanding ring plus a solid center dot. The ring animates from the dot's radius up to `maxPulseRadius` over 1500ms on a linear loop, fading from alpha 0.6 to 0 as it expands.

```kotlin
BeaconStyle.Pulse(
    color = Color(0xFFFF4444),
    beaconRadius = 5.dp,
    maxPulseRadius = 14.dp,
)
```

| Parameter | Default | Description |
|-----------|---------|-------------|
| `color` | `Color(0xFFFF4444)` | Dot and ring color. |
| `beaconRadius` | `5.dp` | Radius of the solid center dot. |
| `maxPulseRadius` | `14.dp` | Maximum radius of the expanding ring. |

The rendered canvas size is `maxPulseRadius * 2`, so the pulse stays inside the allocated space and never clips.

### Dot

Static filled circle, no animation.

```kotlin
BeaconStyle.Dot(
    color = Color(0xFF3F51B5),
    radius = 6.dp,
)
```

| Parameter | Default | Description |
|-----------|---------|-------------|
| `color` | `Color(0xFFFF4444)` | Dot color. |
| `radius` | `5.dp` | Dot radius. |

Use `Dot` when animated pulses are distracting or when you want a cheaper, static indicator for long-lived beacons.

## Click handling

When `onClick` is set, the beacon is wrapped in a `Modifier.clickable` with indication disabled (no ripple). The content underneath keeps its own click handling since the beacon only captures taps on its own canvas area.

```kotlin
var showTip by remember { mutableStateOf(true) }

WaypointBeacon(
    visible = showTip,
    style = BeaconStyle.Pulse(color = Color.Red),
    onClick = {
        // Show a popup, navigate, or dismiss.
        showTip = false
    },
) {
    SettingsButton()
}
```

## Full example: new feature beacon

```kotlin
@Composable
fun SettingsButton(onClick: () -> Unit) {
    var hasSeenFeature by rememberSaveable { mutableStateOf(false) }

    WaypointBeacon(
        visible = !hasSeenFeature,
        style = BeaconStyle.Pulse(
            color = MaterialTheme.colorScheme.primary,
            beaconRadius = 6.dp,
            maxPulseRadius = 18.dp,
        ),
        alignment = Alignment.TopEnd,
        offset = DpOffset(x = (-4).dp, y = 4.dp),
    ) {
        IconButton(
            onClick = {
                hasSeenFeature = true
                onClick()
            },
        ) {
            Icon(Icons.Default.Settings, contentDescription = "Settings")
        }
    }
}
```

The beacon disappears the moment the user taps the button, whether they tapped the button or the beacon itself.

## Beacons vs hints

A `WaypointHint` is a beacon plus a tooltip popup plus persistence. Use a standalone `WaypointBeacon` when you don't need any of that:

| Need | Use |
|------|-----|
| Just a flashing dot, no tooltip | `WaypointBeacon` |
| Beacon with tap-to-show tooltip | `WaypointHint` |
| Dismissal should persist across sessions | `WaypointHint` with `persistence` (plus an optional `groupId` namespace) |
| Multiple hints, centrally managed | `rememberWaypointHintState` |

Beacons are also useful inside tooltip content or other components where you only need the visual without any state machine.

## See also

- [Hints](hints.md), the higher-level component that combines a beacon with a persistent tooltip.
