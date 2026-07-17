package com.saadproductlabs.mizafi

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.saadproductlabs.mizafi.data.entity.SavingsGoalEntity
import com.saadproductlabs.mizafi.data.entity.TransactionEntity
import com.saadproductlabs.mizafi.data.entity.TransactionType
import com.saadproductlabs.mizafi.data.repository.SavingsGoalRepository
import com.saadproductlabs.mizafi.data.repository.TransactionRepository
import com.saadproductlabs.mizafi.ui.screens.TransactionEntryScreen
import com.saadproductlabs.mizafi.ui.theme.MizafiTheme
import com.saadproductlabs.mizafi.viewmodel.TransactionViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransactionEntryScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun addTransaction_validInput_savesAndNavigatesBack() {
        val repository = RecordingTransactionRepository()
        val viewModel = TransactionViewModel(repository, EmptySavingsGoalRepository())
        var navigatedBack = false

        composeRule.setContent {
            MizafiTheme {
                TransactionEntryScreen(
                    viewModel = viewModel,
                    transactionId = null,
                    onNavigateBack = { navigatedBack = true }
                )
            }
        }

        composeRule
            .onNodeWithText(context.getString(R.string.label_amount))
            .performTextInput("42.50")
        composeRule.onNodeWithText(context.getString(R.string.category_shopping)).performClick()
        composeRule
            .onNodeWithText(context.getString(R.string.action_save_transaction))
            .assertIsEnabled()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            repository.transactions.value.size == 1 && navigatedBack
        }

        val saved = repository.transactions.value.single()
        assertEquals(42.50, saved.amount, 0.0)
        assertEquals("Shopping", saved.category)
        assertEquals(TransactionType.EXPENSE, saved.type)
    }

    @Test
    fun editTransaction_prefillsAndUpdatesOriginalRecord() {
        val original = TransactionEntity(
            id = "existing-id",
            amount = 25.0,
            category = "Food",
            date = 1_700_000_000_000,
            type = TransactionType.EXPENSE,
            note = "Lunch"
        )
        val repository = RecordingTransactionRepository(listOf(original))
        val viewModel = TransactionViewModel(repository, EmptySavingsGoalRepository())
        var navigatedBack = false

        composeRule.setContent {
            MizafiTheme {
                TransactionEntryScreen(
                    viewModel = viewModel,
                    transactionId = original.id,
                    onNavigateBack = { navigatedBack = true }
                )
            }
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule
                .onAllNodes(hasSetTextAction() and hasText("25"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule
            .onNode(hasSetTextAction() and hasText("25"))
            .performTextReplacement("30")
        composeRule
            .onNode(hasSetTextAction() and hasText("Lunch"))
            .performTextReplacement("Dinner")
        composeRule
            .onNodeWithText(context.getString(R.string.action_save_changes))
            .assertIsEnabled()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            repository.lastUpdated != null && navigatedBack
        }

        val updated = requireNotNull(repository.lastUpdated)
        assertEquals(original.id, updated.id)
        assertEquals(original.date, updated.date)
        assertEquals(30.0, updated.amount, 0.0)
        assertEquals("Dinner", updated.note)
        assertTrue(repository.transactions.value.any { it == updated })
    }
}

private class RecordingTransactionRepository(
    initialTransactions: List<TransactionEntity> = emptyList()
) : TransactionRepository {
    val transactions = MutableStateFlow(initialTransactions)
    var lastUpdated: TransactionEntity? = null
        private set

    override fun getAllTransactions(): Flow<List<TransactionEntity>> = transactions

    override suspend fun getTransactionById(id: String): TransactionEntity? =
        transactions.value.find { it.id == id }

    override suspend fun insertTransaction(transaction: TransactionEntity) {
        transactions.value = transactions.value
            .filterNot { it.id == transaction.id } + transaction
    }

    override suspend fun updateTransaction(transaction: TransactionEntity) {
        lastUpdated = transaction
        insertTransaction(transaction)
    }

    override suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactions.value = transactions.value.filterNot { it.id == transaction.id }
    }

    override fun getTotalIncome(): Flow<Double?> =
        transactions.map { items ->
            items.filter { it.type == TransactionType.INCOME }
                .sumOf(TransactionEntity::amount)
        }

    override fun getTotalExpenses(): Flow<Double?> =
        transactions.map { items ->
            items.filter { it.type == TransactionType.EXPENSE }
                .sumOf(TransactionEntity::amount)
        }
}

private class EmptySavingsGoalRepository : SavingsGoalRepository {
    private val goals = MutableStateFlow<List<SavingsGoalEntity>>(emptyList())

    override fun getAllGoals(): Flow<List<SavingsGoalEntity>> = goals

    override suspend fun insertGoal(goal: SavingsGoalEntity) {
        goals.value = goals.value + goal
    }

    override suspend fun updateGoal(goal: SavingsGoalEntity) {
        goals.value = goals.value.map { if (it.id == goal.id) goal else it }
    }

    override suspend fun deleteGoal(goal: SavingsGoalEntity) {
        goals.value = goals.value.filterNot { it.id == goal.id }
    }
}
