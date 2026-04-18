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
| `currentStepIndex` | `Int` | 0-based index of the visible step. |
| `totalSteps` | `Int` | Total number of steps in the tour. |
| `isFirstStep` | `Boolean` | True if `currentStepIndex == 0`. |
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
                currentIndex = stepScope.currentStepIndex,
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

A single step can provide its own tooltip composable via `content { ... }` in the step builder. It receives only `StepScope` (placement is not passed because per-step content typically doesn't need auto-flip hints).

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

Waypoint ships an internal `TooltipArrow` composable that draws a triangle pointing at the target, but it's not wired into the core tooltip by default, each custom tooltip can decide whether to show one. `ResolvedPlacement` tells you which direction the arrow should point:

| `ResolvedPlacement` | Arrow points |
|---|---|
| `Top` (tooltip above target) | Down |
| `Bottom` (tooltip below target) | Up |
| `Start` (tooltip left of target in LTR) | Right |
| `End` (tooltip right of target in LTR) | Left |

If you build your own arrow, mirror the direction for RTL layouts.

## Accessibility

The tooltip container is automatically wrapped in `Modifier.semantics { liveRegion = LiveRegionMode.Polite }`, so screen readers announce content when the tooltip appears or changes. You don't need to add `liveRegion` yourself.

Beyond that, treat the tooltip like any other surface: label icon buttons with `contentDescription`, ensure sufficient contrast, and keep tap targets at least `48.dp`.

## See also

- [Material3 API](../api/material3.md), pre-styled tooltip you can use instead
- [Theming](theming.md), customize the Material3 tooltip appearance
- [Highlight Styles](highlight-styles.md), change what surrounds the target
- [WaypointState API](../api/waypoint-state.md), step configuration reference
