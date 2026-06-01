package com.example.flowfi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.flowfi.data.repository.SavingsGoalRepository
import com.example.flowfi.data.repository.TransactionRepository

class TransactionViewModelFactory(
    private val transactionRepository: TransactionRepository,
    private val savingsGoalRepository: SavingsGoalRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionViewModel(transactionRepository, savingsGoalRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
