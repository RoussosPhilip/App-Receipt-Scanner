package com.example.receipt_scanner.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.receipt_scanner.data.FuelRecord
import com.example.receipt_scanner.ui.screens.DashboardScreen
import com.example.receipt_scanner.ui.screens.HistoryScreen
import com.example.receipt_scanner.ui.screens.ManualEntryScreen

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

                onNavigateToHistory = {
                    navController.navigate("history")
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

        composable("history") {
            // temporary data for testing
            val records = listOf(
                FuelRecord("Shell - Kifisias", "27 Nov", 20.0, 39.5),
                FuelRecord("BP - Attica Road", "23 Nov", 35.0, 70.0),
                FuelRecord("Eko - Piraeus", "18 Nov", 15.0, 28.2),
                FuelRecord("Elin - Kifisias", "17 Nov", 20.0, 39.5),
                FuelRecord("Eko - Piraeus", "12 Nov", 15.0, 28.2),
                FuelRecord("BP - Attica Road", "4 Nov", 35.0, 70.0)

            )

            HistoryScreen(
                records = records,
                onNavigateBack = {navController.popBackStack()}
            )
        }
    }
}