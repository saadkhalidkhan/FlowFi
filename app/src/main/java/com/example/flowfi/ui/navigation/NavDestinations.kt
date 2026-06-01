package com.example.flowfi.ui.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object TransactionEntry : Screen("transaction_entry")
    object TransactionList : Screen("transaction_list")
    object Insights : Screen("insights")

    companion object {
        const val EDIT_ROUTE = "transaction_edit/{transactionId}"

        fun editTransaction(transactionId: String): String = "transaction_edit/$transactionId"
    }
}
