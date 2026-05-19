package com.example.flowfi.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.flowfi.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

/** Room data access for [TransactionEntity] records. */
@Dao
interface TransactionDao {
  @Query("SELECT * FROM transactions ORDER BY date DESC")
  fun getAllTransactions(): Flow<List<TransactionEntity>>

  @Query("SELECT * FROM transactions WHERE id = :id")
  suspend fun getTransactionById(id: String): TransactionEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTransaction(transaction: TransactionEntity)

  @Update suspend fun updateTransaction(transaction: TransactionEntity)

  @Delete suspend fun deleteTransaction(transaction: TransactionEntity)

  @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME'")
  fun getTotalIncome(): Flow<Double?>

  @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE'")
  fun getTotalExpenses(): Flow<Double?>
}
