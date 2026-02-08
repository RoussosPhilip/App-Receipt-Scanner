package com.devphilip.receipt_scanner.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fuel_records")
data class FuelRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val stationName: String,
    val date: String,
    val time: String = "",
    val liters: Double,
    val amount: Double,
)