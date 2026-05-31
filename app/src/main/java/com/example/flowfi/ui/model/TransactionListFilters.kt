package com.example.flowfi.ui.model

import com.example.flowfi.data.entity.TransactionEntity
import com.example.flowfi.ui.util.formatMonthYear
import java.util.Calendar

data class MonthFilterOption(
    val year: Int,
    val month: Int,
    val label: String
) {
    val key: String get() = "$year-$month"
}

object TransactionListFilters {
    const val ALL_MONTHS_LABEL = "All months"
    const val ALL_CATEGORIES_LABEL = "All categories"

    fun monthKeyFor(dateMillis: Long): String {
        val calendar = Calendar.getInstance().apply { timeInMillis = dateMillis }
        return "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}"
    }

    fun filter(
        transactions: List<TransactionEntity>,
        monthKey: String?,
        category: String?
    ): List<TransactionEntity> {
        return transactions.filter { transaction ->
            val matchesMonth = monthKey == null || monthKeyFor(transaction.date) == monthKey
            val matchesCategory = category == null || transaction.category == category
            matchesMonth && matchesCategory
        }
    }

    fun monthOptions(transactions: List<TransactionEntity>): List<MonthFilterOption> {
        return transactions
            .map { monthKeyFor(it.date) }
            .distinct()
            .map { key ->
                val parts = key.split("-")
                val year = parts[0].toInt()
                val month = parts[1].toInt()
                MonthFilterOption(year, month, formatMonthYear(year, month))
            }
            .sortedWith(compareByDescending<MonthFilterOption> { it.year }.thenByDescending { it.month })
    }

    fun categoryOptions(transactions: List<TransactionEntity>): List<String> =
        transactions.map { it.category }.distinct().sorted()
}
