package com.saadproductlabs.mizafi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saadproductlabs.mizafi.data.entity.SavingsGoalEntity
import com.saadproductlabs.mizafi.data.entity.TransactionEntity
import com.saadproductlabs.mizafi.data.entity.TransactionType
import com.saadproductlabs.mizafi.data.repository.SavingsGoalRepository
import com.saadproductlabs.mizafi.data.repository.TransactionRepository
import com.saadproductlabs.mizafi.domain.BehavioralInsight
import com.saadproductlabs.mizafi.domain.CategoryAnalytics
import com.saadproductlabs.mizafi.domain.CategorySpend
import com.saadproductlabs.mizafi.domain.InsightEngine
import com.saadproductlabs.mizafi.util.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TransactionUiState(
    val allTransactions: List<TransactionEntity> = emptyList(),
    val monthlyTransactions: List<TransactionEntity> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val balance: Double = 0.0,
    val behavioralInsights: List<BehavioralInsight> = emptyList(),
    val categoryBreakdown: List<CategorySpend> = emptyList(),
    val savingsGoals: List<SavingsGoalEntity> = emptyList()
)

class TransactionViewModel(
    private val repository: TransactionRepository,
    private val savingsGoalRepository: SavingsGoalRepository
) : ViewModel() {

    val uiState: StateFlow<TransactionUiState> = combine(
        repository.getAllTransactions(),
        savingsGoalRepository.getAllGoals()
    ) { all, goals ->
        val monthRange = DateUtils.currentMonthRangeMillis()
        val monthly = all.filter { it.date in monthRange }
        val income = monthly.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expenses = monthly.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        TransactionUiState(
            allTransactions = all,
            monthlyTransactions = monthly,
            totalIncome = income,
            totalExpenses = expenses,
            balance = income - expenses,
            behavioralInsights = InsightEngine.generate(all),
            categoryBreakdown = CategoryAnalytics.monthlyExpenseBreakdown(all),
            savingsGoals = goals
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TransactionUiState()
    )

    fun addTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.insertTransaction(transaction)
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun restoreTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.insertTransaction(transaction)
        }
    }

    fun addSavingsGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch {
            savingsGoalRepository.insertGoal(goal)
        }
    }

    fun updateSavingsGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch {
            savingsGoalRepository.updateGoal(goal)
        }
    }

    fun deleteSavingsGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch {
            savingsGoalRepository.deleteGoal(goal)
        }
    }
}
