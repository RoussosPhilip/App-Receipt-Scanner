package com.devphilip.receipt_scanner.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

fun getGasStationLogo(brand: String?): String {
    return when (brand?.lowercase()?.trim()) {
        "avin" -> "avin.png"
        "bp" -> "bp.png"
        "cyclon" -> "cyclon.png"
        "eko", "εκο" -> "eko.png"
        "elin", "ελινοιλ" -> "elin.png"
        "shell" -> "shell.png"
        "revoil" -> "revoil.png"
        "jetoil" -> "jetoil.png"
        "kmoil", "km oil" -> "kmoil.png"
        "aegean" -> "aegean.png"
        "ap", "απ", "α.π.", "a.p." -> "ap.png"
        "argo" -> "argo.png"
        "dracoil", "draco" -> "dracoil.png"
        "eteka", "ετεκα" -> "eteka.png"
        "kaoil", "ka oil" -> "kaoil.png"
        "medoil" -> "medoil.png"
        "silkoil", "silk" -> "silkoil.png"
        else -> "unknown_gas_station.png"  // Default fallback
    }
}

fun loadBitmapFromAssets(context: Context, fileName: String): Bitmap? {
    return try {
        context.assets.open(fileName).use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    } catch (e: Exception) {
        println("✗ Failed to load $fileName: ${e.message}")

        try {
            context.assets.open("15.png").use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e2: Exception) {
            null
        }
    }
}
