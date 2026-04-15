# Sample App Rework — Design Spec

> Full rework of the demo app into a polished, catalog-style sample showcasing all Waypoint library features with responsive Material3 adaptive layout, Navigation3, and ViewModel+MVI for realistic demos.

---

## 1. Module Changes

- **Rename** `composeApp` → `sample`
- Update `settings.gradle.kts`: `include(":sample")`
- Update `build.gradle.kts`: new dependencies, framework baseName → `"WaypointSample"`
- Package: `com.mohamedrejeb.waypoint.sample`
- Desktop main class: `com.mohamedrejeb.waypoint.sample.MainKt`
- Android namespace/applicationId: `com.mohamedrejeb.waypoint.sample`

---

## 2. New Dependencies

```toml
# gradle/libs.versions.toml additions
nav3-runtime = "1.0.0"
nav3-ui = "1.0.0-alpha06"
kotlinx-serialization = "1.8.1"

[libraries]
androidx-navigation3-runtime = { module = "androidx.navigation3:navigation3-runtime", version.ref = "nav3-runtime" }
androidx-navigation3-ui = { module = "org.jetbrains.androidx.navigation3:navigation3-ui", version.ref = "nav3-ui" }
kotlinx-serialization-core = { module = "org.jetbrains.kotlinx:kotlinx-serialization-core", version.ref = "kotlinx-serialization" }

[plugins]
kotlinx-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

**sample/build.gradle.kts commonMain dependencies:**
```kotlin
implementation(project(":waypoint-core"))
implementation(project(":waypoint-material3"))
implementation(libs.compose.runtime)
implementation(libs.compose.foundation)
implementation(libs.compose.material3)
implementation(libs.compose.ui)
implementation(libs.compose.components.resources)
implementation(libs.androidx.lifecycle.viewmodelCompose)
implementation(libs.androidx.lifecycle.runtimeCompose)
implementation(libs.androidx.navigation3.runtime)
implementation(libs.androidx.navigation3.ui)
implementation(libs.kotlinx.serialization.core)
// Material3 adaptive (via compose plugin shorthand)
implementation(compose.material3AdaptiveNavigationSuite)
```

---

## 3. Theming

### Font: Plus Jakarta Sans

Weights to include in `composeResources/font/`:
- `PlusJakartaSans-Regular.ttf` (400)
- `PlusJakartaSans-Medium.ttf` (500)
- `PlusJakartaSans-SemiBold.ttf` (600)
- `PlusJakartaSans-Bold.ttf` (700)

### Color Palette

Seed color: Rich violet `#7C3AED`

```kotlin
// Light scheme (generated from seed)
val primaryLight = Color(0xFF7C3AED)
val onPrimaryLight = Color(0xFFFFFFFF)
val primaryContainerLight = Color(0xFFEDE7FE)
val secondaryLight = Color(0xFF14B8A6)       // teal accent
val tertiaryLight = Color(0xFFF59E0B)        // amber accent
val surfaceLight = Color(0xFFFCF8FF)
val surfaceContainerLight = Color(0xFFF3EFF7)

// Dark scheme (generated from seed)
val primaryDark = Color(0xFFCFBCFF)
val onPrimaryDark = Color(0xFF3B0095)
val primaryContainerDark = Color(0xFF5B17D1)
val secondaryDark = Color(0xFF5EEAD4)
val tertiaryDark = Color(0xFFFCD34D)
val surfaceDark = Color(0xFF1C1B20)
val surfaceContainerDark = Color(0xFF211F26)
```

Both light and dark `colorScheme(...)` definitions using the full Material3 color roles.

### Typography

All text styles use Plus Jakarta Sans:
- `displayLarge` through `displaySmall`: Bold
- `headlineLarge` through `headlineSmall`: SemiBold
- `titleLarge` through `titleSmall`: SemiBold
- `bodyLarge` through `bodySmall`: Regular
- `labelLarge` through `labelSmall`: Medium

---

