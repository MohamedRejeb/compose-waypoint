package com.mohamedrejeb.waypoint.sample.demos.multitarget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.waypoint.core.HighlightStyle
import com.mohamedrejeb.waypoint.core.SpotlightShape
import com.mohamedrejeb.waypoint.core.WaypointHost
import com.mohamedrejeb.waypoint.core.rememberWaypointState
import com.mohamedrejeb.waypoint.core.waypointTarget
import com.mohamedrejeb.waypoint.sample.components.DemoScaffold

private enum class DashTarget {
    Logo, DateRange, Export,
    Revenue, Users, Orders, Growth,
    Filter, Sort, Download,
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MultiTargetDemo(onBack: () -> Unit) {
    val state = rememberWaypointState {
        step(DashTarget.Logo) {
            additionalTargets = listOf(DashTarget.DateRange, DashTarget.Export)
            highlightStyle = HighlightStyle.Spotlight(shape = SpotlightShape.Pill)
            content { scope ->
                TooltipCard(
                    title = "Dashboard Controls",
                    stepLabel = "1 of 3",
                    scope = scope,
                ) {
                    ControlDescription(
                        icon = Icons.AutoMirrored.Rounded.TrendingUp,
                        label = "Logo",
                        description = "Your brand identity and home navigation",
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ControlDescription(
                        icon = Icons.Rounded.CalendarToday,
                        label = "Date Range",
                        description = "Filter data by time period",
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ControlDescription(
                        icon = Icons.Rounded.Share,
                        label = "Export",
                        description = "Share or download reports",
                    )
                }
            }
        }

        step(DashTarget.Revenue) {
            additionalTargets = listOf(DashTarget.Users, DashTarget.Orders, DashTarget.Growth)
            highlightStyle = HighlightStyle.Spotlight(shape = SpotlightShape.RoundedRect(12.dp))
            content { scope ->
                TooltipCard(
                    title = "Key Metrics",
                    stepLabel = "2 of 3",
                    scope = scope,
                ) {
                    MetricDescription(label = "Revenue", description = "Total earnings this period")
                    Spacer(modifier = Modifier.height(6.dp))
                    MetricDescription(label = "Users", description = "Active user count")
                    Spacer(modifier = Modifier.height(6.dp))
                    MetricDescription(label = "Orders", description = "Completed transactions")
                    Spacer(modifier = Modifier.height(6.dp))
                    MetricDescription(label = "Growth", description = "Period-over-period change")
                }
            }
        }

        step(DashTarget.Filter) {
            additionalTargets = listOf(DashTarget.Sort, DashTarget.Download)
            highlightStyle = HighlightStyle.Spotlight(shape = SpotlightShape.RoundedRect(8.dp))
            content { scope ->
                TooltipCard(
                    title = "Data Actions",
                    stepLabel = "3 of 3",
                    scope = scope,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        ActionDescription(label = "Filter", description = "Narrow results")
                        ActionDescription(label = "Sort", description = "Reorder data")
                        ActionDescription(label = "Download", description = "Export CSV")
                    }
                }
            }
        }
    }

    DemoScaffold(
        title = "Multi-Target Spotlight",
        description = "Highlights multiple elements per step with custom tooltip content",
        onBack = onBack,
        onStartTour = { state.start() },
    ) { padding ->
        WaypointHost(
            state = state,
            tooltipContent = { _, _ -> },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
            ) {
                HeaderRow(state)
                KpiRow(state)
                ChartPlaceholder()
                ActionBar(state)
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

// -- Header Row --

@Composable
private fun HeaderRow(
    state: com.mohamedrejeb.waypoint.core.WaypointState<DashTarget>,
) {
    Surface(
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            // Logo
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .waypointTarget(state, DashTarget.Logo),
            ) {
                Text(
                    text = "W",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )

            // Date range chip
            Surface(
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 4.dp,
                modifier = Modifier.waypointTarget(state, DashTarget.DateRange),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Last 7 days",
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Export button
            IconButton(
                onClick = { /* no-op */ },
                modifier = Modifier.waypointTarget(state, DashTarget.Export),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Share,
                    contentDescription = "Export",
                )
            }
        }
    }
}

// -- KPI Cards --

private val KpiBlue = Color(0xFF3B82F6)
private val KpiGreen = Color(0xFF10B981)
private val KpiPurple = Color(0xFF8B5CF6)
private val KpiOrange = Color(0xFFF59E0B)

private data class KpiData(
    val icon: ImageVector,
    val label: String,
    val value: String,
    val trend: String,
    val accentColor: Color,
    val target: DashTarget,
)

private val kpiItems = listOf(
    KpiData(Icons.Rounded.AttachMoney, "Revenue", "$24.5k", "+8.2%", KpiBlue, DashTarget.Revenue),
    KpiData(Icons.Rounded.Group, "Users", "1,847", "+3.1%", KpiGreen, DashTarget.Users),
    KpiData(Icons.Rounded.ShoppingCart, "Orders", "384", "+5.4%", KpiPurple, DashTarget.Orders),
    KpiData(Icons.AutoMirrored.Rounded.TrendingUp, "Growth", "+12.3%", "+2.1%", KpiOrange, DashTarget.Growth),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun KpiRow(
    state: com.mohamedrejeb.waypoint.core.WaypointState<DashTarget>,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        kpiItems.forEach { kpi ->
            KpiCard(
                kpi = kpi,
                modifier = Modifier
                    .weight(1f)
                    .waypointTarget(state, kpi.target),
            )
        }
    }
}

@Composable
private fun KpiCard(
    kpi: KpiData,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(kpi.accentColor.copy(alpha = 0.12f)),
                ) {
                    Icon(
                        imageVector = kpi.icon,
                        contentDescription = null,
                        tint = kpi.accentColor,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = kpi.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = kpi.value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = kpi.trend,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF10B981),
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

// -- Chart Placeholder --

@Composable
private fun ChartPlaceholder() {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            KpiBlue.copy(alpha = 0.08f),
                            KpiPurple.copy(alpha = 0.12f),
                        ),
                    ),
                ),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Revenue Trends",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Chart visualization area",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }
        }
    }
}

