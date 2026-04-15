package com.mohamedrejeb.waypoint.sample.demos.tutorial

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import com.mohamedrejeb.waypoint.core.TargetInteraction
import com.mohamedrejeb.waypoint.core.TooltipPlacement
import com.mohamedrejeb.waypoint.core.WaypointTrigger
import com.mohamedrejeb.waypoint.core.rememberWaypointState
import com.mohamedrejeb.waypoint.core.waypointTarget
import com.mohamedrejeb.waypoint.material3.WaypointMaterial3Host
import com.mohamedrejeb.waypoint.sample.components.DemoScaffold
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private enum class TutorialTarget { Name, Email, Plans, Terms, Submit }

@OptIn(FlowPreview::class)
@Composable
fun InteractiveTutorialDemo(onBack: () -> Unit) {
    val viewModel = viewModel { TutorialViewModel() }
    val formState by viewModel.state.collectAsState()

    val waypointState = rememberWaypointState {
        step(TutorialTarget.Name) {
            title = "Enter Your Name"
            description = "Type at least 2 characters to continue."
            placement = TooltipPlacement.Bottom
            interaction = TargetInteraction.AllowClick
            advanceOn = WaypointTrigger.Custom {
                viewModel.state
                    .map { it.name }
                    .debounce(800)
                    .filter { it.length >= 2 }
                    .first()
            }
            content { ActionTooltip("Enter Your Name", "Type at least 2 characters to continue.") }
        }
        step(TutorialTarget.Email) {
            title = "Enter Your Email"
            description = "Type an email address with '@' to continue."
            placement = TooltipPlacement.Bottom
            interaction = TargetInteraction.AllowClick
            advanceOn = WaypointTrigger.Custom {
                viewModel.state
                    .map { it.email }
                    .debounce(800)
                    .filter { "@" in it }
                    .first()
            }
            content { ActionTooltip("Enter Your Email", "Type an email address with '@' to continue.") }
        }
        step(TutorialTarget.Plans) {
            title = "Choose a Plan"
            description = "Select one of the plans below to continue."
            placement = TooltipPlacement.Top
            interaction = TargetInteraction.AllowClick
            advanceOn = WaypointTrigger.Custom {
                viewModel.state
                    .filter { it.plan != null }
                    .first()
            }
            content { ActionTooltip("Choose a Plan", "Select one of the plans below to continue.") }
        }
        step(TutorialTarget.Terms) {
            title = "Accept Terms"
            description = "Check the box to agree to the Terms of Service."
            placement = TooltipPlacement.Top
            interaction = TargetInteraction.ClickToAdvance
            showIf { !formState.agreedToTerms }
            content { ActionTooltip("Accept Terms", "Check the box to agree to the Terms of Service.") }
        }
        step(TutorialTarget.Submit) {
            title = "Submit"
            description = "You're all set! Click Create Account to finish."
            placement = TooltipPlacement.Top
            interaction = TargetInteraction.ClickToAdvance
            onEnter { viewModel.onEvent(TutorialEvent.TermsToggled(true)) }
            content { ActionTooltip("Submit", "You're all set! Click Create Account to finish.") }
        }
    }

    DemoScaffold(
        title = "Interactive Tutorial",
        description = "A form walkthrough with event-driven progression and conditional steps.",
        onBack = onBack,
        onStartTour = { waypointState.start() },
    ) { padding ->
        WaypointMaterial3Host(
            state = waypointState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )

                OutlinedTextField(
                    value = formState.name,
                    onValueChange = { viewModel.onEvent(TutorialEvent.NameChanged(it)) },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .waypointTarget(waypointState, TutorialTarget.Name),
                )

                OutlinedTextField(
                    value = formState.email,
                    onValueChange = { viewModel.onEvent(TutorialEvent.EmailChanged(it)) },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .waypointTarget(waypointState, TutorialTarget.Email),
                )

                Text(
                    text = "Choose a Plan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .waypointTarget(waypointState, TutorialTarget.Plans),
                ) {
                    Plan.entries.forEach { plan ->
                        PlanCard(
                            plan = plan,
                            selected = formState.plan == plan,
                            onClick = { viewModel.onEvent(TutorialEvent.PlanSelected(plan)) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .waypointTarget(waypointState, TutorialTarget.Terms),
                ) {
                    Checkbox(
                        checked = formState.agreedToTerms,
                        onCheckedChange = { viewModel.onEvent(TutorialEvent.TermsToggled(it)) },
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "I agree to the Terms of Service",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                Button(
                    onClick = { viewModel.onEvent(TutorialEvent.Submit) },
                    enabled = formState.name.isNotBlank()
                            && formState.email.contains("@")
                            && formState.plan != null
                            && formState.agreedToTerms,
                    modifier = Modifier
                        .fillMaxWidth()
                        .waypointTarget(waypointState, TutorialTarget.Submit),
                ) {
                    Text("Create Account")
                }

                AnimatedVisibility(
                    visible = formState.isSubmitted,
                    enter = fadeIn() + scaleIn(),
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(20.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Account Created!",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                                Text(
                                    text = "Welcome, ${formState.name}!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
private fun PlanCard(
    plan: Plan,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        border = if (selected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        },
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ),
        modifier = modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
        ) {
            Text(
                text = plan.label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = plan.price,
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

@Composable
private fun ActionTooltip(
    title: String,
    description: String,
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .widthIn(min = 200.dp, max = 280.dp)
            .shadow(elevation = 4.dp, shape = shape)
            .clip(shape)
            .background(MaterialTheme.colorScheme.inverseSurface)
            .padding(16.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.inverseOnSurface,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.8f),
        )
    }
}
