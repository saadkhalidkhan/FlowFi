package com.saadproductlabs.mizafi.ui.screens

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.saadproductlabs.mizafi.R
import com.saadproductlabs.mizafi.data.entity.SavingsGoalEntity
import com.saadproductlabs.mizafi.domain.BehavioralInsight
import com.saadproductlabs.mizafi.domain.InsightTone
import com.saadproductlabs.mizafi.ui.components.CategoryBarChart
import com.saadproductlabs.mizafi.ui.theme.ExpenseRed
import com.saadproductlabs.mizafi.ui.theme.ExpenseRedContainer
import com.saadproductlabs.mizafi.ui.theme.IncomeGreen
import com.saadproductlabs.mizafi.ui.theme.IncomeGreenContainer
import com.saadproductlabs.mizafi.ui.util.formatCurrency
import com.saadproductlabs.mizafi.ui.util.formatCurrentMonthYear
import com.saadproductlabs.mizafi.ui.util.isValidAmountInput
import com.saadproductlabs.mizafi.ui.util.validateAmountForSave
import com.saadproductlabs.mizafi.viewmodel.TransactionViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    viewModel: TransactionViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val goalDeletedMessage = stringResource(R.string.msg_goal_deleted)
    val undoLabel = stringResource(R.string.action_undo)
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.title_insights),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
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
                icon = {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.cd_add_savings_goal)
                    )
                },
                text = { Text(stringResource(R.string.action_new_goal)) },
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
                    title = stringResource(R.string.title_insight_engine),
                    subtitle = stringResource(R.string.subtitle_insight_engine),
                    icon = Icons.Default.Lightbulb
                )
            }
            items(uiState.behavioralInsights) { insight ->
                BehavioralInsightCard(insight = insight)
            }

            item {
                SectionHeader(
                    title = stringResource(R.string.title_category_analytics),
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
                    title = stringResource(R.string.title_savings_goals),
                    subtitle = stringResource(R.string.subtitle_savings_goals),
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
                        onDelete = {
                            scope.launch {
                                viewModel.deleteSavingsGoal(goal)
                                val result = snackbarHostState.showSnackbar(
                                    message = goalDeletedMessage,
                                    actionLabel = undoLabel,
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.restoreSavingsGoal(goal)
                                }
                            }
                        }
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
                text = stringResource(R.string.guidance_hero_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.guidance_hero_description),
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
            contentDescription = title,
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

    val iconDescription = when (insight.tone) {
        InsightTone.POSITIVE -> stringResource(R.string.cd_positive_insight)
        InsightTone.WARNING -> stringResource(R.string.cd_warning_insight)
        InsightTone.NEUTRAL -> stringResource(R.string.cd_insights)
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
            Icon(icon, contentDescription = iconDescription, tint = contentColor)
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
    breakdown: List<com.saadproductlabs.mizafi.domain.CategorySpend>,
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
                    text = stringResource(R.string.empty_analytics),
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
                        contentDescription = stringResource(R.string.cd_delete_goal),
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
                text = stringResource(
                    R.string.goal_saved_amount,
                    formatCurrency(goal.currentAmount),
                    formatCurrency(goal.targetAmount)
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(
                    R.string.goal_progress,
                    (goal.progress * 100).toInt()
                ),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = IncomeGreen
            )
            Spacer(modifier = Modifier.height(12.dp))
            FilledTonalButton(onClick = onAddFunds, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.action_add_funds))
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
                text = stringResource(R.string.empty_goals_description),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            FilledTonalButton(onClick = onAddGoal) {
                Text(stringResource(R.string.action_create_first_goal))
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
    val canSave = name.isNotBlank() && validateAmountForSave(target) != null
    val goalPresets = listOf(
        stringResource(R.string.goal_preset_emergency_fund),
        stringResource(R.string.goal_preset_bike),
        stringResource(R.string.goal_preset_marriage),
        stringResource(R.string.goal_preset_travel)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.title_new_savings_goal)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    stringResource(R.string.label_quick_picks),
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
                    label = { Text(stringResource(R.string.label_goal_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = target,
                    onValueChange = { if (isValidAmountInput(it)) target = it },
                    label = { Text(stringResource(R.string.label_target_amount)) },
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
                            targetAmount = validateAmountForSave(target)!!
                        )
                    )
                },
                enabled = canSave
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
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
    val value = validateAmountForSave(amount)
    val canConfirm = value != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.title_add_to_goal, goalName)) },
        text = {
            OutlinedTextField(
                value = amount,
                onValueChange = { if (isValidAmountInput(it)) amount = it },
                label = { Text(stringResource(R.string.label_amount)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(value!!) }, enabled = canConfirm) {
                Text(stringResource(R.string.action_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}
