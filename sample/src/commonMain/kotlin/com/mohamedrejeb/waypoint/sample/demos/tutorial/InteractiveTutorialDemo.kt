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
import com.mohamedrejeb.waypoint.core.WaypointTrigger
import com.mohamedrejeb.waypoint.core.rememberWaypointState
import com.mohamedrejeb.waypoint.core.waypointTarget
import com.mohamedrejeb.waypoint.material3.WaypointMaterial3Host
import com.mohamedrejeb.waypoint.sample.components.DemoScaffold
import com.mohamedrejeb.waypoint.sample.components.FlatCard
import com.mohamedrejeb.waypoint.sample.components.ResetOnLeave
import com.mohamedrejeb.waypoint.sample.components.ScreenPadding
import com.mohamedrejeb.waypoint.sample.components.SectionLabel
import kotlinx.coroutines.delay
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

private enum class TutorialTarget { Name, Email, Plan, Summary, Create }

/**
 * Event-driven progression: each step advances from what the user does in a
 * mock sign-up form, not from the Next button. Includes an async gate
 * (beforeShow) and a conditionally skipped step (showIf).
 */
@OptIn(FlowPreview::class)
@Composable
fun InteractiveTutorialDemo(onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedPlan by remember { mutableStateOf<String?>(null) }
    var summaryLoading by remember { mutableStateOf(false) }
    var summaryVisible by remember { mutableStateOf(false) }

    val state = rememberWaypointState {
        step(TutorialTarget.Name) {
            title = "Enter your name"
            description = "Type at least 2 characters to continue."
            interaction = TargetInteraction.AllowClick
            advanceOn = WaypointTrigger.Custom {
                // Debounced so the tour advances after a typing pause instead
                // of yanking focus mid-word at the second character.
                snapshotFlow { name }.filter { it.length >= 2 }.debounce(600).first()
            }
        }
        step(TutorialTarget.Email) {
            title = "Add your email"
            description = "Type an address containing @ to continue."
            interaction = TargetInteraction.AllowClick
            advanceOn = WaypointTrigger.Custom {
                snapshotFlow { email }.filter { "@" in it }.debounce(600).first()
            }
        }
        step(TutorialTarget.Plan) {
            title = "Pick a plan"
            description = "Select any plan to continue."
            interaction = TargetInteraction.AllowClick
            advanceOn = WaypointTrigger.Custom {
                snapshotFlow { selectedPlan }.filter { it != null }.first()
            }
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
            description = "Tap the highlighted button to finish."
            interaction = TargetInteraction.ClickToAdvance
            showIf { selectedPlan != null && selectedPlan != "Free" }
        }
    }

    ResetOnLeave {
        state.stop()
        name = ""
        email = ""
        selectedPlan = null
        summaryLoading = false
        summaryVisible = false
    }

    DemoScaffold(
        title = "Interactive tutorial",
        description = "Steps advance from your actions in the form: typing, selecting a plan, and an async gate.",
        onBack = onBack,
        onStartTour = {
            name = ""
            email = ""
            selectedPlan = null
            summaryLoading = false
            summaryVisible = false
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
                            onSelect = { selectedPlan = "Free" },
                            modifier = Modifier.weight(1f),
                        )
                        PlanTile(
                            name = "Pro",
                            price = "$12/mo",
                            selected = selectedPlan == "Pro",
                            onSelect = { selectedPlan = "Pro" },
                            modifier = Modifier.weight(1f),
                        )
                        PlanTile(
                            name = "Team",
                            price = "$29/mo",
                            selected = selectedPlan == "Team",
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
                    onClick = {},
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .waypointTarget(state, TutorialTarget.Create),
                ) {
                    Text("Create account")
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
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onSelect,
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
