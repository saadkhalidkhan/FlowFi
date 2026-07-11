package com.saadproductlabs.mizafi.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.saadproductlabs.mizafi.R
import com.saadproductlabs.mizafi.domain.BehavioralInsight
import com.saadproductlabs.mizafi.ui.components.MizafiEmptyState
import com.saadproductlabs.mizafi.ui.components.TransactionItem
import com.saadproductlabs.mizafi.ui.theme.ExpenseRed
import com.saadproductlabs.mizafi.ui.theme.IncomeGreen
import com.saadproductlabs.mizafi.ui.util.formatCurrency
import com.saadproductlabs.mizafi.ui.util.formatCurrentMonthYear
import com.saadproductlabs.mizafi.ui.util.formatTransactionDate
import com.saadproductlabs.mizafi.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: TransactionViewModel,
    onNavigateToEntry: () -> Unit,
    onNavigateToList: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToInsights: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val addTransactionDescription = stringResource(R.string.cd_add_transaction)
    val viewAllDescription = stringResource(R.string.cd_view_all_transactions)

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.app_name), fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToEntry,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.semantics {
                    contentDescription = addTransactionDescription
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) { innerPadding ->
        if (!uiState.isDataReady) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically { it / 4 }
            ) {
                SummaryCard(
                    balance = uiState.balance,
                    income = uiState.totalIncome,
                    expenses = uiState.totalExpenses
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            GuidancePreviewCard(
                topInsight = uiState.behavioralInsights.firstOrNull(),
                onViewInsights = onNavigateToInsights
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.title_recent_transactions),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (uiState.allTransactions.isNotEmpty()) {
                    TextButton(
                        onClick = onNavigateToList,
                        modifier = Modifier.semantics {
                            contentDescription = viewAllDescription
                        }
                    ) {
                        Text(stringResource(R.string.action_view_all))
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.monthlyTransactions.isEmpty()) {
                MizafiEmptyState(
                    title = stringResource(R.string.empty_month_title),
                    description = stringResource(R.string.empty_month_description),
                    icon = Icons.Default.ReceiptLong,
                    iconContentDescription = stringResource(R.string.cd_no_transactions),
                    actionLabel = stringResource(R.string.cd_add_transaction),
                    actionIcon = Icons.Default.Add,
                    onAction = onNavigateToEntry,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(
                        items = uiState.monthlyTransactions.take(5),
                        key = { it.id }
                    ) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            formattedDate = formatTransactionDate(transaction.date),
                            onClick = { onNavigateToEdit(transaction.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GuidancePreviewCard(
    topInsight: BehavioralInsight?,
    onViewInsights: () -> Unit
) {
    val viewInsightsDescription = stringResource(R.string.cd_view_insights)
    Card(
        onClick = onViewInsights,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = viewInsightsDescription },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.title_guidance),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    Icons.Default.Insights,
                    contentDescription = stringResource(R.string.cd_insights),
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = topInsight?.message
                    ?: stringResource(R.string.guidance_fallback),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.action_view_insights),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun SummaryCard(balance: Double, income: Double, expenses: Double) {
    val balanceLabel = when {
        balance > 0 -> stringResource(R.string.label_positive_balance)
        balance < 0 -> stringResource(R.string.label_negative_balance)
        else -> stringResource(R.string.label_even_balance)
    }
    val balanceDescription = stringResource(
        R.string.cd_balance_summary,
        balanceLabel,
        formatCurrency(balance),
        formatCurrentMonthYear()
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = balanceDescription
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(R.string.label_total_balance),
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = formatCurrentMonthYear(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = formatCurrency(balance),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = when {
                    balance > 0 -> IncomeGreen
                    balance < 0 -> ExpenseRed
                    else -> MaterialTheme.colorScheme.onSecondaryContainer
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    label = stringResource(R.string.label_income),
                    amount = income,
                    icon = Icons.Default.ArrowUpward,
                    color = IncomeGreen,
                    iconDescription = stringResource(R.string.label_income)
                )
                SummaryItem(
                    label = stringResource(R.string.label_expenses),
                    amount = expenses,
                    icon = Icons.Default.ArrowDownward,
                    color = ExpenseRed,
                    iconDescription = stringResource(R.string.label_expenses)
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    amount: Double,
    icon: ImageVector,
    color: Color,
    iconDescription: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = iconDescription,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(
                formatCurrency(amount),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
