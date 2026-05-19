package com.example.flowfi.ui.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object TransactionEntry : Screen("transaction_entry")
    object TransactionList : Screen("transaction_list")
}
