package com.devphilip.receipt_scanner.navigation

import android.net.Uri
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.devphilip.receipt_scanner.ui.screens.*

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "dashboard",
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None }
    ) {
        composable("dashboard") {
            DashboardScreen(
                onNavigateToManualEntry = { navController.navigate("manual_entry") },
                onNavigateToScanner = { navController.navigate("scanner") },
                onNavigateToHistory = { navController.navigate("history") },
                onNavigateToStatistics = { navController.navigate("statistics") },
                onNavigateToRecordDetail = { recordId ->
                    navController.navigate("recordDetail/$recordId")
                }
            )
        }

        composable("scanner") {
            ScannerScreen(
                onNavigateBack = { navController.popBackStack() },
                onScanComplete = { uri ->
                    navController.navigate("manual_entry") {
                        popUpTo("scanner") { inclusive = true }
                    }
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("scanned_uri", uri.toString())
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

        composable("statistics") {
            StatisticsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "recordDetail/{recordId}",
            arguments = listOf(navArgument("recordId") { type = NavType.IntType })
        ) { backStackEntry ->
            val recordId = backStackEntry.arguments?.getInt("recordId") ?: 0
            RecordDetailScreen(
                recordId = recordId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate("manual_entry?editId=$id")
                }
            )
        }

        composable("favorites_station") {
            FavoritesStationScreen(
                onNavigateBack = { navController.popBackStack() },
                onStationSelected = { station, fuelType ->
                    navController.previousBackStackEntry?.savedStateHandle?.apply {
                        set("selected_station",       station.owner)
                        set("selected_station_brand", station.brand)
                        set("selected_fuel",          fuelType)
                        set("selected_unit_price",    "")
                    }
                    navController.popBackStack()
                }
            )
        }

        composable("map") {
            MapScreen(
                onNavigateBack = { navController.popBackStack() },
                onStationSelected = { station, fuelTypeString ->
                    val parts     = fuelTypeString.split(" - ")
                    val fuelName  = parts.getOrElse(0) { fuelTypeString }
                    val fuelPrice = parts.getOrElse(1) { "" }
                        .replace(" €", "")
                        .trim()

                    navController.previousBackStackEntry?.savedStateHandle?.apply {
                        set("selected_station",       station.owner)
                        set("selected_station_brand", station.brand)
                        set("selected_fuel",          fuelName)
                        set("selected_unit_price",    "")
                    }
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "manual_entry?editId={editId}",
            arguments = listOf(
                navArgument("editId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val editId = backStackEntry.arguments?.getInt("editId")
                ?.takeIf { it != -1 }

            val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle

            val selectedStation by savedStateHandle
                ?.getStateFlow("selected_station", "")
                ?.collectAsState() ?: remember { mutableStateOf("") }

            val selectedBrand by savedStateHandle
                ?.getStateFlow("selected_station_brand", "")
                ?.collectAsState() ?: remember { mutableStateOf("") }

            val selectedFuel by savedStateHandle
                ?.getStateFlow("selected_fuel", "")
                ?.collectAsState() ?: remember { mutableStateOf("") }

            val selectedUnitPrice by savedStateHandle
                ?.getStateFlow("selected_unit_price", "")
                ?.collectAsState() ?: remember { mutableStateOf("") }

            val scannedUri by savedStateHandle
                ?.getStateFlow("scanned_uri", "")
                ?.collectAsState() ?: remember { mutableStateOf("") }

            ManualEntryScreen(
                onNavigateBack              = { navController.popBackStack() },
                onSaveReceipt               = { _, _, _, _ -> navController.popBackStack() },
                onNavigateToFavoritesStation = { navController.navigate("favorites_station") },
                onNavigateToMap             = { navController.navigate("map") },
                selectedStationName         = selectedStation.ifBlank { null },
                selectedStationBrand        = selectedBrand.ifBlank { null },
                selectedFuelType            = selectedFuel.ifBlank { null },
                selectedUnitPrice           = selectedUnitPrice.ifBlank { null },
                scannedImageUri             = scannedUri.ifBlank { null },
                editRecordId                = editId
            )
        }
    }
}