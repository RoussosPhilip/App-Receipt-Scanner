package com.example.receipt_scanner.data

data class FuelRecord(
    val stationName: String,
    val date: String,
    val liters: Double,
    val amount: Double
)