package com.example.flowfi.ui.screens

import androidx.compose.foundation.layout.*
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
import com.example.flowfi.ui.util.isValidAmountInput
import com.example.flowfi.viewmodel.TransactionViewModel

private val expenseCategories = listOf("Food", "Transport", "Bills", "Shopping", "Other")
private val incomeCategories = listOf("Salary", "Other")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionEntryScreen(
    viewModel: TransactionViewModel,
    onNavigateBack: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(expenseCategories.first()) }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var note by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val categories = if (type == TransactionType.EXPENSE) expenseCategories else incomeCategories

    LaunchedEffect(type) {
        if (selectedCategory !in categories) {
            selectedCategory = categories.first()
        }
    }

    val amountValue = amount.toDoubleOrNull()
    val canSave = amountValue != null && amountValue > 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Transaction", fontWeight = FontWeight.Bold) },
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
                        viewModel.addTransaction(
                            TransactionEntity(
                                amount = amountValue!!,
                                category = selectedCategory,
                                date = System.currentTimeMillis(),
                                type = type,
                                note = note.trim()
                            )
                        )
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    shape = MaterialTheme.shapes.large,
                    enabled = canSave
                ) {
                    Text("Save Transaction", style = MaterialTheme.typography.titleMedium)
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
                prefix = { Text("$ ") },
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
