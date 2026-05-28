package com.example.flowfi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.flowfi.data.entity.TransactionEntity
import com.example.flowfi.data.entity.TransactionType
import com.example.flowfi.ui.model.TransactionCategories
import com.example.flowfi.ui.util.currencyPrefix
import com.example.flowfi.ui.util.formatAmountForInput
import com.example.flowfi.ui.util.formatTransactionDate
import com.example.flowfi.ui.util.isValidAmountInput
import com.example.flowfi.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionEntryScreen(
    viewModel: TransactionViewModel,
    transactionId: String?,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val existingTransaction = transactionId?.let { id ->
        uiState.allTransactions.find { it.id == id }
    }
    val isEditMode = existingTransaction != null

    var amount by remember(transactionId) {
        mutableStateOf(existingTransaction?.let { formatAmountForInput(it.amount) } ?: "")
    }
    var selectedCategory by remember(transactionId) {
        mutableStateOf(existingTransaction?.category ?: TransactionCategories.expense.first())
    }
    var type by remember(transactionId) {
        mutableStateOf(existingTransaction?.type ?: TransactionType.EXPENSE)
    }
    var note by remember(transactionId) {
        mutableStateOf(existingTransaction?.note ?: "")
    }
    var expanded by remember { mutableStateOf(false) }

    val categories = remember(type, selectedCategory) {
        TransactionCategories.optionsFor(type, selectedCategory)
    }

    LaunchedEffect(type) {
        if (selectedCategory !in TransactionCategories.forType(type)) {
            selectedCategory = TransactionCategories.forType(type).first()
        }
    }

    val amountValue = amount.toDoubleOrNull()
    val canSave = amountValue != null && amountValue > 0

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditMode) "Edit Transaction" else "Add Transaction",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Button(
                    onClick = {
                        if (isEditMode && existingTransaction != null) {
                            viewModel.updateTransaction(
                                existingTransaction.copy(
                                    amount = amountValue!!,
                                    category = selectedCategory,
                                    type = type,
                                    note = note.trim()
                                )
                            )
                        } else {
                            viewModel.addTransaction(
                                TransactionEntity(
                                    amount = amountValue!!,
                                    category = selectedCategory,
                                    date = System.currentTimeMillis(),
                                    type = type,
                                    note = note.trim()
                                )
                            )
                        }
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    shape = MaterialTheme.shapes.large,
                    enabled = canSave
                ) {
                    Text(
                        if (isEditMode) "Save Changes" else "Save Transaction",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            if (isEditMode && existingTransaction != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Recorded on",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatTransactionDate(existingTransaction.date),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = type == TransactionType.EXPENSE,
                    onClick = { type = TransactionType.EXPENSE },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text("Expense")
                }
                SegmentedButton(
                    selected = type == TransactionType.INCOME,
                    onClick = { type = TransactionType.INCOME },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text("Income")
                }
            }

            OutlinedTextField(
                value = amount,
                onValueChange = { if (isValidAmountInput(it)) amount = it },
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                prefix = { Text("${currencyPrefix()} ") },
                supportingText = {
                    if (amount.isNotEmpty() && !canSave) {
                        Text("Enter an amount greater than 0")
                    }
                },
                isError = amount.isNotEmpty() && !canSave
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Notes (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
