package com.gradientgeeks.sfe.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gradientgeeks.sfe.ui.dashboard.DashboardScreen
import com.gradientgeeks.sfe.ui.login.LoginScreen
import com.gradientgeeks.sfe.ui.sendmoney.SendMoneyScreen
import com.gradientgeeks.sfe.ui.splash.SplashScreen
import com.gradientgeeks.sfe.ui.transactions.TransactionsScreen

/**
 * Main navigation routes for the app.
 */
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object SendMoney : Screen("send_money")
    object RequestMoney : Screen("request_money")
    object Transactions : Screen("transactions")
    object Profile : Screen("profile")
}

/**
 * Main navigation component for the app.
 */
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onSendMoneyClick = { navController.navigate(Screen.SendMoney.route) },
                onTransactionsClick = { navController.navigate(Screen.Transactions.route) }
            )
        }
        
        composable(Screen.SendMoney.route) {
            SendMoneyScreen(
                onBackClick = { navController.popBackStack() },
                onTransactionComplete = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Transactions.route) {
            TransactionsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