## 4. Navigation

### Navigation3 Setup

Routes as a sealed interface with `@Serializable` variants:

```kotlin
@Serializable
sealed interface Route {
    @Serializable data object Catalog : Route
    @Serializable data object Onboarding : Route
    @Serializable data object FeatureDiscovery : Route
    @Serializable data object InteractiveTutorial : Route
    @Serializable data object MultiTarget : Route
    @Serializable data object HighlightGallery : Route
    @Serializable data object ThemingPlayground : Route
    @Serializable data object AnalyticsDashboard : Route
    @Serializable data object ModalTours : Route
}
```

**App.kt** root structure:

```kotlin
@Composable
fun App() {
    SampleTheme {
        val backStack = rememberNavBackStack(Route.Catalog)
        NavDisplay(backStack = backStack) { route ->
            when (route) {
                is Route.Catalog -> CatalogScreen(onDemoClick = { backStack.add(it) })
                is Route.Onboarding -> OnboardingDemo(onBack = { backStack.removeLastOrNull() })
                // ... etc
            }
        }
    }
}
```

Predictive back is handled automatically by Navigation3's `NavDisplay`.

### Adaptive Layout

The `CatalogScreen` uses `NavigationSuiteScaffold` with responsive navigation:
- **Compact** (phones): Bottom navigation bar
- **Medium** (tablets/foldables): Navigation rail
- **Expanded** (desktop/large tablets): Permanent navigation drawer

On expanded screens, `CatalogScreen` uses a two-pane layout: demo list on the left, selected demo on the right.

---

## 5. Catalog Screen

A responsive grid of `DemoCard` composables.

**Layout:**
- Compact: Single column, vertical scroll
- Medium: 2-column grid
- Expanded: 3-column grid (or list+detail pane)

**DemoCard** design:
```
┌─────────────────────────────────┐
│  🎯  Icon (in colored circle)  │
│                                 │
│  Onboarding Tour               │  ← title (SemiBold)
│  First-launch app walkthrough   │  ← description (bodyMedium)
│                                 │
│  [Spotlight] [Tooltip] [Nav]    │  ← feature chips (labelSmall)
└─────────────────────────────────┘
```

Each card has:
- A distinctive icon (from Material Icons)
- Title and short description
- Feature chips showing which Waypoint features are demonstrated
- Tap navigates to the demo

---

## 6. Demo Screens — Detailed Design

Each demo wraps its content in a shared `DemoScaffold`:
```kotlin
@Composable
fun DemoScaffold(
    title: String,
    description: String,
    onBack: () -> Unit,
    onStartTour: () -> Unit,
    content: @Composable () -> Unit,
)
```
- Top app bar with back arrow and title
- Description text below the bar
- "Start Tour" FAB
- Content area for the mock UI

---

### 6.1 Onboarding Tour

**Mock UI:** Social/productivity app
- TopAppBar: avatar, "Home" title, search icon, notification bell
- Greeting card: "Welcome back, Alex!"
- 3 stat cards in a row (Tasks: 12, Completed: 8, Streak: 5)
- A list of 3-4 task items
- FAB for "New Task"
- Bottom nav: Home, Tasks, Messages, Profile

**Tour (6 steps):**

| # | Target | Placement | Features |
|---|--------|-----------|----------|
| 1 | Search icon | Bottom | Spotlight + Circle shape |
| 2 | Notification bell | Bottom | Spotlight + RoundedRect, custom padding |
| 3 | Greeting card | Bottom | Spotlight + Pill shape |
| 4 | Stats row | Top | Spotlight + RoundedRect |
| 5 | FAB | Start | Spotlight + Circle, ClickToAdvance |
| 6 | Bottom nav | Top | Spotlight + RoundedRect |

**Waypoint features demonstrated:**
- `rememberWaypointState` with DSL builder
- `Modifier.waypointTarget`
- `WaypointMaterial3Host`
- All `TooltipPlacement` values (Top, Bottom, Start, End, Auto)
- Multiple `SpotlightShape` variants
- `SpotlightPadding` customization
- Navigation (next, previous, skip)
- Keyboard navigation (desktop)

