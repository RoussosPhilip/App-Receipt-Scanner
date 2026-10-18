package com.devphilip.receipt_scanner.data
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stations")
data class Station(
    @PrimaryKey val stationGUID: String,
    val gasStationLat: Double,
    val gasStationLong: Double,
    val fuelCompNormalName: String?,
    val gasStationAddress: String?,
    val gasStationOwner: String?
)