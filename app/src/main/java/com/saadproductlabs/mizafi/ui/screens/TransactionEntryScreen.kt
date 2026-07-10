package com.saadproductlabs.mizafi.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.saadproductlabs.mizafi.R
import com.saadproductlabs.mizafi.data.entity.TransactionEntity
import com.saadproductlabs.mizafi.data.entity.TransactionType
import com.saadproductlabs.mizafi.ui.components.CategoryChipSelector
import com.saadproductlabs.mizafi.ui.model.TransactionCategories
import com.saadproductlabs.mizafi.ui.util.currencyPrefix
import com.saadproductlabs.mizafi.ui.util.formatAmountForInput
import com.saadproductlabs.mizafi.ui.util.formatTransactionDate
import com.saadproductlabs.mizafi.ui.util.isValidAmountInput
import com.saadproductlabs.mizafi.ui.util.validateAmountForSave
import com.saadproductlabs.mizafi.viewmodel.TransactionViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionEntryScreen(
    viewModel: TransactionViewModel,
    transactionId: String?,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val amountFocusRequester = remember { FocusRequester() }
    val backDescription = stringResource(R.string.cd_back)
    val transactionTypeDescription = stringResource(R.string.cd_transaction_type)
    val expenseLabel = stringResource(R.string.label_expense)
    val incomeLabel = stringResource(R.string.label_income)
    val amountErrorMessage = stringResource(R.string.error_amount_positive)

    val existingTransaction = remember(transactionId, uiState.allTransactions) {
        transactionId?.let { id -> uiState.allTransactions.find { it.id == id } }
    }
    val isEditMode = transactionId != null

    var amount by remember(transactionId) { mutableStateOf("") }
    var selectedCategory by remember(transactionId) {
        mutableStateOf(TransactionCategories.expense.first())
    }
    var type by remember(transactionId) { mutableStateOf(TransactionType.EXPENSE) }
    var note by remember(transactionId) { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var showAmountError by remember { mutableStateOf(false) }

    LaunchedEffect(existingTransaction) {
        existingTransaction?.let { transaction ->
            amount = formatAmountForInput(transaction.amount)
            selectedCategory = transaction.category
            type = transaction.type
            note = transaction.note
        }
    }

    LaunchedEffect(transactionId, uiState.isDataReady, existingTransaction) {
        if (transactionId != null && uiState.isDataReady && existingTransaction == null) {
            onNavigateBack()
        }
    }

    LaunchedEffect(Unit) {
        if (transactionId == null) {
            // Wait until the text field's focus target is attached.
            withFrameNanos { }
            amountFocusRequester.requestFocus()
        }
    }

    val categories = remember(type, selectedCategory) {
        TransactionCategories.optionsFor(type, selectedCategory)
    }

    LaunchedEffect(type) {
        if (selectedCategory !in TransactionCategories.forType(type)) {
            selectedCategory = TransactionCategories.forType(type).first()
        }
    }

    val canSave by remember {
        derivedStateOf {
            validateAmountForSave(amount) != null && !isSaving
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (isEditMode) R.string.title_edit_transaction
                            else R.string.title_add_transaction
                        ),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.semantics { contentDescription = backDescription }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = backDescription
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Button(
                    onClick = {
                        val amountValue = validateAmountForSave(amount)
                        if (amountValue == null) {
                            showAmountError = true
                            return@Button
                        }
                        scope.launch {
                            isSaving = true
                            val entity = if (isEditMode && existingTransaction != null) {
                                existingTransaction.copy(
                                    amount = amountValue,
                                    category = selectedCategory,
                                    type = type,
                                    note = note.trim()
                                )
                            } else {
                                TransactionEntity(
                                    amount = amountValue,
                                    category = selectedCategory,
                                    date = System.currentTimeMillis(),
                                    type = type,
                                    note = note.trim()
                                )
                            }
                            val success = viewModel.saveTransaction(
                                transaction = entity,
                                isUpdate = isEditMode && existingTransaction != null
                            )
                            isSaving = false
                            if (success) onNavigateBack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    shape = MaterialTheme.shapes.large,
                    enabled = canSave
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            stringResource(
                                if (isEditMode) R.string.action_save_changes
                                else R.string.action_save_transaction
                            ),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            if (isEditMode && existingTransaction != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.label_recorded_on),
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

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = transactionTypeDescription }
            ) {
                SegmentedButton(
                    selected = type == TransactionType.EXPENSE,
                    onClick = { type = TransactionType.EXPENSE },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    modifier = Modifier.semantics { contentDescription = expenseLabel }
                ) {
                    Text(expenseLabel)
                }
                SegmentedButton(
                    selected = type == TransactionType.INCOME,
                    onClick = { type = TransactionType.INCOME },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    modifier = Modifier.semantics { contentDescription = incomeLabel }
                ) {
                    Text(incomeLabel)
                }
            }

            OutlinedTextField(
                value = amount,
                onValueChange = {
                    if (isValidAmountInput(it)) {
                        amount = it
                        showAmountError = false
                    }
                },
                label = { Text(stringResource(R.string.label_amount)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(amountFocusRequester),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                prefix = { Text("${currencyPrefix()} ") },
                supportingText = {
                    if (
                        showAmountError ||
                        (amount.isNotEmpty() && validateAmountForSave(amount) == null)
                    ) {
                        Text(amountErrorMessage)
                    }
                },
                isError = showAmountError ||
                    (amount.isNotEmpty() && validateAmountForSave(amount) == null)
            )

            CategoryChipSelector(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )

            OutlinedTextField(
                value = note,
                onValueChange = { if (it.length <= 200) note = it },
                label = { Text(stringResource(R.string.label_notes_optional)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                supportingText = {
                    Text(stringResource(R.string.character_count, note.length))
                }
            )

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
