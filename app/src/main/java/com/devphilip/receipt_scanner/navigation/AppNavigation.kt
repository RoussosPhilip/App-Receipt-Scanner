package com.devphilip.receipt_scanner.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.devphilip.receipt_scanner.ui.screens.DashboardScreen
import com.devphilip.receipt_scanner.ui.screens.HistoryScreen
import com.devphilip.receipt_scanner.ui.screens.ManualEntryScreen
import com.devphilip.receipt_scanner.ui.screens.RecordDetailScreen
import com.devphilip.receipt_scanner.ui.screens.ScannerScreen
import com.devphilip.receipt_scanner.ui.screens.FavoritesStationScreen
import com.devphilip.receipt_scanner.ui.screens.MapScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "dashboard",
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None }
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


        composable("favorites_station") {
            FavoritesStationScreen(
                onNavigateBack = {navController.popBackStack() }
            )
        }

        composable("map") {
            MapScreen(
                onNavigateBack = { navController.popBackStack() },
                onStationSelected = { station, fuelType ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("selected_station", station.brand)
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("selected_fuel", fuelType)
                    navController.popBackStack()
                }
            )
        }

        composable("manual_entry") {
            val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle

            val selectedStation = savedStateHandle?.get<String>("selected_station")
            val selectedFuel = savedStateHandle?.get<String>("selected_fuel")

            ManualEntryScreen(
                onNavigateBack = { navController.popBackStack() },
                selectedStationName = selectedStation,
                selectedFuelType = selectedFuel,
                onSaveReceipt = { stationName, date, liters, amount ->
                    println("Saved: $stationName, $date, $liters, $amount")
                    navController.popBackStack()
                },
                onNavigateToFavoritesStation = {
                    navController.navigate("favorites_station")
                },
                onNavigateToMap = {
                    navController.navigate("map")
                }
            )

            LaunchedEffect(selectedStation, selectedFuel) {
                if (selectedStation != null) {
                    savedStateHandle?.remove<String>("selected_station")
                    savedStateHandle?.remove<String>("selected_fuel")
                }
            }
        }

    }
}