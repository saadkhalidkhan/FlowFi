package com.example.flowfi

import com.example.flowfi.data.entity.TransactionEntity
import com.example.flowfi.data.entity.TransactionType
import com.example.flowfi.ui.model.TransactionListFilters
import com.example.flowfi.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class TransactionListFiltersTest {
    @Test
    fun filter_byMonth_returnsOnlyMatchingTransactions() {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -1)
        val lastMonth = cal.timeInMillis
        val monthKey = TransactionListFilters.monthKeyFor(lastMonth)

        val transactions = listOf(
            TransactionEntity(
                amount = 10.0,
                category = "Food",
                date = lastMonth,
                type = TransactionType.EXPENSE
            ),
            TransactionEntity(
                amount = 20.0,
                category = "Food",
                date = System.currentTimeMillis(),
                type = TransactionType.EXPENSE
            )
        )

        val filtered = TransactionListFilters.filter(transactions, monthKey, null)
        assertEquals(1, filtered.size)
        assertEquals(10.0, filtered.first().amount, 0.0)
    }

    @Test
    fun filter_byCategory_returnsOnlyMatchingTransactions() {
        val transactions = listOf(
            TransactionEntity(
                amount = 10.0,
                category = "Food",
                date = System.currentTimeMillis(),
                type = TransactionType.EXPENSE
            ),
            TransactionEntity(
                amount = 50.0,
                category = "Salary",
                date = System.currentTimeMillis(),
                type = TransactionType.INCOME
            )
        )

        val filtered = TransactionListFilters.filter(transactions, null, "Food")
        assertEquals(1, filtered.size)
        assertEquals("Food", filtered.first().category)
    }

    @Test
    fun filter_byMonthAndCategory_appliesBothFilters() {
        val cal = Calendar.getInstance()
        val monthKey = TransactionListFilters.monthKeyFor(cal.timeInMillis)

        val transactions = listOf(
            TransactionEntity(
                amount = 10.0,
                category = "Food",
                date = cal.timeInMillis,
                type = TransactionType.EXPENSE
            ),
            TransactionEntity(
                amount = 15.0,
                category = "Transport",
                date = cal.timeInMillis,
                type = TransactionType.EXPENSE
            )
        )

        val filtered = TransactionListFilters.filter(transactions, monthKey, "Food")
        assertEquals(1, filtered.size)
        assertEquals("Food", filtered.first().category)
    }

    @Test
    fun monthOptions_areSortedNewestFirst() {
        val cal = Calendar.getInstance()
        val current = cal.timeInMillis
        cal.add(Calendar.MONTH, -2)
        val older = cal.timeInMillis

        val options = TransactionListFilters.monthOptions(
            listOf(
                TransactionEntity(
                    amount = 1.0,
                    category = "Food",
                    date = older,
                    type = TransactionType.EXPENSE
                ),
                TransactionEntity(
                    amount = 2.0,
                    category = "Food",
                    date = current,
                    type = TransactionType.EXPENSE
                )
            )
        )

        assertEquals(2, options.size)
        assertTrue(options.first().year > options.last().year || options.first().month > options.last().month)
    }

    @Test
    fun monthRangeMillis_containsTransactionInSameMonth() {
        val cal = Calendar.getInstance()
        val range = DateUtils.monthRangeMillis(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH)
        )
        assertTrue(cal.timeInMillis in range)
    }
}
