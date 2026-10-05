# Accessibility

Waypoint tours run over your app's UI, so tours inherit your app's accessibility story. The library handles a few tour-specific concerns for you, but the rest lives in your code: content descriptions, reduced motion, and RTL layout testing.

## What Waypoint does automatically

### Screen reader announcements

Every tooltip renders inside a `Box` with `liveRegion = LiveRegionMode.Polite`. Screen readers (TalkBack, VoiceOver, NVDA) announce tooltip content when it appears, and announce the new tooltip when the step changes, without you having to wire anything up.

This applies to both `WaypointHost` and `WaypointOverlayHost`, so tooltips inside dialogs also get announced.

### Focus management

When keyboard navigation is enabled, `WaypointHost` requests focus on itself when the tour starts and on every step change, so keyboard shortcuts (arrow keys, Enter, Escape) work immediately.

The exception is a step with `TargetInteraction.PassThrough`, where the user works inside the target: the host leaves focus where it is and only handles the dismiss keys, so a text field keeps its cursor and receives every other key.

The host never moves focus into a target. If a step should start with the cursor in a field, request focus yourself in `onEnter`. See [Keyboard Navigation](keyboard.md#text-input-during-a-tour).

### RTL-aware positioning

`WaypointPositionProvider` handles RTL layouts:

- `TooltipPlacement.Start` / `TooltipPlacement.End` swap physical sides based on `LocalLayoutDirection`.
- When placement is `Auto`, the "space available" calculations use logical start/end, so the tooltip still prefers the side with more room in either direction.
- Arrow positions mirror correctly.

`SpotlightPadding.start` and `SpotlightPadding.end` also swap at render time, so a padding of `start = 16.dp, end = 4.dp` becomes a left-padding of 16 in LTR and 16 on the right in RTL.

## What you are responsible for

### Content descriptions

Target content and custom tooltip content both need `contentDescription` for screen reader users:

```kotlin
Icon(
    imageVector = Icons.Default.Search,
    contentDescription = "Search",
    modifier = Modifier.waypointTarget(state, Targets.SearchIcon),
)
```

If you write a custom tooltip with icon buttons:

```kotlin
IconButton(onClick = { stepScope.skip() }) {
    Icon(
        imageVector = Icons.Default.Close,
        contentDescription = "Close tour",
    )
}
```

The buttons of `WaypointMaterial3Tooltip` are text buttons, read by their label, and the close icon of `WaypointMaterial3HintTooltip` has a configurable `closeContentDescription`.

### Reduced motion

Built-in animated highlight styles (`Pulse`, `Ripple`, and the `Glow` / `SoftEdge` spotlight effects that render statically but draw attention) don't consult a reduced-motion flag. If your users include people who prefer reduced motion, check the platform preference and pick a static highlight instead.

On Android, read the system preference via `LocalAccessibilityManager` or the system content resolver, then branch:

```kotlin
@Composable
fun accessibleHighlight(): HighlightStyle {
    val isReduced = isReduceMotionEnabled() // platform-specific
    return if (isReduced) {
        HighlightStyle.Border(color = Color.Blue, borderWidth = 3.dp)
    } else {
        HighlightStyle.Pulse(color = Color.Blue)
    }
}
```

For custom highlights that use `rememberInfiniteTransition`, conditionally switch to a static rendering:

```kotlin
HighlightStyle.Custom { targetBounds, animatedBounds ->
    if (reduceMotion) {
        drawStaticRing(animatedBounds)
    } else {
        drawPulsingRing(animatedBounds, infiniteTransition.value)
    }
}
```

!!! note
    Compose Multiplatform does not have a cross-platform `isReduceMotionEnabled` API. On Android, use `AccessibilityManager.isReducedMotionEnabled()` via `LocalContext.current.getSystemService`. On iOS, read `UIAccessibility.isReduceMotionEnabled`. On Desktop and Web, this is typically not exposed, you may want to provide an app-level toggle.

### Testing RTL

Wrap your tour in a `CompositionLocalProvider` that flips layout direction, and verify:

```kotlin
@Test
fun tooltipFlipsInRtl() = runComposeUiTest {
    setContent {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            MyTourScreen()
        }
    }
    // Assert tooltip is positioned on the correct side.
}
```

At runtime, test-flip your whole app by setting `LocalLayoutDirection` at the root. This is also how Compose renders Arabic, Hebrew, and Farsi locales.

## Full example: accessible custom tooltip

```kotlin
@Composable
fun AccessibleTooltip(stepScope: StepScope) {
    Column(
        modifier = Modifier
            .widthIn(max = 320.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
            .semantics {
                // Explicit description helps screen readers read the full context.
                contentDescription = "Tour tooltip, step ${stepScope.currentStepNumber} of ${stepScope.totalSteps}"
            },
    ) {
        Text(
            text = "Step ${stepScope.currentStepNumber}",
            style = MaterialTheme.typography.labelSmall,
        )
        stepScope.title?.let {
            Text(text = it, style = MaterialTheme.typography.titleMedium)
        }
        stepScope.description?.let {
            Text(text = it, style = MaterialTheme.typography.bodyMedium)
        }

        Row(horizontalArrangement = Arrangement.End) {
            TextButton(
                onClick = { stepScope.skip() },
                modifier = Modifier.semantics { contentDescription = "Skip tour" },
            ) {
                Text("Skip")
            }
            Button(
                onClick = { stepScope.next() },
                modifier = Modifier.semantics {
                    contentDescription = if (stepScope.isLastStep) "Finish tour" else "Next step"
                },
            ) {
                Text(if (stepScope.isLastStep) "Finish" else "Next")
            }
        }
    }
}
```

Because Waypoint already wraps the tooltip in a polite live region, its content is announced every time a new step appears.

## RTL test example

```kotlin
@Composable
fun RtlTourPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val state = rememberWaypointState {
            step(Targets.Start) {
                title = "ابدأ هنا"
                description = "اضغط الزر للمتابعة"
                placement = TooltipPlacement.End
            }
        }
        WaypointMaterial3Host(state = state) {
            MyContent()
        }
    }
}
```

The tooltip placed at `End` will render on the left side of the target (since "end" in RTL is the physical left). `SpotlightPadding.end` will also be applied to the left edge.

## Checklist

Before shipping a tour:

- [ ] Every target has a `contentDescription` on its content.
- [ ] Every custom tooltip button has an accessible label.
- [ ] Arrow-key and Escape behavior is tested on Desktop or Web.
- [ ] The tour renders correctly under `LayoutDirection.Rtl`.
- [ ] Animated highlights have a static fallback for reduced-motion users.
- [ ] The tour can be skipped entirely, dismissal is not required for app use.

## See also

- [Custom Tooltips](custom-tooltips.md), how to build tooltip components that preserve accessibility semantics.
- [Keyboard Navigation](keyboard.md), the keyboard handling path for focus and shortcuts.
