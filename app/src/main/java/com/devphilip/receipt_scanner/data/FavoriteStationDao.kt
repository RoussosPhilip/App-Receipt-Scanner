package com.devphilip.receipt_scanner.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteStationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(station: FavoriteStation)

    @Query("DELETE FROM favorite_stations WHERE stationGUID = :stationGUID")
    suspend fun delete(stationGUID: String)

    @Query("SELECT * FROM favorite_stations ORDER BY brand ASC")
    fun getAll(): Flow<List<FavoriteStation>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_stations WHERE stationGUID = :stationGUID)")
    fun isFavorite(stationGUID: String): Flow<Boolean>
}