---

### 6.2 Feature Discovery

**Architecture:** ViewModel + MVI

**State:**
```kotlin
data class DiscoveryState(
    val hasSeenMessaging: Boolean = false,
    val hasSeenFilters: Boolean = false,
    val hasSeenDarkMode: Boolean = false,
)
```

**Mock UI:** Settings/feature screen
- 3 feature cards, each wrapped in `WaypointBeacon`:
  - "Smart Messaging" — Pulse beacon (violet)
  - "Advanced Filters" — Pulse beacon (teal)
  - "Dark Mode" — Dot beacon (amber)
- Each beacon disappears after tapping (via ViewModel state)
- "Reset All" button to bring beacons back

**Tour integration:**
- Tapping a beacon starts a 1-step tour for that feature
- Uses `WaypointPersistence` with in-memory fake (backed by ViewModel state)
- Tour auto-skips if already seen (`hasCompleted`)

**Waypoint features demonstrated:**
- `WaypointBeacon` — Pulse and Dot styles
- `BeaconStyle.Pulse` with custom color, radius
- `BeaconStyle.Dot` with custom color
- `WaypointPersistence` interface (in-memory implementation)
- `hasCompleted`, `markCompleted()`, `resetCompletion()`
- `tourId` for each feature
- Border + Pulse highlight styles

---

### 6.3 Interactive Tutorial

**Architecture:** ViewModel + MVI

**State:**
```kotlin
data class TutorialState(
    val name: String = "",
    val email: String = "",
    val plan: Plan? = null,
    val agreedToTerms: Boolean = false,
    val isSubmitted: Boolean = false,
)

enum class Plan { Free, Pro, Enterprise }

sealed interface TutorialEvent {
    data class NameChanged(val value: String) : TutorialEvent
    data class EmailChanged(val value: String) : TutorialEvent
    data class PlanSelected(val plan: Plan) : TutorialEvent
    data class TermsToggled(val agreed: Boolean) : TutorialEvent
    data object Submit : TutorialEvent
    data object Reset : TutorialEvent
}
```

**Mock UI:** Account setup / checkout form
- Name text field
- Email text field
- Plan selector (3 cards: Free, Pro, Enterprise)
- Terms checkbox
- Submit button

**Tour (5 steps):**

| # | Target | Interaction | AdvanceOn |
|---|--------|------------|-----------|
| 1 | Name field | AllowClick | Custom trigger: advance when name ≥ 2 chars |
| 2 | Email field | AllowClick | Custom trigger: advance when email contains "@" |
| 3 | Plan selector | AllowClick | Custom trigger: advance when plan selected |
| 4 | Terms checkbox | AllowClick | Conditional (`showIf { !agreedToTerms }`), ClickToAdvance |
| 5 | Submit button | ClickToAdvance | Default (button click) |

**Waypoint features demonstrated:**
- `WaypointTrigger.Custom` with `snapshotFlow` + `filter`
- `TargetInteraction.AllowClick` — user interacts while tour is active
- `TargetInteraction.ClickToAdvance`
- `showIf { }` — conditional step
- `onEnter` / `onExit` lifecycle callbacks
- ViewModel integration pattern

---

### 6.4 Multi-Target Spotlight

**Mock UI:** Analytics dashboard
- Header row: logo + "Dashboard" + date range picker + export button
- 4 KPI cards in a row (Revenue, Users, Orders, Growth)
- Chart placeholder area
- Action bar: Filter, Sort, Download buttons

**Tour (3 steps):**

| # | Primary Target | Additional Targets | Custom Tooltip |
|---|---------------|--------------------|---------------|
| 1 | Logo | Date picker, Export button | "These controls manage your dashboard view" — custom composable with icon row |
| 2 | Revenue card | Users card, Orders card, Growth card | "Your key metrics at a glance" — custom composable with mini sparklines |
| 3 | Filter button | Sort button, Download button | "Use these actions to refine your data" — custom composable with button descriptions |

