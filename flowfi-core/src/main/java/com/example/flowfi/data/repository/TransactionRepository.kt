package com.example.flowfi.data.repository

import com.example.flowfi.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

/** Repository contract for transaction persistence. */
interface TransactionRepository {
  fun getAllTransactions(): Flow<List<TransactionEntity>>

  suspend fun getTransactionById(id: String): TransactionEntity?

  suspend fun insertTransaction(transaction: TransactionEntity)

  suspend fun updateTransaction(transaction: TransactionEntity)

  suspend fun deleteTransaction(transaction: TransactionEntity)

  fun getTotalIncome(): Flow<Double?>

  fun getTotalExpenses(): Flow<Double?>
}

/** Default [TransactionRepository] backed by Room. */
class TransactionRepositoryImpl(private val transactionDao: com.example.flowfi.data.dao.TransactionDao) :
  TransactionRepository {
  override fun getAllTransactions(): Flow<List<TransactionEntity>> =
    transactionDao.getAllTransactions()

  override suspend fun getTransactionById(id: String): TransactionEntity? =
    transactionDao.getTransactionById(id)

  override suspend fun insertTransaction(transaction: TransactionEntity) {
    transactionDao.insertTransaction(transaction)
  }

  override suspend fun updateTransaction(transaction: TransactionEntity) {
    transactionDao.updateTransaction(transaction)
  }

  override suspend fun deleteTransaction(transaction: TransactionEntity) {
    transactionDao.deleteTransaction(transaction)
  }

  override fun getTotalIncome(): Flow<Double?> = transactionDao.getTotalIncome()

  override fun getTotalExpenses(): Flow<Double?> = transactionDao.getTotalExpenses()
}
