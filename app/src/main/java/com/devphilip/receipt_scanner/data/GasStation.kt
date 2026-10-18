package com.devphilip.receipt_scanner.data.remote

data class GasStation(
    val stationGUID: String = "",
    val brand: String,
    val lat: Double,
    val lon: Double,
    val address: String = "",
    val owner: String = "",
    val fuelTypes: List<String> = emptyList()
)