**Waypoint features demonstrated:**
- `additionalTargets` — multi-element highlight
- Custom `@Composable` tooltip content via `step.content { scope -> ... }`
- `SpotlightShape.Circle`, `SpotlightShape.Pill`, `SpotlightShape.RoundedRect`
- `StepScope` access (currentStepIndex, totalSteps, isFirstStep, isLastStep)

---

### 6.5 Highlight Styles Gallery

**Mock UI:** Grid of 7 cards, one per highlight style.

Each card shows:
- Style name and description
- A target element (icon button or colored box)
- "Try It" button that starts a 1-step tour using that style

**Cards:**

| Style | Shape | Config |
|-------|-------|--------|
| Spotlight (Circle) | Circle | Default overlay |
| Spotlight (RoundedRect) | RoundedRect(12.dp) | Custom padding (8.dp) |
| Spotlight (Pill) | Pill | — |
| Pulse | Circle | Filled |
| Border | RoundedRect | 3.dp width, teal color |
| Ripple | Circle | Custom color |
| None | — | Tooltip only, no highlight |

Also includes a "Custom Highlight" card that demonstrates `HighlightStyle.Custom` with an animated composable (bouncing arrow or glowing ring).

**Waypoint features demonstrated:**
- ALL `HighlightStyle` variants: Spotlight, Pulse, Border, Ripple, None, Custom
- ALL `SpotlightShape` variants: Circle, Rect, RoundedRect, Pill
- `SpotlightPadding` — uniform vs per-side
- `HighlightStyle.Spotlight` — overlayColor, overlayAlpha
- `HighlightStyle.Custom` — user-provided composable
- `OverlayClickBehavior` — Dismiss, NextStep, Nothing, Custom

---

### 6.6 Theming Playground

**Architecture:** ViewModel + MVI

**State:**
```kotlin
data class ThemingState(
    val tooltipBackground: Color = Color(0xFF1B1B2F),
    val titleColor: Color = Color.White,
    val descriptionColor: Color = Color(0xFFB0B0B0),
    val primaryButtonColor: Color = Color(0xFF7C3AED),
    val skipButtonColor: Color = Color(0xFF888888),
    val tooltipShape: ThemingShape = ThemingShape.Rounded16,
    val tooltipElevation: Dp = 8.dp,
    val tooltipPadding: Dp = 20.dp,
)

enum class ThemingShape { Rounded8, Rounded16, Rounded24, Rect }
```

**Mock UI:** Split layout
- **Left/Top panel** — Theme controls:
  - Color buttons with color picker (simplified: row of preset color swatches)
  - Shape selector (segmented button: 8dp, 16dp, 24dp, Rect)
  - Elevation slider
  - Padding slider
- **Right/Bottom panel** — Live preview:
  - A mock card with a target element
  - Tooltip preview rendered with current theme settings
  - "Start Themed Tour" button

**Tour (3 steps)** using the customized theme via `WaypointMaterial3Theme`.

**Waypoint features demonstrated:**
- `WaypointMaterial3Theme` composable
- `WaypointMaterial3Colors` — all color properties
- `WaypointMaterial3Typography` — custom text styles
- `WaypointMaterial3Dimensions` — shape, padding, elevation, min/max width
- Live theme switching

---

### 6.7 Analytics Dashboard

**Architecture:** ViewModel + MVI

**State:**
```kotlin
data class AnalyticsState(
    val events: List<AnalyticsEvent> = emptyList(),
    val tourActive: Boolean = false,
)

data class AnalyticsEvent(
    val timestamp: Long,
    val type: String,        // "Tour Started", "Step Viewed", etc.
    val details: String,     // "tourId=demo, step=2, key=SearchBar"
)
```

**Mock UI:** Vertical split
- **Top** — Simple mock app with 4 targets (toolbar items + content card)
  - "Start Tour" / "Cancel Tour" toggle button
