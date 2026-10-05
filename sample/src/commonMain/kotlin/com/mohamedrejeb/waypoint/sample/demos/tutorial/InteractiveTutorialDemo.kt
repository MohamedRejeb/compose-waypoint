package com.mohamedrejeb.waypoint.sample.demos.tutorial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.waypoint.core.TargetInteraction
import com.mohamedrejeb.waypoint.core.rememberWaypointState
import com.mohamedrejeb.waypoint.core.waypointTarget
import com.mohamedrejeb.waypoint.material3.WaypointMaterial3Host
import com.mohamedrejeb.waypoint.sample.components.DemoScaffold
import com.mohamedrejeb.waypoint.sample.components.FlatCard
import com.mohamedrejeb.waypoint.sample.components.ResetOnLeave
import com.mohamedrejeb.waypoint.sample.components.ScreenPadding
import com.mohamedrejeb.waypoint.sample.components.SectionLabel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

private enum class TutorialTarget { Name, Email, Plan, Summary, Create }

private const val MinNameLength = 2

/**
 * A hands-on tutorial over a mock sign-up form. The user really types and taps
 * inside the highlighted element (PassThrough) while the rest of the screen is
 * blocked, and each step advances from what they do (advanceOn), not from the
 * Next button. Opens and closes with a centered card (steps without a target),
 * and includes an async gate (beforeShow) and a conditionally skipped step
 * (showIf).
 */
@Composable
fun InteractiveTutorialDemo(onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedPlan by remember { mutableStateOf<String?>(null) }
    var summaryLoading by remember { mutableStateOf(false) }
    var summaryVisible by remember { mutableStateOf(false) }
    var accountCreated by remember { mutableStateOf(false) }

    val state = rememberWaypointState {
        step {
            title = "Create an account, hands on"
            description = "You will fill in this form yourself. " +
                "Only the highlighted part of the screen responds at each step."
        }
        step(TutorialTarget.Name) {
            title = "Enter your name"
            description = "Type at least $MinNameLength characters to continue."
            interaction = TargetInteraction.PassThrough
            advanceOn { snapshotFlow { name }.first { it.trim().length >= MinNameLength } }
        }
        step(TutorialTarget.Email) {
            title = "Add your email"
            description = "Type an address like you@example.com to continue."
            interaction = TargetInteraction.PassThrough
            advanceOn { snapshotFlow { email }.first { it.looksLikeEmail() } }
        }
        step(TutorialTarget.Plan) {
            title = "Pick a plan"
            description = "Select any plan to continue."
            interaction = TargetInteraction.PassThrough
            advanceOn { snapshotFlow { selectedPlan }.first { it != null } }
        }
        step(TutorialTarget.Summary) {
            title = "Your summary"
            description = "This step waited for an async gate before showing. " +
                "On the Free plan, the next step is skipped."
            beforeShow {
                summaryLoading = true
                delay(1200)
                summaryLoading = false
                summaryVisible = true
            }
        }
        step(TutorialTarget.Create) {
            title = "Create your account"
            description = "Tap the highlighted button."
            interaction = TargetInteraction.PassThrough
            showIf { selectedPlan != "Free" }
            advanceOn { snapshotFlow { accountCreated }.first { it } }
        }
        step {
            title = "That's the whole form"
            description = "Every step advanced from something you did. " +
                "Start the tour again to try another plan."
        }
    }

    // While a step is pending (the summary gate is running) nothing is
    // highlighted and nothing is blocked, so the form is disabled meanwhile.
    val formEnabled = !state.isActive || state.isStepVisible

    val resetForm = {
        name = ""
        email = ""
        selectedPlan = null
        summaryLoading = false
        summaryVisible = false
        accountCreated = false
    }

    ResetOnLeave {
        state.stop()
        resetForm()
    }

    DemoScaffold(
        title = "Interactive tutorial",
        description = "You work inside the highlighted element while the rest is blocked, and steps advance from your actions.",
        onBack = onBack,
        onStartTour = {
            resetForm()
            state.start()
        },
        startTourVisible = !state.isActive,
    ) { padding ->
        WaypointMaterial3Host(state = state) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(horizontal = ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    enabled = formEnabled,
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .waypointTarget(state, TutorialTarget.Name),
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    enabled = formEnabled,
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .waypointTarget(state, TutorialTarget.Email),
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionLabel("Plan")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .waypointTarget(state, TutorialTarget.Plan),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        PlanTile(
                            name = "Free",
                            price = "$0",
                            selected = selectedPlan == "Free",
                            enabled = formEnabled,
                            onSelect = { selectedPlan = "Free" },
                            modifier = Modifier.weight(1f),
                        )
                        PlanTile(
                            name = "Pro",
                            price = "$12/mo",
                            selected = selectedPlan == "Pro",
                            enabled = formEnabled,
                            onSelect = { selectedPlan = "Pro" },
                            modifier = Modifier.weight(1f),
                        )
                        PlanTile(
                            name = "Team",
                            price = "$29/mo",
                            selected = selectedPlan == "Team",
                            enabled = formEnabled,
                            onSelect = { selectedPlan = "Team" },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                if (summaryLoading) {
                    FlatCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = "Preparing your summary",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 12.dp),
                            )
                        }
                    }
                }
                if (summaryVisible) {
                    SummaryCard(
                        name = name,
                        email = email,
                        plan = selectedPlan ?: "None",
                        modifier = Modifier.waypointTarget(state, TutorialTarget.Summary),
                    )
                }
                Button(
                    onClick = { accountCreated = true },
                    enabled = formEnabled && !accountCreated,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .waypointTarget(state, TutorialTarget.Create),
                ) {
                    Text(if (accountCreated) "Account created" else "Create account")
                }
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun PlanTile(
    name: String,
    price: String,
    selected: Boolean,
    enabled: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onSelect,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = price,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SummaryCard(
    name: String,
    email: String,
    plan: String,
    modifier: Modifier = Modifier,
) {
    FlatCard(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Text(
            text = "Summary",
            style = MaterialTheme.typography.titleSmall,
        )
        Spacer(Modifier.height(8.dp))
        SummaryRow(label = "Name", value = name)
        SummaryRow(label = "Email", value = email)
        SummaryRow(label = "Plan", value = plan)
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

/** Loose check, enough to know the user has finished typing an address. */
private fun String.looksLikeEmail(): Boolean {
    val domain = substringAfter('@', missingDelimiterValue = "")
    return substringBefore('@').isNotBlank() &&
        domain.substringBeforeLast('.', missingDelimiterValue = "").isNotEmpty() &&
        domain.substringAfterLast('.', missingDelimiterValue = "").length >= 2
}
