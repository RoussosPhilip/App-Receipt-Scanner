package com.example.receipt_scanner.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.receipt_scanner.ui.screens.DashboardScreen
import com.example.receipt_scanner.ui.screens.ManualEntryScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "Dashboard"
    ){
        composable("dashboard") {
            DashboardScreen(
                onNavigateToManualEntry = {
                    navController.navigate("manual_entry")
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
    }
}