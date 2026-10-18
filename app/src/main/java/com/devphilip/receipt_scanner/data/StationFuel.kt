package com.devphilip.receipt_scanner.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "station_fuels")
data class StationFuel(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val stationGUID: String,
    val fuelName: String?,
    val fuelPrice: Double?
)