- **Bottom** — Analytics event log:
  - Real-time scrolling list of events as they fire
  - Each event shows: timestamp, type badge (color-coded), details
  - Summary stats at top: Total events, Steps viewed, Completion status

**WaypointAnalytics implementation:**
```kotlin
class DemoAnalytics(private val viewModel: AnalyticsViewModel) : WaypointAnalytics {
    override fun onTourStarted(tourId: String?, totalSteps: Int) {
        viewModel.onEvent(LogEvent("Tour Started", "tourId=$tourId, steps=$totalSteps"))
    }
    // ... all 5 callbacks
}
```

**Waypoint features demonstrated:**
- `WaypointAnalytics` interface — all 5 event methods
- `tourId` — identifying tours
- Real-time event tracking
- `analytics` parameter on `rememberWaypointState`

---

### 6.8 Modal Tours

**Mock UI:** Three sections with launch buttons

**Section 1: Dialog Tour**
- Button "Open Dialog with Tour"
- Launches `Dialog` containing a settings form with 2-step tour inside
- Demonstrates coordinate space handling inside dialogs

**Section 2: Bottom Sheet Tour**
- Button "Open Sheet with Tour"
- Launches `ModalBottomSheet` with content and 2-step tour
- Demonstrates sheet coordinate handling

**Section 3: Scrollable Tour**
- A tall scrollable content area with targets at various scroll positions
- Tour with 3 steps, some targets below the fold
- Demonstrates auto-scroll via `BringIntoViewRequester`
- Shows tooltip staying visible when target scrolls out of view

**Waypoint features demonstrated:**
- Dialog support (localBoundingBoxOf + positionInWindow)
- BottomSheet support
- Auto-scroll to off-screen targets
- Scroll-out-of-view highlight hiding
- Tooltip persistence during scroll

---

## 7. File Structure

```
sample/
├── build.gradle.kts
├── src/
│   ├── commonMain/
│   │   ├── kotlin/com/mohamedrejeb/waypoint/sample/
│   │   │   ├── App.kt                             // Root: theme + NavDisplay
│   │   │   ├── theme/
│   │   │   │   ├── Color.kt                       // Light/dark palettes
│   │   │   │   ├── Type.kt                        // Plus Jakarta Sans typography
│   │   │   │   └── Theme.kt                       // SampleTheme composable
│   │   │   ├── navigation/
│   │   │   │   └── Route.kt                       // Sealed interface routes
│   │   │   ├── catalog/
│   │   │   │   ├── CatalogScreen.kt               // Adaptive grid of demo cards
│   │   │   │   └── DemoCard.kt                    // Reusable card component
│   │   │   ├── components/
│   │   │   │   ├── DemoScaffold.kt                // Shared demo wrapper
│   │   │   │   └── ColorSwatch.kt                 // Color picker helper (theming)
│   │   │   └── demos/
│   │   │       ├── onboarding/
│   │   │       │   └── OnboardingDemo.kt
│   │   │       ├── discovery/
│   │   │       │   ├── FeatureDiscoveryDemo.kt
│   │   │       │   ├── DiscoveryViewModel.kt
│   │   │       │   └── DiscoveryContract.kt       // State + Event
│   │   │       ├── tutorial/
│   │   │       │   ├── InteractiveTutorialDemo.kt
│   │   │       │   ├── TutorialViewModel.kt
│   │   │       │   └── TutorialContract.kt
│   │   │       ├── multitarget/
│   │   │       │   └── MultiTargetDemo.kt
│   │   │       ├── highlights/
│   │   │       │   └── HighlightGalleryDemo.kt
│   │   │       ├── theming/
│   │   │       │   ├── ThemingPlaygroundDemo.kt
│   │   │       │   ├── ThemingViewModel.kt
│   │   │       │   └── ThemingContract.kt
│   │   │       ├── analytics/
│   │   │       │   ├── AnalyticsDashboardDemo.kt
│   │   │       │   ├── AnalyticsViewModel.kt
│   │   │       │   └── AnalyticsContract.kt
│   │   │       └── modals/
│   │   │           └── ModalToursDemo.kt
│   │   └── composeResources/
│   │       └── font/
│   │           ├── PlusJakartaSans-Regular.ttf
│   │           ├── PlusJakartaSans-Medium.ttf
│   │           ├── PlusJakartaSans-SemiBold.ttf
│   │           └── PlusJakartaSans-Bold.ttf
│   ├── androidMain/kotlin/.../MainActivity.kt
│   ├── iosMain/kotlin/.../MainViewController.kt
│   ├── jvmMain/kotlin/.../main.kt
│   ├── jsMain/kotlin/.../main.kt
│   └── wasmJsMain/kotlin/.../main.kt
```

