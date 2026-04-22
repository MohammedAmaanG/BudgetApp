package com.prog7313.budgetapp.ui.navigation


import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.prog7313.budgetapp.data.model.Screen
import com.prog7313.budgetapp.ui.screens.analyze.AnalyzeScreen
import com.prog7313.budgetapp.ui.screens.auth.LoginScreen
import com.prog7313.budgetapp.ui.screens.auth.RegisterScreen
import com.prog7313.budgetapp.ui.screens.budget.BudgetScreen
import com.prog7313.budgetapp.ui.screens.goals.SavingsGoalsScreen
import com.prog7313.budgetapp.ui.screens.overview.OverviewScreen
import com.prog7313.budgetapp.ui.screens.subscriptions.SubscriptionsScreen
import com.prog7313.budgetapp.ui.screens.transactions.AddExpenseScreen
import com.prog7313.budgetapp.ui.screens.transactions.TransactionsScreen
import com.prog7313.budgetapp.viewmodel.AppViewModel
import com.prog7313.budgetapp.viewmodel.AuthViewModel


data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

val bottomNavItems = listOf(
    BottomNavItem("Overview",     Icons.Default.Home,          Screen.Overview.route),
    BottomNavItem("Budget",       Icons.Default.AccountBalance, Screen.Budget.route),
    BottomNavItem("Transactions", Icons.Default.Receipt,        Screen.Transactions.route),
    BottomNavItem("Analyze",      Icons.Default.BarChart,       Screen.Analyze.route),
    BottomNavItem("Goals",        Icons.Default.Star,           Screen.Goals.route)
)

val routesWithBottomBar = bottomNavItems.map { it.route }

@Composable
fun AppNavHost(
    authViewModel: AuthViewModel,
    appViewModel: AppViewModel,
    isLoggedIn: Boolean
) {
    val navController = rememberNavController()
    val navBackStack  by navController.currentBackStackEntryAsState()
    val currentRoute  = navBackStack?.destination?.route
    val showBottomBar = currentRoute in routesWithBottomBar

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    val currentDest = navBackStack?.destination
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon     = { Icon(item.icon, contentDescription = item.label) },
                            label    = { Text(item.label) },
                            selected = currentDest?.hierarchy?.any { it.route == item.route } == true,
                            onClick  = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = if (isLoggedIn) Screen.Overview.route else Screen.Login.route,
            modifier         = Modifier.padding(innerPadding)
        ) {
             composable(Screen.Login.route) {
                LoginScreen(
                    viewModel  = authViewModel,
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onLoginSuccess = {
                        navController.navigate(Screen.Overview.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    viewModel  = authViewModel,
                    onNavigateToLogin = { navController.popBackStack() },
                    onRegisterSuccess = {
                        navController.navigate(Screen.Overview.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

             composable(Screen.Overview.route) {
                OverviewScreen(
                    viewModel  = appViewModel,
                    onLogout   = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToSubscriptions = { navController.navigate(Screen.Subscriptions.route) }
                )
            }
            composable(Screen.Budget.route) {
                BudgetScreen(viewModel = appViewModel)
            }
            composable(Screen.Transactions.route) {
                TransactionsScreen(
                    viewModel = appViewModel,
                    onAddExpense = { navController.navigate(Screen.AddExpense.route) }
                )
            }
            composable(Screen.Analyze.route) {
                AnalyzeScreen(viewModel = appViewModel)
            }
            composable(Screen.Goals.route) {
                SavingsGoalsScreen(viewModel = appViewModel)
            }

           composable(Screen.AddExpense.route) {
                AddExpenseScreen(
                    viewModel = appViewModel,
                    onBack    = { navController.popBackStack() }
                )
            }
            composable(Screen.Subscriptions.route) {
                SubscriptionsScreen(
                    viewModel = appViewModel,
                    onBack    = { navController.popBackStack() }
                )
            }
        }
    }
}

