package com.saadproductlabs.mizafi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.saadproductlabs.mizafi.ui.model.TransactionListFilters
import com.saadproductlabs.mizafi.ui.theme.ExpenseRed
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
    var selectedMonthKey by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }

    val monthOptions = remember(uiState.allTransactions) {
        TransactionListFilters.monthOptions(uiState.allTransactions)
    }
    val categoryOptions = remember(uiState.allTransactions) {
        TransactionListFilters.categoryOptions(uiState.allTransactions)
    }
    val filteredTransactions = remember(
        uiState.allTransactions,
        selectedMonthKey,
        selectedCategory
    ) {
        TransactionListFilters.filter(uiState.allTransactions, selectedMonthKey, selectedCategory)
    }
    val selectedMonthLabel = remember(selectedMonthKey, monthOptions) {
        monthOptions.find { it.key == selectedMonthKey }?.label
            ?: TransactionListFilters.ALL_MONTHS_LABEL
    }
    val selectedCategoryLabel = selectedCategory ?: TransactionListFilters.ALL_CATEGORIES_LABEL
    val filtersActive = selectedMonthKey != null || selectedCategory != null

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
                title = { Text("All Transactions", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.allTransactions.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No transactions yet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Go back and tap + on the dashboard to add your first transaction.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
            ) {
                item {
                    Text(
                        text = "Tap to edit · Swipe left to delete",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
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
                        FilterEmptyState(
                            filtersActive = filtersActive,
                            onClearFilters = {
                                selectedMonthKey = null
                                selectedCategory = null
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
                                            message = "Transaction deleted",
                                            actionLabel = "Undo",
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
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
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
    monthOptions: List<com.saadproductlabs.mizafi.ui.model.MonthFilterOption>,
    categoryOptions: List<String>,
    onMonthSelected: (String?) -> Unit,
    onCategorySelected: (String?) -> Unit
) {
    var monthExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterDropdownField(
            label = "Month",
            value = selectedMonthLabel,
            expanded = monthExpanded,
            onExpandedChange = { monthExpanded = it },
            onDismiss = { monthExpanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(TransactionListFilters.ALL_MONTHS_LABEL) },
                onClick = {
                    onMonthSelected(null)
                    monthExpanded = false
                }
            )
            monthOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onMonthSelected(option.key)
                        monthExpanded = false
                    }
                )
            }
        }
        FilterDropdownField(
            label = "Category",
            value = selectedCategoryLabel,
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = it },
            onDismiss = { categoryExpanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(TransactionListFilters.ALL_CATEGORIES_LABEL) },
                onClick = {
                    onCategorySelected(null)
                    categoryExpanded = false
                }
            )
            categoryOptions.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = {
                        onCategorySelected(category)
                        categoryExpanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterDropdownField(
    label: String,
    value: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    menuContent: @Composable ColumnScope.() -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss,
            content = menuContent
        )
    }
}

@Composable
private fun FilterEmptyState(
    filtersActive: Boolean,
    onClearFilters: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (filtersActive) {
                "No transactions match your filters"
            } else {
                "No transactions to show"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        if (filtersActive) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onClearFilters) {
                Text("Clear filters")
            }
        }
    }
}
