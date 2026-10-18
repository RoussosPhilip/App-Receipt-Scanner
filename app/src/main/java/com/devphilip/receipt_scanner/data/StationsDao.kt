package com.devphilip.receipt_scanner.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface StationsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStations(stations: List<Station>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFuels(fuels: List<StationFuel>)

    @Query("""
        SELECT s.stationGUID, s.gasStationLat, s.gasStationLong,
               s.fuelCompNormalName, s.gasStationAddress,
               s.gasStationOwner,
               f.fuelName, f.fuelPrice
        FROM stations s
        LEFT JOIN station_fuels f ON s.stationGUID = f.stationGUID
        WHERE s.gasStationLat BETWEEN :minLat AND :maxLat
          AND s.gasStationLong BETWEEN :minLon AND :maxLon
    """)
    suspend fun getStationsWithFuels(
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double
    ): List<StationFuelRow>

    @Query("""
        SELECT fuelName FROM station_fuels
        WHERE stationGUID = :stationGUID
        AND fuelName IS NOT NULL
    """)
    suspend fun getFuelsForStation(stationGUID: String): List<String>

    @Query("SELECT DISTINCT fuelCompNormalName FROM stations WHERE fuelCompNormalName IS NOT NULL")
    suspend fun getAllBrandNames(): List<String>

    @Query("SELECT DISTINCT gasStationOwner FROM stations WHERE gasStationOwner IS NOT NULL")
    suspend fun getAllOwnerNames(): List<String>

    @Query("""
    SELECT DISTINCT gasStationOwner FROM stations 
    WHERE fuelCompNormalName = :brand 
    AND gasStationOwner IS NOT NULL
""")
    suspend fun getOwnersByBrand(brand: String): List<String>
}