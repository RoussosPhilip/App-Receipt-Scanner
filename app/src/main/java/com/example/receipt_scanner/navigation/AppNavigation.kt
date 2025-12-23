package com.example.receipt_scanner.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.receipt_scanner.ui.screens.DashboardScreen
import com.example.receipt_scanner.ui.screens.HistoryScreen
import com.example.receipt_scanner.ui.screens.ManualEntryScreen
import com.example.receipt_scanner.ui.screens.RecordDetailScreen
import com.example.receipt_scanner.ui.screens.ScannerScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "dashboard"
    ){
        composable("dashboard") {
            DashboardScreen(
                onNavigateToManualEntry = {
                    navController.navigate("manual_entry")
                },

                onNavigateToScanner = {
                    navController.navigate("scanner")
                },

                onNavigateToHistory = {
                    navController.navigate("history")
                },

                onNavigateToRecordDetail = { recordId ->
                    navController.navigate("recordDetail/$recordId")
                }
            )
        }

        composable("manual_entry"){
            ManualEntryScreen(
                onNavigateBack = { navController.popBackStack() },

                onSaveReceipt = { stationName, date, liters, amount ->
                    println("Saved: $stationName, $date, $liters, $amount")
                    navController.popBackStack()
                }
            )
        }

        composable("scanner") {
            ScannerScreen(
                onNavigateBack = { navController.popBackStack() },
                onScanComplete = { uri ->
                    println("Scanned image: $uri")
                    navController.popBackStack()
                }
            )
        }

        composable("history") {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRecordDetail = { recordId ->
                    navController.navigate("recordDetail/$recordId")
                }
            )
        }


        composable(
            route = "recordDetail/{recordId}",
            arguments = listOf(navArgument("recordId") { type = NavType.IntType })
        ) { backStackEntry ->
            val recordId = backStackEntry.arguments?.getInt("recordId") ?: 0
            RecordDetailScreen(
                recordId = recordId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

    }
}