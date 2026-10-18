package com.devphilip.receipt_scanner.data

import com.devphilip.receipt_scanner.data.remote.GasStation

class StationsRepository(private val dao: StationsDao) {

    suspend fun getNearbyStations(
        lat: Double,
        lon: Double,
        radiusKm: Double = 15.0
    ): List<GasStation> {
        val delta = radiusKm / 111.0
        val rows = dao.getStationsWithFuels(
            minLat = lat - delta,
            maxLat = lat + delta,
            minLon = lon - delta,
            maxLon = lon + delta
        )
        return rows
            .groupBy { it.stationGUID }
            .map { (_, stationRows) ->
                val first = stationRows.first()
                GasStation(
                    stationGUID = first.stationGUID,
                    brand   = first.fuelCompNormalName ?: "Unknown",
                    lat     = first.gasStationLat,
                    lon     = first.gasStationLong,
                    address = first.gasStationAddress ?: "",
                    owner   = first.gasStationOwner ?: "",
                    fuelTypes = stationRows.mapNotNull { row -> row.fuelName }
                )
            }
    }
}