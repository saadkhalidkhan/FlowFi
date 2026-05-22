package com.example.flowfi

import com.example.flowfi.data.entity.TransactionEntity
import com.example.flowfi.data.entity.TransactionType
import com.example.flowfi.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionViewModelTest {
    @Test
    fun currentMonthRange_containsToday() {
        val range = DateUtils.currentMonthRangeMillis()
        val now = System.currentTimeMillis()
        assertTrue(now in range)
    }

    @Test
    fun currencyPrefix_isNotBlank() {
        assertTrue(com.example.flowfi.ui.util.currencyPrefix().isNotBlank())
    }

    @Test
    fun isValidAmountInput_rejectsInvalidPatterns() {
        assertTrue(com.example.flowfi.ui.util.isValidAmountInput("12.50"))
        assertTrue(com.example.flowfi.ui.util.isValidAmountInput(""))
        assertTrue(!com.example.flowfi.ui.util.isValidAmountInput("12.345"))
        assertTrue(!com.example.flowfi.ui.util.isValidAmountInput("abc"))
    }

    @Test
    fun monthlyFilter_excludesOtherMonths() {
        val range = DateUtils.currentMonthRangeMillis()
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.MONTH, -2)
        val oldDate = cal.timeInMillis

        val old = TransactionEntity(
            amount = 100.0,
            category = "Food",
            date = oldDate,
            type = TransactionType.EXPENSE
        )
        val current = TransactionEntity(
            amount = 50.0,
            category = "Food",
            date = System.currentTimeMillis(),
            type = TransactionType.EXPENSE
        )

        val monthly = listOf(old, current).filter { it.date in range }
        assertEquals(1, monthly.size)
        assertEquals(current.id, monthly.first().id)
    }
}
