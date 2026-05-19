package com.example.flowfi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowfi.data.entity.TransactionEntity
import com.example.flowfi.data.entity.TransactionType
import com.example.flowfi.data.repository.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TransactionUiState(
    val transactions: List<TransactionEntity> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val balance: Double = 0.0,
    val insights: List<String> = emptyList()
)

class TransactionViewModel(private val repository: TransactionRepository) : ViewModel() {

    val uiState: StateFlow<TransactionUiState> = repository.getAllTransactions()
        .map { transactions ->
            val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val expenses = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            TransactionUiState(
                transactions = transactions,
                totalIncome = income,
                totalExpenses = expenses,
                balance = income - expenses,
                insights = generateInsights(transactions, income, expenses)
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

    private fun generateInsights(
        transactions: List<TransactionEntity>,
        income: Double,
        expenses: Double
    ): List<String> {
        val insights = mutableListOf<String>()

        if (transactions.isEmpty()) {
            return listOf("No transactions yet. Start tracking to see insights!")
        }

        if (income == 0.0) {
            insights.add("No income recorded yet. Try to add your earnings!")
        }

        if (expenses > income && income > 0) {
            insights.add("Warning: Your expenses are higher than your income this month.")
        }

        val savings = income - expenses
        if (income > 0 && (savings / income) < 0.10) {
            insights.add("Tip: Your savings are below 10% of your income. Try to save more!")
        }
        
        if (income > 0 && (savings / income) > 0.20) {
            insights.add("Great job! You've saved more than 20% of your income.")
        }

        val foodExpenses = transactions
            .filter { it.type == TransactionType.EXPENSE && it.category.equals("Food", ignoreCase = true) }
            .sumOf { it.amount }
        
        if (expenses > 0 && (foodExpenses / expenses) > 0.30) {
            insights.add("Alert: Food spending is over 30% of your total expenses.")
        }

        return insights.take(3)
    }
}
