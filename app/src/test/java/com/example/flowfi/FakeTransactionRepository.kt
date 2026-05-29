package com.example.flowfi

import com.example.flowfi.data.entity.TransactionEntity
import com.example.flowfi.data.entity.TransactionType
import com.example.flowfi.data.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeTransactionRepository : TransactionRepository {
    private val transactions = mutableListOf<TransactionEntity>()
    private val flow = MutableStateFlow<List<TransactionEntity>>(emptyList())

    private fun publish() {
        flow.value = transactions.sortedByDescending { it.date }
    }

    override fun getAllTransactions(): Flow<List<TransactionEntity>> = flow

    override suspend fun getTransactionById(id: String): TransactionEntity? =
        transactions.find { it.id == id }

    override suspend fun insertTransaction(transaction: TransactionEntity) {
        transactions.removeAll { it.id == transaction.id }
        transactions.add(transaction)
        publish()
    }

    override suspend fun updateTransaction(transaction: TransactionEntity) {
        insertTransaction(transaction)
    }

    override suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactions.removeAll { it.id == transaction.id }
        publish()
    }

    override fun getTotalIncome(): Flow<Double?> =
        flow.map { list ->
            list.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }.takeIf { list.isNotEmpty() }
        }

    override fun getTotalExpenses(): Flow<Double?> =
        flow.map { list ->
            list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }.takeIf { list.isNotEmpty() }
        }
}
