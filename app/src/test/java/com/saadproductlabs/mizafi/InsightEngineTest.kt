package com.saadproductlabs.mizafi

import com.saadproductlabs.mizafi.data.entity.TransactionEntity
import com.saadproductlabs.mizafi.data.entity.TransactionType
import com.saadproductlabs.mizafi.domain.InsightEngine
import com.saadproductlabs.mizafi.domain.InsightTone
import com.saadproductlabs.mizafi.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class InsightEngineTest {
    @Test
    fun generate_emptyTransactions_returnsStarterInsight() {
        val insights = InsightEngine.generate(emptyList())
        assertEquals(1, insights.size)
        assertTrue(insights.first().message.contains("first transaction"))
    }

    @Test
    fun generate_foodWeekIncrease_includesWarning() {
        val thisWeek = DateUtils.weekRangeMillis(0)
        val lastWeek = DateUtils.weekRangeMillis(1)
        val midThisWeek = thisWeek.first + (thisWeek.last - thisWeek.first) / 2
        val midLastWeek = lastWeek.first + (lastWeek.last - lastWeek.first) / 2

        val transactions = listOf(
            TransactionEntity(
                amount = 50.0,
                category = "Food",
                date = midLastWeek,
                type = TransactionType.EXPENSE
            ),
            TransactionEntity(
                amount = 100.0,
                category = "Food",
                date = midThisWeek,
                type = TransactionType.EXPENSE
            )
        )

        val messages = InsightEngine.generate(transactions).map { it.message }
        assertTrue(messages.any { it.contains("Food spending increased") })
    }

    @Test
    fun generate_savingsRateImprovement_isPositiveTone() {
        val thisMonth = DateUtils.currentMonthRangeMillis()
        val lastMonth = DateUtils.previousMonthRangeMillis()
        val thisDate = thisMonth.first + 1
        val lastDate = lastMonth.first + 1

        val transactions = listOf(
            TransactionEntity(
                amount = 1000.0,
                category = "Salary",
                date = lastDate,
                type = TransactionType.INCOME
            ),
            TransactionEntity(
                amount = 900.0,
                category = "Food",
                date = lastDate,
                type = TransactionType.EXPENSE
            ),
            TransactionEntity(
                amount = 2000.0,
                category = "Salary",
                date = thisDate,
                type = TransactionType.INCOME
            ),
            TransactionEntity(
                amount = 500.0,
                category = "Food",
                date = thisDate,
                type = TransactionType.EXPENSE
            )
        )

        val insight = InsightEngine.generate(transactions)
            .firstOrNull { it.message.contains("savings rate improved") }
        assertTrue(insight != null)
        assertEquals(InsightTone.POSITIVE, insight?.tone)
    }
}
