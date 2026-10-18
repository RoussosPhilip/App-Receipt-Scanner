package com.devphilip.receipt_scanner.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_stations")
data class FavoriteStation(
    @PrimaryKey val stationGUID: String,
    val brand: String,
    val owner: String,
    val address: String
)