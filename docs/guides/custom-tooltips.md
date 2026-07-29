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

`WaypointHost` requires a `tooltipContent` slot. It's called on every frame the tooltip is visible.

```kotlin
WaypointHost(
    state = tourState,
    tooltipContent = { stepScope, placement ->
        MyTooltip(stepScope, placement)
    },
) {
    MyScreen()
}
```

The slot signature is `@Composable (StepScope, ResolvedPlacement) -> Unit`.

### `StepScope`

`StepScope` is the runtime view of the current step. It exposes progress and navigation callbacks.

| Property | Type | Description |
|---|---|---|
| `currentStepIndex` | `Int` | Raw 0-based index in the full steps list, including hidden steps. |
| `currentStepNumber` | `Int` | 1-based position among currently-visible steps, for "X of Y" progress. |
| `totalSteps` | `Int` | Number of currently-visible steps (steps whose `showIf` passes). |
| `isFirstStep` | `Boolean` | True if the step is the first visible step. |
| `isLastStep` | `Boolean` | True if the step is the last visible step. |
| `onNext` | `() -> Unit` | Advance to the next step, or complete the tour. |
| `onPrevious` | `() -> Unit` | Go back one step. |
| `onSkip` | `() -> Unit` | Cancel the tour (invokes `onTourCancel`). |
| `onClose` | `() -> Unit` | Alias for `onSkip`, use whichever reads better in your UI. |

`StepScope` does not expose the step's title or description directly. Read them from `state.currentStep` (or capture them via closure when building the state).

### `ResolvedPlacement`

`ResolvedPlacement` is the final placement after auto-flip and space calculation. Values: `Top`, `Bottom`, `Start`, `End`. Use it to point an arrow or adjust styling based on which side of the target the tooltip ended up on.

## Complete example

A full custom tooltip with title, description, progress dots, and Back/Next/Skip buttons:

```kotlin
enum class Targets { Search, Add, Profile }

@Composable
fun CustomOnboardingScreen() {
    val tourState = rememberWaypointState<Targets> {
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
        tooltipContent = { stepScope, placement ->
            val step = tourState.currentStep
            MyTooltip(
                title = step?.title.orEmpty(),
                description = step?.description.orEmpty(),
                currentIndex = stepScope.currentStepNumber - 1,
                total = stepScope.totalSteps,
                placement = placement,
                onBack = if (stepScope.isFirstStep) null else stepScope.onPrevious,
                onNext = stepScope.onNext,
                onSkip = stepScope.onSkip,
                isLast = stepScope.isLastStep,
            )
        },
    ) {
        MyScreen(tourState)
    }
}

@Composable
fun MyTooltip(
    title: String,
    description: String,
    currentIndex: Int,
    total: Int,
    placement: ResolvedPlacement,
    onBack: (() -> Unit)?,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    isLast: Boolean,
) {
    Column(
        modifier = Modifier
            .widthIn(max = 320.dp)
            .shadow(12.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1B1B2F))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Progress dots
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(total) { i ->
                val isCurrent = i == currentIndex
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

        Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium)
        Text(description, color = Color.White.copy(alpha = 0.8f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButton(onClick = onSkip) { Text("Skip", color = Color.White.copy(alpha = 0.7f)) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (onBack != null) {
                    TextButton(onClick = onBack) { Text("Back", color = Color.White) }
                }
                Button(onClick = onNext) {
                    Text(if (isLast) "Done" else "Next")
                }
            }
        }
    }
}
```

## Per-step overrides

A single step can provide its own tooltip composable via `content { ... }` in the step builder. It receives only `StepScope` (placement is not passed because per-step content typically doesn't need auto-flip hints). If per-step content does need the resolved placement, for example to draw an arrow, read `LocalTooltipArrowGeometry`.

```kotlin
rememberWaypointState<Targets> {
    step(Targets.Search) {
        title = "Search"
        description = "Standard tooltip."
    }

    step(Targets.Add) {
        // Fully custom content for this step only
        content { stepScope ->
            VideoTooltip(
                videoUrl = "https://...",
                onNext = stepScope.onNext,
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

`TooltipPlacement` values: `Top`, `Bottom`, `Start`, `End`, `Auto`. Even when you request a specific placement, Waypoint auto-flips to the opposite side if there isn't enough room, so `ResolvedPlacement` in your tooltip may differ from the `placement` you requested.

```kotlin
WaypointHost(
    state = tourState,
    tooltipSpacing = 16.dp,
    screenMargin = 24.dp,
    tooltipContent = { stepScope, placement -> MyTooltip(placement) },
) { MyScreen() }
```

## Arrows

Waypoint ships a public `TooltipArrow(placement, color, size, modifier)` composable that draws a triangle pointing at the target. The Material3 tooltip renders it automatically; for custom tooltips you decide whether to show one. The direction follows the resolved placement:

| `ResolvedPlacement` | Arrow points |
|---|---|
| `Top` (tooltip above target) | Down |
| `Bottom` (tooltip below target) | Up |
| `Start` (tooltip left of target in LTR) | Right |
| `End` (tooltip right of target in LTR) | Left |

`TooltipArrow` mirrors `Start`/`End` for RTL layouts internally, so you don't have to.

To position the arrow so it keeps pointing at the target even when the tooltip is clamped by a screen edge, read `LocalTooltipArrowGeometry`. It provides a `TooltipArrowGeometry(placement, arrowOffset)` while tooltip content is composed inside the popup (null outside of one). `arrowOffset` is the px distance of the arrow's center from the tooltip's left edge for `Top`/`Bottom` placements, and from the top edge for `Start`/`End`.

```kotlin
tooltipContent = { stepScope, placement ->
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

- [Material3 API](../api/material3.md), pre-styled tooltip you can use instead
- [Theming](theming.md), customize the Material3 tooltip appearance
- [Highlight Styles](highlight-styles.md), change what surrounds the target
- [WaypointState API](../api/waypoint-state.md), step configuration reference
