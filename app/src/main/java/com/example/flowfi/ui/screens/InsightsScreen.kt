package com.example.flowfi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.flowfi.data.entity.SavingsGoalEntity
import com.example.flowfi.domain.BehavioralInsight
import com.example.flowfi.domain.InsightTone
import com.example.flowfi.ui.components.CategoryBarChart
import com.example.flowfi.ui.theme.ExpenseRed
import com.example.flowfi.ui.theme.ExpenseRedContainer
import com.example.flowfi.ui.theme.IncomeGreen
import com.example.flowfi.ui.theme.IncomeGreenContainer
import com.example.flowfi.ui.util.formatCurrency
import com.example.flowfi.ui.util.formatCurrentMonthYear
import com.example.flowfi.ui.util.isValidAmountInput
import com.example.flowfi.viewmodel.TransactionViewModel

private val goalPresets = listOf("Emergency fund", "Bike", "Marriage", "Travel")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    viewModel: TransactionViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var goalToFund by remember { mutableStateOf<SavingsGoalEntity?>(null) }

    val chartColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary,
        ExpenseRed,
        IncomeGreen,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.primaryContainer
    )

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("Insights", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddGoalDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New goal") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
        ) {
            item {
                GuidanceHeroCard()
            }

            item {
                SectionHeader(
                    title = "Insight engine",
                    subtitle = "Guidance based on your spending patterns",
                    icon = Icons.Default.Lightbulb
                )
            }
            items(uiState.behavioralInsights) { insight ->
                BehavioralInsightCard(insight = insight)
            }

            item {
                SectionHeader(
                    title = "Category analytics",
                    subtitle = formatCurrentMonthYear(),
                    icon = Icons.Default.PieChart
                )
            }
            item {
                AnalyticsCard(
                    breakdown = uiState.categoryBreakdown,
                    chartColors = chartColors
                )
            }

            item {
                SectionHeader(
                    title = "Savings goals",
                    subtitle = "Track progress toward what matters",
                    icon = Icons.Default.Savings
                )
            }
            if (uiState.savingsGoals.isEmpty()) {
                item {
                    EmptyGoalsCard(onAddGoal = { showAddGoalDialog = true })
                }
            } else {
                items(uiState.savingsGoals, key = { it.id }) { goal ->
                    SavingsGoalCard(
                        goal = goal,
                        onAddFunds = { goalToFund = goal },
                        onDelete = { viewModel.deleteSavingsGoal(goal) }
                    )
                }
            }
        }
    }

    if (showAddGoalDialog) {
        AddSavingsGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onSave = { goal ->
                viewModel.addSavingsGoal(goal)
                showAddGoalDialog = false
            }
        )
    }

    goalToFund?.let { goal ->
        AddFundsDialog(
            goalName = goal.name,
            onDismiss = { goalToFund = null },
            onConfirm = { amount ->
                viewModel.updateSavingsGoal(
                    goal.copy(currentAmount = goal.currentAmount + amount)
                )
                goalToFund = null
            }
        )
    }
}

@Composable
private fun GuidanceHeroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Turn data into decisions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "FlowFi highlights trends, category mix, and goal progress so you can act—not just read numbers.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    icon: ImageVector
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun BehavioralInsightCard(insight: BehavioralInsight) {
    val containerColor = when (insight.tone) {
        InsightTone.POSITIVE -> IncomeGreenContainer
        InsightTone.WARNING -> ExpenseRedContainer
        InsightTone.NEUTRAL -> MaterialTheme.colorScheme.tertiaryContainer
    }
    val contentColor = when (insight.tone) {
        InsightTone.POSITIVE -> IncomeGreen
        InsightTone.WARNING -> ExpenseRed
        InsightTone.NEUTRAL -> MaterialTheme.colorScheme.tertiary
    }
    val icon = when (insight.tone) {
        InsightTone.POSITIVE -> Icons.Default.TrendingUp
        InsightTone.WARNING -> Icons.Default.TrendingDown
        InsightTone.NEUTRAL -> Icons.Default.Lightbulb
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(icon, contentDescription = null, tint = contentColor)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = insight.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun AnalyticsCard(
    breakdown: List<com.example.flowfi.domain.CategorySpend>,
    chartColors: List<Color>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            if (breakdown.isEmpty()) {
                Text(
                    text = "No expense data this month yet. Add spending to see category breakdown.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                CategoryBarChart(breakdown = breakdown, colors = chartColors)
            }
        }
    }
}

@Composable
private fun SavingsGoalCard(
    goal: SavingsGoalEntity,
    onAddFunds: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(goal.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete goal",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { goal.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
                color = IncomeGreen,
                trackColor = MaterialTheme.colorScheme.surface,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${formatCurrency(goal.currentAmount)} of ${formatCurrency(goal.targetAmount)} saved",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${(goal.progress * 100).toInt()}% complete",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = IncomeGreen
            )
            Spacer(modifier = Modifier.height(12.dp))
            FilledTonalButton(onClick = onAddFunds, modifier = Modifier.fillMaxWidth()) {
                Text("Add funds")
            }
        }
    }
}

@Composable
private fun EmptyGoalsCard(onAddGoal: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Set a goal for emergency savings, travel, or anything else.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            FilledTonalButton(onClick = onAddGoal) {
                Text("Create your first goal")
            }
        }
    }
}

@Composable
private fun AddSavingsGoalDialog(
    onDismiss: () -> Unit,
    onSave: (SavingsGoalEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    val canSave = name.isNotBlank() && (target.toDoubleOrNull() ?: 0.0) > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New savings goal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Quick picks",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(goalPresets) { preset ->
                        FilterChip(
                            selected = name == preset,
                            onClick = { name = preset },
                            label = { Text(preset) }
                        )
                    }
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Goal name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = target,
                    onValueChange = { if (isValidAmountInput(it)) target = it },
                    label = { Text("Target amount") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        SavingsGoalEntity(
                            name = name.trim(),
                            targetAmount = target.toDouble()
                        )
                    )
                },
                enabled = canSave
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun AddFundsDialog(
    goalName: String,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    val value = amount.toDoubleOrNull()
    val canConfirm = value != null && value > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to $goalName") },
        text = {
            OutlinedTextField(
                value = amount,
                onValueChange = { if (isValidAmountInput(it)) amount = it },
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(value!!) }, enabled = canConfirm) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
