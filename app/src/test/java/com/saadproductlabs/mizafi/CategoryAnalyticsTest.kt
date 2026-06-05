package com.saadproductlabs.mizafi

import com.saadproductlabs.mizafi.data.entity.TransactionEntity
import com.saadproductlabs.mizafi.data.entity.TransactionType
import com.saadproductlabs.mizafi.domain.CategoryAnalytics
import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryAnalyticsTest {
    @Test
    fun monthlyExpenseBreakdown_calculatesPercentages() {
        val now = System.currentTimeMillis()
        val transactions = listOf(
            TransactionEntity(
                amount = 75.0,
                category = "Food",
                date = now,
                type = TransactionType.EXPENSE
            ),
            TransactionEntity(
                amount = 25.0,
                category = "Shopping",
                date = now,
                type = TransactionType.EXPENSE
            )
        )

        val breakdown = CategoryAnalytics.monthlyExpenseBreakdown(transactions)
        assertEquals(2, breakdown.size)
        assertEquals("Food", breakdown.first().category)
        assertEquals(75f, breakdown.first().percent, 0.1f)
    }
}
