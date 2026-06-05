package com.saadproductlabs.mizafi.ui.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val currencyFormat: NumberFormat
    get() = NumberFormat.getCurrencyInstance(Locale.getDefault())

fun formatCurrency(amount: Double): String = currencyFormat.format(amount)

fun currencyPrefix(): String {
    val formatted = currencyFormat.format(0)
    return formatted.takeWhile { !it.isDigit() }.ifEmpty { "$" }
}

fun formatTransactionDate(timestamp: Long): String {
    return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
}

fun formatCurrentMonthYear(): String {
    return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
}

fun formatMonthYear(year: Int, month: Int): String {
    val calendar = Calendar.getInstance().apply {
        clear()
        set(year, month, 1, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
}

fun formatAmountForInput(amount: Double): String {
    val raw = amount.toString()
    return if (raw.endsWith(".0")) raw.dropLast(2) else raw
}

fun isValidAmountInput(input: String): Boolean {
    if (input.isEmpty()) return true
    if (!Regex("""^\d*\.?\d{0,2}$""").matches(input)) return false
    val value = input.toDoubleOrNull() ?: return false
    return value >= 0
}