// -- Action Bar --

@Composable
private fun ActionBar(
    state: com.mohamedrejeb.waypoint.core.WaypointState<DashTarget>,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        OutlinedButton(
            onClick = { /* no-op */ },
            modifier = Modifier
                .weight(1f)
                .waypointTarget(state, DashTarget.Filter),
        ) {
            Icon(
                imageVector = Icons.Rounded.FilterList,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Filter")
        }

        OutlinedButton(
            onClick = { /* no-op */ },
            modifier = Modifier
                .weight(1f)
                .waypointTarget(state, DashTarget.Sort),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Sort,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Sort")
        }

        OutlinedButton(
            onClick = { /* no-op */ },
            modifier = Modifier
                .weight(1f)
                .waypointTarget(state, DashTarget.Download),
        ) {
            Icon(
                imageVector = Icons.Rounded.Download,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Download")
        }
    }
}

// -- Custom Tooltip Components --

@Composable
private fun TooltipCard(
    title: String,
    stepLabel: String,
    scope: com.mohamedrejeb.waypoint.core.StepScope,
    content: @Composable () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        modifier = Modifier.widthIn(max = 360.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Text(
                        text = stepLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            content()

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                TextButton(onClick = scope.onSkip) {
                    Text("Skip")
                }
                Spacer(modifier = Modifier.weight(1f))
                if (!scope.isFirstStep) {
                    OutlinedButton(
                        onClick = scope.onPrevious,
                        modifier = Modifier.padding(end = 8.dp),
                    ) {
                        Text("Back")
                    }
                }
                Button(
                    onClick = scope.onNext,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text(if (scope.isLastStep) "Done" else "Next")
                }
            }
        }
    }
}

@Composable
private fun ControlDescription(
    icon: ImageVector,
    label: String,
    description: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(16.dp),
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MetricDescription(
    label: String,
    description: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "- $description",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ActionDescription(
    label: String,
    description: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