---

## 8. Feature Coverage Matrix

Every public API in the library is demonstrated at least once:

| API | Demo(s) |
|-----|---------|
| `rememberWaypointState { }` | All demos |
| `Modifier.waypointTarget()` | All demos |
| `WaypointHost` | All demos |
| `WaypointMaterial3Host` | Onboarding, Analytics |
| `WaypointState.start/next/previous/stop` | All demos |
| `WaypointState.goTo(index/key)` | Multi-Target |
| `WaypointState.pause/resume` | Modal Tours |
| `TooltipPlacement.*` | Onboarding (all 5) |
| `SpotlightShape.*` | Onboarding + Gallery (all 4) |
| `SpotlightPadding` | Onboarding, Gallery |
| `HighlightStyle.Spotlight` | Onboarding, Gallery |
| `HighlightStyle.Pulse` | Discovery, Gallery |
| `HighlightStyle.Border` | Discovery, Gallery |
| `HighlightStyle.Ripple` | Gallery |
| `HighlightStyle.None` | Gallery |
| `HighlightStyle.Custom` | Gallery |
| `TargetInteraction.AllowClick` | Tutorial |
| `TargetInteraction.ClickToAdvance` | Tutorial, Onboarding |
| `OverlayClickBehavior.*` | Gallery (all 4) |
| `WaypointTrigger.Custom` | Tutorial |
| `showIf { }` | Tutorial |
| `onEnter / onExit` | Tutorial |
| `additionalTargets` | Multi-Target |
| Custom `content { scope -> }` | Multi-Target |
| `StepScope` | Multi-Target, all M3 tooltips |
| `WaypointBeacon` (Pulse + Dot) | Discovery |
| `WaypointPersistence` | Discovery |
| `hasCompleted / markCompleted / resetCompletion` | Discovery |
| `WaypointAnalytics` | Analytics |
| `tourId` | Analytics, Discovery |
| `WaypointMaterial3Theme` | Theming |
| `WaypointMaterial3Colors/Typography/Dimensions` | Theming |
| `KeyboardConfig` | Onboarding (desktop) |
| Dialog support | Modal Tours |
| BottomSheet support | Modal Tours |
| Auto-scroll (BringIntoViewRequester) | Modal Tours |
| Scroll-out-of-view hiding | Modal Tours |

---

## 9. Platform Entry Points

Minimal platform-specific code (entry points only):

- **Android**: `MainActivity` with `enableEdgeToEdge()` + `setContent { App() }`
- **iOS**: `MainViewController` returning `ComposeUIViewController { App() }`
- **Desktop/JVM**: `application { Window { App() } }`
- **Web (JS)**: `onWasmReady { BrowserViewportWindow { App() } }` or `renderComposable`
- **Web (WasmJs)**: `ComposeViewport(document.body!!) { App() }`

---

## 10. Non-Goals

- No network calls or real backend
- No local database — persistence uses in-memory fake
- No unit tests for the sample (library tests are sufficient)
- No custom icons — use Material Icons throughout
- No Compose Hot Reload changes (keep existing plugin)
- No localization / i18n
