package com.saadproductlabs.mizafi.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.saadproductlabs.mizafi.ui.screens.DashboardScreen
import com.saadproductlabs.mizafi.ui.screens.InsightsScreen
import com.saadproductlabs.mizafi.ui.screens.TransactionEntryScreen
import com.saadproductlabs.mizafi.ui.screens.TransactionListScreen
import com.saadproductlabs.mizafi.viewmodel.TransactionViewModel
import com.saadproductlabs.mizafi.viewmodel.UiEvent

private const val TRANSITION_DURATION_MS = 280

private fun AnimatedContentTransitionScope<NavBackStackEntry>.forwardEnter() =
    slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Start,
        animationSpec = tween(TRANSITION_DURATION_MS)
    ) + fadeIn(tween(TRANSITION_DURATION_MS))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.forwardExit() =
    slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Start,
        animationSpec = tween(TRANSITION_DURATION_MS)
    ) + fadeOut(tween(TRANSITION_DURATION_MS))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.backwardEnter() =
    slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.End,
        animationSpec = tween(TRANSITION_DURATION_MS)
    ) + fadeIn(tween(TRANSITION_DURATION_MS))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.backwardExit() =
    slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.End,
        animationSpec = tween(TRANSITION_DURATION_MS)
    ) + fadeOut(tween(TRANSITION_DURATION_MS))

@Composable
fun MizafiNavHost(
    viewModel: TransactionViewModel,
    navController: NavHostController = rememberNavController()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel, context) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.Message -> snackbarHostState.showSnackbar(
                    message = context.getString(event.textRes),
                    actionLabel = event.actionLabelRes?.let(context::getString),
                    duration = SnackbarDuration.Short
                )
                is UiEvent.Error -> snackbarHostState.showSnackbar(
                    message = context.getString(event.textRes),
                    duration = SnackbarDuration.Long
                )
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier
        ) {
            composable(
                route = Screen.Dashboard.route,
                enterTransition = { forwardEnter() },
                exitTransition = { forwardExit() },
                popEnterTransition = { backwardEnter() },
                popExitTransition = { backwardExit() }
            ) {
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
            composable(
                route = Screen.Insights.route,
                enterTransition = { forwardEnter() },
                exitTransition = { forwardExit() },
                popEnterTransition = { backwardEnter() },
                popExitTransition = { backwardExit() }
            ) {
                InsightsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.TransactionEntry.route,
                enterTransition = { forwardEnter() },
                exitTransition = { forwardExit() },
                popEnterTransition = { backwardEnter() },
                popExitTransition = { backwardExit() }
            ) {
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
                ),
                enterTransition = { forwardEnter() },
                exitTransition = { forwardExit() },
                popEnterTransition = { backwardEnter() },
                popExitTransition = { backwardExit() }
            ) { backStackEntry ->
                val transactionId = backStackEntry.arguments?.getString("transactionId")
                TransactionEntryScreen(
                    viewModel = viewModel,
                    transactionId = transactionId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.TransactionList.route,
                enterTransition = { forwardEnter() },
                exitTransition = { forwardExit() },
                popEnterTransition = { backwardEnter() },
                popExitTransition = { backwardExit() }
            ) {
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
}
