package com.devphilip.receipt_scanner.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder

suspend fun fetchGasStations(
    centerLat: Double,
    centerLon: Double,
    radiusMeters: Int = 10000
): List<GasStation> = withContext(Dispatchers.IO) {
    val client = OkHttpClient()

    val query = """
        [out:json][timeout:25];
        (
          node["amenity"="fuel"](around:$radiusMeters,$centerLat,$centerLon);
          way["amenity"="fuel"](around:$radiusMeters,$centerLat,$centerLon);
        );
        out center tags;
    """.trimIndent()

    val url = "https://overpass-api.de/api/interpreter?data=${
        URLEncoder.encode(query, "UTF-8")
    }"

    val request = Request.Builder().url(url).build()

    try {
        val response = client.newCall(request).execute()
        val jsonString = response.body?.string() ?: return@withContext emptyList()

        val jsonObject = JSONObject(jsonString)
        val elements = jsonObject.getJSONArray("elements")

        val stations = mutableListOf<GasStation>()

        for (i in 0 until elements.length()) {
            val element = elements.getJSONObject(i)

            val lat = when {
                element.has("lat") -> element.getDouble("lat")
                element.has("center") -> element.getJSONObject("center").getDouble("lat")
                else -> continue
            }

            val lon = when {
                element.has("lon") -> element.getDouble("lon")
                element.has("center") -> element.getJSONObject("center").getDouble("lon")
                else -> continue
            }

            val brand = if (element.has("tags")) {
                val tags = element.getJSONObject("tags")
                tags.optString("brand", "").ifEmpty { "Unknown" }
            } else {
                "Unknown"
            }

            stations.add(
                GasStation(
                    brand     = brand,
                    lat       = lat,
                    lon       = lon,
                    address   = "",
                    fuelTypes = emptyList()
                )
            )
        }

        println("✓ Found ${stations.size} gas stations")
        stations

    } catch (e: Exception) {
        println("✗ Error: ${e.message}")
        e.printStackTrace()
        emptyList()
    }
}