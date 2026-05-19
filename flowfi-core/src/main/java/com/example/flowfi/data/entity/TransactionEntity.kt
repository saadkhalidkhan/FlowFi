package com.example.flowfi.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Local persistence model for a single income or expense transaction.
 */
enum class TransactionType {
  INCOME,
  EXPENSE,
}

@Entity(tableName = "transactions")
data class TransactionEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val amount: Double,
  val category: String,
  val date: Long,
  val type: TransactionType,
  val note: String = "",
)
