package com.devphilip.receipt_scanner.data

data class StationFuelRow(
    val stationGUID: String,
    val gasStationLat: Double,
    val gasStationLong: Double,
    val fuelCompNormalName: String?,
    val gasStationAddress: String?,
    val gasStationOwner: String?,
    val fuelName: String?,
    val fuelPrice: Double?
)