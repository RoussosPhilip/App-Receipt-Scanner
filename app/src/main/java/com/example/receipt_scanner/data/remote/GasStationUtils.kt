package com.example.receipt_scanner.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

fun getGasStationLogo(brand: String?): String {
    return when (brand?.lowercase()?.trim()) {
        "avin" -> "avin.png"
        "bp" -> "bp.png"
        "cyclon" -> "cyclon.png"
        "eko" -> "eko.png"
        "elin", "ελινοιλ" -> "elin.png"
        "shell" -> "shell.png"
        "revoil" -> "revoil.png"
        "jetoil" -> "jetoil.png"
        "kmoil", "km oil" -> "kmoil.png"
        "aegean" -> "aegean.png"
        "ap", "απ", "attikes" -> "ap.png"
        "argo" -> "argo.png"
        "dracoil", "draco" -> "dracoil.png"
        "eteka" -> "eteka.png"
        "kaoil", "ka oil" -> "kaoil.png"
        "medoil" -> "medoil.png"
        "silkoil", "silk" -> "silkoil.png"
        else -> "15.png"  // Default fallback
    }
}

fun loadBitmapFromAssets(context: Context, fileName: String): Bitmap? {
    return try {
        context.assets.open(fileName).use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    } catch (e: Exception) {
        println("✗ Failed to load $fileName: ${e.message}")

        // Fallback: δοκίμασε το 15.png
        try {
            context.assets.open("15.png").use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e2: Exception) {
            null
        }
    }
}
