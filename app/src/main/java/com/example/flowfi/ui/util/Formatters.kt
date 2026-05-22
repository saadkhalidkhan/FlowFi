package com.example.flowfi.ui.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatCurrency(amount: Double): String {
    return NumberFormat.getCurrencyInstance(Locale.getDefault()).format(amount)
}

fun formatTransactionDate(timestamp: Long): String {
    return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
}

fun isValidAmountInput(input: String): Boolean {
    if (input.isEmpty()) return true
    if (!Regex("""^\d*\.?\d{0,2}$""").matches(input)) return false
    val value = input.toDoubleOrNull() ?: return false
    return value >= 0
}
