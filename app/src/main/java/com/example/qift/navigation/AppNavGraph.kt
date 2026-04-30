package com.example.qift.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

sealed class AppRoute(val route: String) {
    data object IssueCard : AppRoute("issue_card")
    data object ScanRedeem : AppRoute("scan_redeem")
    data object AdminDashboard : AppRoute("admin_dashboard")
}

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = AppRoute.IssueCard.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(AppRoute.IssueCard.route) { IssueCardScreen() }
        composable(AppRoute.ScanRedeem.route) { ScanRedeemScreen() }
        composable(AppRoute.AdminDashboard.route) { AdminDashboardScreen() }
    }
}

@Composable
fun IssueCardScreen() {
    ScreenPlaceholder(title = "Issue Card")
}

@Composable
fun ScanRedeemScreen() {
    ScreenPlaceholder(title = "Scan & Redeem")
}

@Composable
fun AdminDashboardScreen() {
    ScreenPlaceholder(title = "Admin Dashboard")
}

@Composable
private fun ScreenPlaceholder(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}