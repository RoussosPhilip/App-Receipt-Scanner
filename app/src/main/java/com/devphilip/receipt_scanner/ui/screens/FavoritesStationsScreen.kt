package com.devphilip.receipt_scanner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devphilip.receipt_scanner.data.AppDataBase
import com.devphilip.receipt_scanner.data.FavoriteStation
import com.devphilip.receipt_scanner.data.StationsDataBase
import com.devphilip.receipt_scanner.ui.components.ChevronLeft
import com.devphilip.receipt_scanner.ui.components.GasStationInfoBottomSheet
import kotlinx.coroutines.launch

@Composable
fun FavoritesStationScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    onStationSelected: (FavoriteStation, String) -> Unit = { _, _ -> }  // ← station + fuelType
) {
    val context      = LocalContext.current
    val dao          = remember { AppDataBase.getDataBase(context).favoriteStationDao() }
    val stationsDao  = remember { StationsDataBase.getDataBase(context).stationsDao() }
    val scope        = rememberCoroutineScope()
    val favorites    by dao.getAll().collectAsState(initial = emptyList())

    // Bottom sheet state
    var selectedStation  by remember { mutableStateOf<FavoriteStation?>(null) }
    var selectedFuelTypes by remember { mutableStateOf<List<String>>(emptyList()) }
    val isFavorite = true

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ChevronLeft(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(45.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Favorite Stations",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 25.sp),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                if (favorites.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                            Text(
                                text = "No favorite stations yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Tap the heart icon on any station",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(favorites, key = { it.stationGUID }) { station ->
                            FavoriteStationCard(
                                station  = station,
                                onSelect = {
                                    scope.launch {
                                        val fuels = stationsDao.getFuelsForStation(station.stationGUID)
                                        selectedFuelTypes = fuels
                                        selectedStation   = station
                                    }
                                },
                                onRemove = {
                                    scope.launch { dao.delete(station.stationGUID) }
                                }
                            )
                        }
                    }
                }
            }

            GasStationInfoBottomSheet(
                stationName      = selectedStation?.owner ?: "",
                fuelTypes        = selectedFuelTypes,
                showSheet        = selectedStation != null,
                isFavorite       = isFavorite,
                onToggleFavorite = {
                    selectedStation?.let { station ->
                        scope.launch {
                            dao.delete(station.stationGUID)
                            selectedStation = null
                        }
                    }
                },
                onDismiss        = { selectedStation = null },
                onFuelSelected   = { fuelType ->
                    selectedStation?.let { station ->
                        onStationSelected(station, fuelType)
                    }
                    selectedStation = null
                }
            )
        }
    }
}

@Composable
private fun FavoriteStationCard(
    station: FavoriteStation,
    onSelect: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = station.owner,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = station.brand,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (station.address.isNotBlank()) {
                    Text(
                        text = station.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Remove from favorites",
                    tint = Color.Red
                )
            }
        }
    }
}