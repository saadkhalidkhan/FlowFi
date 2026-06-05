package com.saadproductlabs.mizafi

import com.saadproductlabs.mizafi.data.entity.TransactionEntity
import com.saadproductlabs.mizafi.data.entity.TransactionType
import com.saadproductlabs.mizafi.viewmodel.TransactionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionViewModelRestoreTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun restoreTransaction_bringsBackDeletedItem() = runTest {
        val repository = FakeTransactionRepository()
        val viewModel = TransactionViewModel(repository, FakeSavingsGoalRepository())
        val transaction = TransactionEntity(
            amount = 25.0,
            category = "Food",
            date = System.currentTimeMillis(),
            type = TransactionType.EXPENSE,
            note = "Lunch"
        )

        viewModel.addTransaction(transaction)
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.first().allTransactions.size)

        viewModel.deleteTransaction(transaction)
        advanceUntilIdle()
        assertEquals(0, viewModel.uiState.first().allTransactions.size)

        viewModel.restoreTransaction(transaction)
        advanceUntilIdle()
        val restored = viewModel.uiState.first().allTransactions.single()
        assertEquals(transaction.id, restored.id)
        assertEquals(transaction.amount, restored.amount, 0.0)
    }
}
