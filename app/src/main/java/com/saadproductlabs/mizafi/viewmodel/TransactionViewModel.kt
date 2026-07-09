package com.saadproductlabs.mizafi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saadproductlabs.mizafi.R
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
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
    val savingsGoals: List<SavingsGoalEntity> = emptyList(),
    val isDataReady: Boolean = false
)

class TransactionViewModel(
    private val repository: TransactionRepository,
    private val savingsGoalRepository: SavingsGoalRepository
) : ViewModel() {

    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

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
            savingsGoals = goals,
            isDataReady = true
        )
    }
        .onEach { /* first emission marks data ready via isDataReady */ }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TransactionUiState()
        )

    fun addTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            runCatching { repository.insertTransaction(transaction) }
                .onSuccess {
                    _events.emit(UiEvent.Message(R.string.msg_transaction_saved))
                }
                .onFailure {
                    _events.emit(UiEvent.Error(R.string.msg_save_error))
                }
        }
    }

    suspend fun saveTransaction(transaction: TransactionEntity, isUpdate: Boolean): Boolean {
        return runCatching {
            if (isUpdate) {
                repository.updateTransaction(transaction)
            } else {
                repository.insertTransaction(transaction)
            }
        }.fold(
            onSuccess = {
                _events.emit(
                    UiEvent.Message(
                        if (isUpdate) R.string.msg_changes_saved
                        else R.string.msg_transaction_saved
                    )
                )
                true
            },
            onFailure = {
                _events.emit(UiEvent.Error(R.string.msg_save_error))
                false
            }
        )
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            runCatching { repository.updateTransaction(transaction) }
                .onSuccess { _events.emit(UiEvent.Message(R.string.msg_changes_saved)) }
                .onFailure {
                    _events.emit(UiEvent.Error(R.string.msg_update_error))
                }
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun restoreTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            runCatching { repository.insertTransaction(transaction) }
                .onSuccess { _events.emit(UiEvent.Message(R.string.msg_transaction_restored)) }
                .onFailure {
                    _events.emit(UiEvent.Error(R.string.msg_restore_transaction_error))
                }
        }
    }

    fun addSavingsGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch {
            runCatching { savingsGoalRepository.insertGoal(goal) }
                .onSuccess { _events.emit(UiEvent.Message(R.string.msg_goal_created)) }
                .onFailure { _events.emit(UiEvent.Error(R.string.msg_create_goal_error)) }
        }
    }

    fun updateSavingsGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch {
            runCatching { savingsGoalRepository.updateGoal(goal) }
                .onSuccess { _events.emit(UiEvent.Message(R.string.msg_funds_added)) }
                .onFailure { _events.emit(UiEvent.Error(R.string.msg_update_goal_error)) }
        }
    }

    fun deleteSavingsGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch {
            savingsGoalRepository.deleteGoal(goal)
        }
    }

    fun restoreSavingsGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch {
            runCatching { savingsGoalRepository.insertGoal(goal) }
                .onSuccess { _events.emit(UiEvent.Message(R.string.msg_goal_restored)) }
                .onFailure { _events.emit(UiEvent.Error(R.string.msg_restore_goal_error)) }
        }
    }
}
