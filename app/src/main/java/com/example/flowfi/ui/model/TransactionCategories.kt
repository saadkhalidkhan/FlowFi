package com.example.flowfi.ui.model

import com.example.flowfi.data.entity.TransactionType

object TransactionCategories {
    val expense = listOf("Food", "Transport", "Bills", "Shopping", "Other")
    val income = listOf("Salary", "Other")

    fun forType(type: TransactionType): List<String> =
        if (type == TransactionType.EXPENSE) expense else income

    fun optionsFor(type: TransactionType, current: String): List<String> {
        val base = forType(type)
        return if (current in base) base else listOf(current) + base
    }
}
