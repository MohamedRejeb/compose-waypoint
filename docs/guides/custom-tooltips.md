# Custom Tooltips

A **custom tooltip** is any composable you render as the tour's callout UI. Waypoint's core module doesn't ship any visual opinions, so when you use `WaypointHost` directly you own the tooltip entirely.

## When to use a custom tooltip

| Situation | Recommendation |
|---|---|
| Standard onboarding with buttons, progress, title, description | Use `WaypointMaterial3Host` from `waypoint-material3`. See the [Material3 API](../api/material3.md). |
| Your app doesn't use Material3 | Use `WaypointHost` with your own `tooltipContent`. |
| You need a custom shape, animation, or content per step | Use a custom tooltip, either host-wide or per-step. |
| You want Material3 defaults with one or two custom steps | Use `WaypointMaterial3Host` and override `content` on specific steps. |

## Host-level tooltip

`WaypointHost` requires a `tooltipContent` slot. It renders the tooltip of every step that has no content of its own.

```kotlin
WaypointHost(
    state = tourState,
    tooltipContent = { stepScope ->
        MyTooltip(stepScope)
    },
) {
    MyScreen()
}
```

The slot signature is `@Composable (StepScope) -> Unit`.

### `StepScope`

`StepScope` carries everything a tooltip needs: the step's texts, where the tooltip sits, progress, and navigation.

