package com.devphilip.receipt_scanner.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class ScannedReceiptData(
    val stationName: String = "",
    val stationBrand: String = "",
    val stationAddress: String = "",
    val date: String = "",        // format "yyyy-MM-dd"
    val time: String = "",        // format "HH:mm"
    val liters: Double = 0.0,
    val unitPrice: Double = 0.0,
    val amount: Double = 0.0,
    val fuelType: String = ""
)

class FirebaseReceiptService(private val context: Context) {

    private val model = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel("gemini-2.5-flash")

    suspend fun analyzeReceipt(uri: Uri): Result<ScannedReceiptData> = withContext(Dispatchers.IO) {
        try {
            val bitmap = uriToBitmap(uri)

            val response = model.generateContent(
                content {
                    image(bitmap)
                    text("""
                        Analyze this fuel receipt (Greek context) and extract the following into a valid JSON object.
                        Correct any OCR errors based on context.

                        Required fields:
                        - date: Format "DD/MM/YYYY"
                        - time: Format "HH:mm" 24h
                        - business_name: The company name
                        - station_brand: The station brand (Extract only if explicitly written in the header/logo OR inside a branded fuel name such as 
                            "Crystal"(ELIN),"Best"(AVIN),"V-Power"(Shell),"Kinitron"(EKO)). Otherwise null. No guessing)
                        - address: Station Address (if is available, otherwise null) 
                        - fuel_type: The specific type in english (e.g., "Unleaded 95", "Diesel")
                        - quantity: The volume in liters (number)
                        - unit_price: Price per liter (number)
                        - price: The final total amount (number)

                        Return ONLY the raw JSON.
                    """.trimIndent())
                }
            )

            // json cleanup
            val rawJson = response.text
                ?.replace(Regex("```json|```"), "")
                ?.trim() ?: "{}"

            Log.d("ReceiptService", "Raw response: $rawJson")

            val parsed = parseJson(rawJson)
            Log.d("ReceiptService", "Parsed: $parsed")


            Result.success(parsed)
        } catch (e: Exception) {
            Log.e("ReceiptService", "Error: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun uriToBitmap(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(context.contentResolver, uri)
            ) { decoder, _, _ ->
                decoder.isMutableRequired = true
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    }

    private fun parseJson(json: String): ScannedReceiptData {
        return try {
            val obj = JSONObject(json)
            ScannedReceiptData(
                stationName = obj.optString("business_name"),
                stationBrand = obj.optString("station_brand"),
                stationAddress = obj.optString("address"),
                date = convertDate(obj.optString("date")),  // DD/MM/YYYY to yyyy-MM-dd
                time = obj.optString("time"),
                liters = obj.optDouble("quantity", 0.0),
                unitPrice = obj.optDouble("unit_price", 0.0),
                amount = obj.optDouble("price", 0.0),
                fuelType = obj.optString("fuel_type")
            )
        } catch (e: Exception) {
            ScannedReceiptData()
        }
    }

    // date type conversion
    private fun convertDate(input: String): String {
        return try {
            val parts = input.split("/")
            if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else input
        } catch (e: Exception) {
            input
        }
    }
}
