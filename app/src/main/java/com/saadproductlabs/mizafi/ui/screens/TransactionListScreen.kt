package com.saadproductlabs.mizafi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.saadproductlabs.mizafi.R
import com.saadproductlabs.mizafi.ui.components.MizafiDropdownField
import com.saadproductlabs.mizafi.ui.components.MizafiDropdownMenuItem
import com.saadproductlabs.mizafi.ui.components.MizafiEmptyState
import com.saadproductlabs.mizafi.ui.components.TransactionItem
import com.saadproductlabs.mizafi.ui.model.MonthFilterOption
import com.saadproductlabs.mizafi.ui.model.TransactionListFilters
import com.saadproductlabs.mizafi.ui.theme.ExpenseRed
import com.saadproductlabs.mizafi.ui.util.categoryDisplayName
import com.saadproductlabs.mizafi.ui.util.formatTransactionDate
import com.saadproductlabs.mizafi.viewmodel.TransactionViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    viewModel: TransactionViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val transactionDeletedMessage = stringResource(R.string.msg_transaction_deleted)
    val undoLabel = stringResource(R.string.action_undo)
    val deleteTransactionDescription = stringResource(R.string.cd_delete_transaction)
    val deleteDescription = stringResource(R.string.cd_delete)
    var selectedMonthKey by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }

    val monthOptions = remember(uiState.allTransactions) {
        TransactionListFilters.monthOptions(uiState.allTransactions)
    }
    val categoryOptions = remember(uiState.allTransactions) {
        TransactionListFilters.categoryOptions(uiState.allTransactions)
    }
    val filteredTransactions by remember {
        derivedStateOf {
            TransactionListFilters.filter(
                uiState.allTransactions,
                selectedMonthKey,
                selectedCategory
            )
        }
    }
    val selectedMonthLabel = monthOptions.find { it.key == selectedMonthKey }?.label
        ?: stringResource(R.string.filter_all_months)
    val selectedCategoryLabel = selectedCategory?.let { categoryDisplayName(it) }
        ?: stringResource(R.string.filter_all_categories)
    val filtersActive by remember {
        derivedStateOf {
            selectedMonthKey != null || selectedCategory != null
        }
    }

    LaunchedEffect(monthOptions, selectedMonthKey) {
        if (selectedMonthKey != null && monthOptions.none { it.key == selectedMonthKey }) {
            selectedMonthKey = null
        }
    }
    LaunchedEffect(categoryOptions, selectedCategory) {
        if (selectedCategory != null && selectedCategory !in categoryOptions) {
            selectedCategory = null
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.title_all_transactions),
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
                }
            )
        }
    ) { innerPadding ->
        if (!uiState.isDataReady) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (uiState.allTransactions.isEmpty()) {
            MizafiEmptyState(
                title = stringResource(R.string.empty_transactions_title),
                description = stringResource(R.string.empty_transactions_description),
                icon = Icons.Default.ReceiptLong,
                iconContentDescription = stringResource(R.string.cd_empty_transaction_list),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
            ) {
                item {
                    Text(
                        text = stringResource(R.string.transaction_list_hint),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                item {
                    TransactionFilterFields(
                        selectedMonthLabel = selectedMonthLabel,
                        selectedCategoryLabel = selectedCategoryLabel,
                        monthOptions = monthOptions,
                        categoryOptions = categoryOptions,
                        onMonthSelected = { selectedMonthKey = it },
                        onCategorySelected = { selectedCategory = it }
                    )
                }
                if (filteredTransactions.isEmpty()) {
                    item {
                        MizafiEmptyState(
                            title = if (filtersActive) {
                                stringResource(R.string.empty_filters_title)
                            } else {
                                stringResource(R.string.empty_filtered_list_title)
                            },
                            description = if (filtersActive) {
                                stringResource(R.string.empty_filters_description)
                            } else {
                                stringResource(R.string.empty_filtered_list_description)
                            },
                            secondaryActionLabel = if (filtersActive) {
                                stringResource(R.string.action_clear_filters)
                            } else {
                                null
                            },
                            onSecondaryAction = if (filtersActive) {
                                {
                                    selectedMonthKey = null
                                    selectedCategory = null
                                }
                            } else {
                                null
                            }
                        )
                    }
                } else {
                    items(
                        items = filteredTransactions,
                        key = { it.id }
                    ) { transaction ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.EndToStart) {
                                    scope.launch {
                                        viewModel.deleteTransaction(transaction)
                                        val result = snackbarHostState.showSnackbar(
                                            message = transactionDeletedMessage,
                                            actionLabel = undoLabel,
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.restoreTransaction(transaction)
                                        }
                                    }
                                    true
                                } else {
                                    false
                                }
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false,
                            backgroundContent = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(ExpenseRed, MaterialTheme.shapes.large)
                                        .padding(horizontal = 20.dp)
                                        .semantics {
                                            contentDescription = deleteTransactionDescription
                                        },
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = deleteDescription,
                                        tint = Color.White
                                    )
                                }
                            }
                        ) {
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionFilterFields(
    selectedMonthLabel: String,
    selectedCategoryLabel: String,
    monthOptions: List<MonthFilterOption>,
    categoryOptions: List<String>,
    onMonthSelected: (String?) -> Unit,
    onCategorySelected: (String?) -> Unit
) {
    var monthExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MizafiDropdownField(
            label = stringResource(R.string.label_month),
            value = selectedMonthLabel,
            expanded = monthExpanded,
            onExpandedChange = { monthExpanded = it },
            onDismiss = { monthExpanded = false }
        ) {
            MizafiDropdownMenuItem(
                text = stringResource(R.string.filter_all_months),
                onClick = {
                    onMonthSelected(null)
                    monthExpanded = false
                }
            )
            monthOptions.forEach { option ->
                MizafiDropdownMenuItem(
                    text = option.label,
                    onClick = {
                        onMonthSelected(option.key)
                        monthExpanded = false
                    }
                )
            }
        }
        MizafiDropdownField(
            label = stringResource(R.string.label_category),
            value = selectedCategoryLabel,
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = it },
            onDismiss = { categoryExpanded = false }
        ) {
            MizafiDropdownMenuItem(
                text = stringResource(R.string.filter_all_categories),
                onClick = {
                    onCategorySelected(null)
                    categoryExpanded = false
                }
            )
            categoryOptions.forEach { category ->
                MizafiDropdownMenuItem(
                    text = categoryDisplayName(category),
                    onClick = {
                        onCategorySelected(category)
                        categoryExpanded = false
                    }
                )
            }
        }
    }
}