| Member | Type | Description |
|---|---|---|
| `title` | `String?` | Title configured on the step. |
| `description` | `String?` | Description configured on the step. |
| `placement` | `ResolvedPlacement?` | Side of the target the tooltip ended up on, `null` for a step without a target. |
| `currentStepIndex` | `Int` | Raw 0-based index in the full steps list, including hidden steps. |
| `currentStepNumber` | `Int` | 1-based position among currently-visible steps, for "X of Y" progress. |
| `totalSteps` | `Int` | Number of currently-visible steps (steps whose `showIf` passes). |
| `isFirstStep` | `Boolean` | True if the step is the first visible step. |
| `isLastStep` | `Boolean` | True if the step is the last visible step. |
| `advancesAutomatically` | `Boolean` | True when the step's `advanceOn` is armed for this visit, so it will move on by itself. Hide your Next button in that case, see [Event-Driven Progression](advance-on.md#how-it-interacts-with-the-next-button). |
| `next()` | | Advance to the next step, or complete the tour on the last one. |
| `previous()` | | Go back one step. |
| `skip()` | | Cancel the tour (the host's `onTourCancel` fires). |

### `ResolvedPlacement`

`ResolvedPlacement` is the final placement after auto-flip and space calculation. Values: `Top`, `Bottom`, `Start`, `End`. Use it to adjust styling based on which side of the target the tooltip ended up on. It is `null` for a [step without a target](interactive-tutorials.md#intro-and-outro-cards), whose tooltip is centered over the host.

## Complete example

A full custom tooltip with an arrow, title, description, progress dots, and Back/Next/Skip buttons:

```kotlin
enum class Targets { Search, Add, Profile }

@Composable
fun CustomOnboardingScreen() {
    val tourState = rememberWaypointState {
        step(Targets.Search) {
            title = "Search"
            description = "Find anything in your workspace."
        }
        step(Targets.Add) {
            title = "Create"
            description = "Add new items from anywhere."
        }
        step(Targets.Profile) {
            title = "Profile"
            description = "Manage your account and preferences."
        }
    }

    LaunchedEffect(Unit) { tourState.start() }

    WaypointHost(
        state = tourState,
        tooltipContent = { stepScope -> MyTooltip(stepScope) },
    ) {
        MyScreen(tourState)
    }
}

private val TooltipColor = Color(0xFF1B1B2F)

@Composable
fun MyTooltip(stepScope: StepScope) {
    TooltipArrowBox(arrowColor = TooltipColor) {
        Column(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(TooltipColor)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Progress dots
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(stepScope.totalSteps) { i ->
                    val isCurrent = i == stepScope.currentStepNumber - 1
                    Box(
                        modifier = Modifier
                            .size(if (isCurrent) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isCurrent) Color.White else Color.White.copy(alpha = 0.3f),
                            ),
                    )
                }
            }

            stepScope.title?.let {
                Text(it, color = Color.White, style = MaterialTheme.typography.titleMedium)
            }
            stepScope.description?.let {
                Text(it, color = Color.White.copy(alpha = 0.8f))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(onClick = { stepScope.skip() }) {
                    Text("Skip", color = Color.White.copy(alpha = 0.7f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!stepScope.isFirstStep) {
                        TextButton(onClick = { stepScope.previous() }) {
                            Text("Back", color = Color.White)
                        }
                    }
                    Button(onClick = { stepScope.next() }) {
                        Text(if (stepScope.isLastStep) "Done" else "Next")
                    }
                }
            }
        }
    }
}
```

## Per-step overrides

A single step can provide its own tooltip composable via `content { ... }` in the step builder. It receives the same `StepScope` as the host-level slot.

```kotlin
rememberWaypointState {
    step(Targets.Search) {
        title = "Search"
        description = "Standard tooltip."
    }

    step(Targets.Add) {
        // Fully custom content for this step only
        content { stepScope ->
            VideoTooltip(
                videoUrl = "https://...",
                onNext = { stepScope.next() },
            )
        }
    }
}
```

When `content` is set, the host-level `tooltipContent` is bypassed for that step.

## Positioning mechanics

Tooltips render in a Compose `Popup` positioned by `WaypointPositionProvider`. Three parameters control positioning:

| Parameter | On | Default | Description |
|---|---|---|---|
| `placement` | step | `TooltipPlacement.Auto` | Desired side. `Auto` picks the side with most space. |
| `tooltipSpacing` | host | `12.dp` | Gap between target and tooltip. |
| `screenMargin` | host | `16.dp` | Minimum distance from screen edges. |

`TooltipPlacement` values: `Top`, `Bottom`, `Start`, `End`, `Auto`. Even when you request a specific placement, Waypoint auto-flips to the opposite side if there isn't enough room, so `StepScope.placement` may differ from the `placement` you requested.

```kotlin
WaypointHost(
    state = tourState,
    tooltipSpacing = 16.dp,
    screenMargin = 24.dp,
    tooltipContent = { stepScope -> MyTooltip(stepScope) },
) { MyScreen() }
```

A step without a target ignores all three: its tooltip is centered over the host.

## Arrows

The Material3 tooltip draws an arrow automatically. For a custom tooltip, wrap its body in `TooltipArrowBox`:

```kotlin
tooltipContent = { stepScope ->
    TooltipArrowBox(arrowColor = Color(0xFF1B1B2F)) {
        MyTooltipBody(stepScope)
    }
}
```

```kotlin
@Composable
public fun TooltipArrowBox(
    arrowColor: Color,
    modifier: Modifier = Modifier,
    arrowSize: Dp = 10.dp,
    content: @Composable () -> Unit,
)
```

| Parameter | Default | Description |
|---|---|---|
| `arrowColor` | required | Arrow fill color, typically the tooltip background color. |
| `modifier` | `Modifier` | Applied to the layout holding the arrow and the content. |
| `arrowSize` | `10.dp` | How far the arrow protrudes from the tooltip edge. Its base along the edge is twice this. |

The arrow sits on the edge that faces the target and keeps pointing at it when the tooltip is pushed sideways by a screen edge. For a step without a target, or outside a Waypoint tooltip, `TooltipArrowBox` renders the bare content. It works the same way inside hint tooltips.

!!! tip
    Put the shadow, clip and background on the content inside the box, not on the box itself, otherwise the arrow gets clipped or sits inside the background.

### Drawing the arrow yourself

`TooltipArrowBox` is built from two public pieces you can use directly: the `TooltipArrow(placement, color, modifier, size)` composable, which draws a triangle, and `LocalTooltipArrowGeometry`, which tells you where it goes.

The arrow direction follows the resolved placement:

| `ResolvedPlacement` | Arrow points |
|---|---|
| `Top` (tooltip above target) | Down |
| `Bottom` (tooltip below target) | Up |
| `Start` (tooltip left of target in LTR) | Right |
| `End` (tooltip right of target in LTR) | Left |

`TooltipArrow` mirrors `Start`/`End` for RTL layouts internally, so you don't have to.

`LocalTooltipArrowGeometry` provides a `TooltipArrowGeometry(placement, arrowOffset)` while tooltip content is composed for a step or hint with a target, and `null` otherwise. `arrowOffset` is the px distance of the arrow's center from the tooltip's left edge for `Top`/`Bottom` placements, and from the top edge for `Start`/`End`.

```kotlin
tooltipContent = { stepScope ->
    val geometry = LocalTooltipArrowGeometry.current
    Column {
        if (geometry != null && geometry.placement == ResolvedPlacement.Bottom) {
            TooltipArrow(
                placement = geometry.placement,
                color = Color(0xFF1B1B2F),
                modifier = Modifier
                    .size(width = 20.dp, height = 10.dp)
                    .offset { IntOffset((geometry.arrowOffset - 10.dp.toPx()).roundToInt(), 0) },
            )
        }
        MyTooltipBody(stepScope)
    }
}
```

## Accessibility

The tooltip container is automatically wrapped in `Modifier.semantics { liveRegion = LiveRegionMode.Polite }`, so screen readers announce content when the tooltip appears or changes. You don't need to add `liveRegion` yourself.

Beyond that, treat the tooltip like any other surface: label icon buttons with `contentDescription`, ensure sufficient contrast, and keep tap targets at least `48.dp`.

## See also

- [Interactive Tutorials](interactive-tutorials.md), a tooltip without a Next button for hands-on steps
- [Material3 API](../api/material3.md), pre-styled tooltip you can use instead
- [Theming](theming.md), customize the Material3 tooltip appearance
- [Highlight Styles](highlight-styles.md), change what surrounds the target
- [WaypointState API](../api/waypoint-state.md), step configuration reference
