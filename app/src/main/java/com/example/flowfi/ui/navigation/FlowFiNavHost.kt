package com.example.flowfi.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.flowfi.ui.screens.DashboardScreen
import com.example.flowfi.ui.screens.TransactionEntryScreen
import com.example.flowfi.ui.screens.TransactionListScreen
import com.example.flowfi.viewmodel.TransactionViewModel

@Composable
fun FlowFiNavHost(
    viewModel: TransactionViewModel,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToEntry = { navController.navigate(Screen.TransactionEntry.route) },
                onNavigateToList = { navController.navigate(Screen.TransactionList.route) }
            )
        }
        composable(Screen.TransactionEntry.route) {
            TransactionEntryScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.TransactionList.route) {
            TransactionListScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
