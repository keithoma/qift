package com.example.qift.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
// If you add the material-icons-extended dependency, you can use these:
// import androidx.compose.material.icons.filled.AddCard
// import androidx.compose.material.icons.filled.QrCodeScanner
// import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.qift.auth.LoginScreen
import com.example.qift.issue.IssueCardScreen
import com.example.qift.scan.ScanRedeemScreen
import com.example.qift.admin.AdminDashboardScreen
import com.google.firebase.auth.FirebaseAuth

sealed class AppRoute(val route: String) {
    data object Login : AppRoute("login")
    data object IssueCard : AppRoute("issue_card")
    data object ScanRedeem : AppRoute("scan_redeem")
    data object AdminDashboard : AppRoute("admin_dashboard")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = AppRoute.Login.route
) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    
    // If user is not logged in and not on login screen, navigate to login
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    if (currentUser == null && currentRoute != AppRoute.Login.route) {
        navController.navigate(AppRoute.Login.route) {
            popUpTo(AppRoute.Login.route) { inclusive = true }
        }
    }

    if (currentRoute == AppRoute.Login.route) {
        LoginScreen(
            onLoginSuccess = {
                navController.navigate(AppRoute.IssueCard.route) {
                    popUpTo(AppRoute.Login.route) { inclusive = true }
                }
            }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Qift") },
                    actions = {
                        IconButton(onClick = {
                            auth.signOut()
                            navController.navigate(AppRoute.Login.route) {
                                popUpTo(AppRoute.Login.route) { inclusive = true }
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                        }
                    }
                )
            },
            bottomBar = {
                val items = listOf(
                    AppRoute.IssueCard.route to "Issue",
                    AppRoute.ScanRedeem.route to "Scan",
                    AppRoute.AdminDashboard.route to "Admin"
                )
                
                // Note: Since `material-icons-extended` is not present, we use basic fallback icons.
                val icons = listOf(
                    Icons.Filled.Add, 
                    Icons.Filled.Search,
                    Icons.Filled.Settings
                )

                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    items.forEachIndexed { index, pair ->
                        NavigationBarItem(
                            icon = { Icon(icons[index], contentDescription = pair.second) },
                            label = { Text(pair.second) },
                            selected = currentRoute == pair.first,
                            onClick = {
                                navController.navigate(pair.first) {
                                    // Pop up to the start destination of the graph to
                                    // avoid building up a large stack of destinations
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(AppRoute.IssueCard.route) { IssueCardScreen() }
                composable(AppRoute.ScanRedeem.route) { ScanRedeemScreen() }
                composable(AppRoute.AdminDashboard.route) { AdminDashboardScreen() }
            }
        }
    }
}
