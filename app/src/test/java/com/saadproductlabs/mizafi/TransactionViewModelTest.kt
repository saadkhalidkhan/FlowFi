package com.saadproductlabs.mizafi

import com.saadproductlabs.mizafi.data.entity.TransactionEntity
import com.saadproductlabs.mizafi.data.entity.TransactionType
import com.saadproductlabs.mizafi.util.DateUtils
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
    fun formatAmountForInput_stripsTrailingZero() {
        assertEquals("42", com.saadproductlabs.mizafi.ui.util.formatAmountForInput(42.0))
        assertEquals("12.5", com.saadproductlabs.mizafi.ui.util.formatAmountForInput(12.5))
    }

    @Test
    fun transactionCategories_includesUnknownCategory() {
        val options = com.saadproductlabs.mizafi.ui.model.TransactionCategories.optionsFor(
            com.saadproductlabs.mizafi.data.entity.TransactionType.EXPENSE,
            "Custom"
        )
        assertEquals("Custom", options.first())
    }

    @Test
    fun formatCurrentMonthYear_isNotBlank() {
        assertTrue(com.saadproductlabs.mizafi.ui.util.formatCurrentMonthYear().isNotBlank())
    }

    @Test
    fun currencyPrefix_isNotBlank() {
        assertTrue(com.saadproductlabs.mizafi.ui.util.currencyPrefix().isNotBlank())
    }

    @Test
    fun validateAmountForSave_rejectsZeroAndInvalid() {
        assertEquals(12.5, com.saadproductlabs.mizafi.ui.util.validateAmountForSave("12.5")!!, 0.0)
        assertEquals(null, com.saadproductlabs.mizafi.ui.util.validateAmountForSave(""))
        assertEquals(null, com.saadproductlabs.mizafi.ui.util.validateAmountForSave("0"))
        assertEquals(null, com.saadproductlabs.mizafi.ui.util.validateAmountForSave("abc"))
    }

    @Test
    fun isValidAmountInput_rejectsInvalidPatterns() {
        assertTrue(com.saadproductlabs.mizafi.ui.util.isValidAmountInput("12.50"))
        assertTrue(com.saadproductlabs.mizafi.ui.util.isValidAmountInput(""))
        assertTrue(!com.saadproductlabs.mizafi.ui.util.isValidAmountInput("12.345"))
        assertTrue(!com.saadproductlabs.mizafi.ui.util.isValidAmountInput("abc"))
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
