package com.saadproductlabs.mizafi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.saadproductlabs.mizafi.data.repository.SavingsGoalRepository
import com.saadproductlabs.mizafi.data.repository.TransactionRepository

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
