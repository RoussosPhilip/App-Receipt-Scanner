package com.devphilip.receipt_scanner.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.devphilip.receipt_scanner.ui.components.ChevronLeft
import com.devphilip.receipt_scanner.ui.components.GasStationInfoBottomSheet
import com.devphilip.receipt_scanner.data.remote.GasStation
import com.devphilip.receipt_scanner.data.remote.fetchGasStations
import com.devphilip.receipt_scanner.data.remote.getGasStationLogo
import com.devphilip.receipt_scanner.data.remote.loadBitmapFromAssets
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import com.mapbox.maps.viewannotation.geometry
import kotlinx.coroutines.launch


@Composable
fun MapScreen(
    onNavigateBack: () -> Unit,
    onStationSelected: (GasStation, String) -> Unit
) {
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()
    val scope = rememberCoroutineScope()

    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(5.5)
            center(Point.fromLngLat(23.7275, 37.9838))
            pitch(0.0)
            bearing(0.0)
        }
    }

    var gasStations by remember { mutableStateOf<List<GasStation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedStation by remember { mutableStateOf<GasStation?>(null) }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    val mapStyle = remember(isDarkTheme) {
        if (isDarkTheme) Style.DARK else Style.LIGHT
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        if (hasLocationPermission) {
            isLoading = true
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            try {
                val cancellationToken = CancellationTokenSource()
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationToken.token
                ).addOnSuccessListener { location ->
                    location?.let {
                        mapViewportState.flyTo(
                            cameraOptions = CameraOptions.Builder()
                                .center(Point.fromLngLat(it.longitude, it.latitude))
                                .zoom(14.0)
                                .build()
                        )

                        scope.launch {
                            gasStations = fetchGasStations(it.latitude, it.longitude)
                            isLoading = false
                        }
                    } ?: run {
                        isLoading = false
                    }
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
                isLoading = false
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            MapboxMap(
                modifier = Modifier.fillMaxSize(),
                mapViewportState = mapViewportState,
                style = { MapStyle(style = mapStyle) },
                scaleBar = { },
                attribution = { },
                logo = {
                    Logo(
                        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
                        alignment = Alignment.BottomStart
                    )
                }
            ) {
                MapEffect(hasLocationPermission) { mapView ->
                    if (hasLocationPermission) {
                        mapView.location.apply {
                            enabled = true
                            pulsingEnabled = true
                        }
                    }
                }

                gasStations.forEach { station ->
                    val logoFileName = getGasStationLogo(station.brand)
                    val bitmap = remember(station.brand) {
                        loadBitmapFromAssets(context, logoFileName)
                    }

                    ViewAnnotation(
                        options = viewAnnotationOptions {
                            geometry(Point.fromLngLat(station.lon, station.lat))
                            allowOverlap(true)
                            allowOverlapWithPuck(true)
                        }
                    ) {
                        bitmap?.let { bmp ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = if (station == selectedStation)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else
                                            Color.White,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedStation = station },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = station.brand ?: "Gas Station",
                                    modifier = Modifier.size(25.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        } ?: run {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Color(0xFFFF5722), CircleShape)
                                    .clickable { selectedStation = station }
                            )
                        }
                    }
                }
            }

            // Bottom Sheet Component
            GasStationInfoBottomSheet(
                stationName = selectedStation?.brand ?: "Unknown Station",
                fuelTypes = selectedStation?.fuelTypes ?: emptyList(),
                showSheet = selectedStation != null,
                onDismiss = { selectedStation = null },
                onFuelSelected = { fuelType ->
                    selectedStation?.let { station ->
                        onStationSelected(station, fuelType)
                    }
                    selectedStation = null
                }
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 20.dp, start = 20.dp),
                shape = CircleShape,
                shadowElevation = 4.dp
            ) {
                ChevronLeft(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(45.dp)
                )
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            if (gasStations.isNotEmpty()) {
                Text(
                    text = "⛽ ${gasStations.size} stations",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 80.dp)
                        .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    color = Color.White
                )
            }
        }
    }
}
