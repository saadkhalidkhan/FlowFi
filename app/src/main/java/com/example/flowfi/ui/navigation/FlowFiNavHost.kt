package com.example.flowfi.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.compose.rememberNavController
import com.example.flowfi.ui.screens.DashboardScreen
import com.example.flowfi.ui.screens.InsightsScreen
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
                onNavigateToList = { navController.navigate(Screen.TransactionList.route) },
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.editTransaction(id))
                },
                onNavigateToInsights = { navController.navigate(Screen.Insights.route) }
            )
        }
        composable(Screen.Insights.route) {
            InsightsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.TransactionEntry.route) {
            TransactionEntryScreen(
                viewModel = viewModel,
                transactionId = null,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.EDIT_ROUTE,
            arguments = listOf(
                navArgument("transactionId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId")
            TransactionEntryScreen(
                viewModel = viewModel,
                transactionId = transactionId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.TransactionList.route) {
            TransactionListScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.editTransaction(id))
                }
            )
        }
    }
}
