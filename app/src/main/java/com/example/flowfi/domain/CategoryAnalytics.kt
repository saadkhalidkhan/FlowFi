package com.example.flowfi.domain

import com.example.flowfi.data.entity.TransactionEntity
import com.example.flowfi.data.entity.TransactionType
import com.example.flowfi.util.DateUtils

data class CategorySpend(
    val category: String,
    val amount: Double,
    val percent: Float
)

object CategoryAnalytics {
    fun monthlyExpenseBreakdown(transactions: List<TransactionEntity>): List<CategorySpend> {
        val monthRange = DateUtils.currentMonthRangeMillis()
        val expenses = transactions.filter {
            it.type == TransactionType.EXPENSE && it.date in monthRange
        }
        if (expenses.isEmpty()) return emptyList()

        val total = expenses.sumOf { it.amount }
        if (total <= 0) return emptyList()

        return expenses
            .groupBy { it.category }
            .map { (category, items) ->
                val amount = items.sumOf { it.amount }
                CategorySpend(
                    category = category,
                    amount = amount,
                    percent = (amount / total * 100).toFloat()
                )
            }
            .sortedByDescending { it.amount }
    }
}
