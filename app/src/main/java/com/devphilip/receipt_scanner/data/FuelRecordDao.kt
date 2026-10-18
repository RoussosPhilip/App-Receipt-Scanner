package com.devphilip.receipt_scanner.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelRecordDao{
    @Insert
    suspend fun  insert(record: FuelRecord)

    @Update
    suspend fun update(record: FuelRecord)

    @Query("SELECT * FROM fuel_records ORDER BY date DESC")
    fun getAllRecords(): Flow<List<FuelRecord>>

    @Query("SELECT * FROM fuel_records WHERE id = :id")
    fun getRecordById(id: Int): Flow<FuelRecord?>

    @Delete
    suspend fun delete(record: FuelRecord)

    @Query("DELETE FROM fuel_records")
    suspend fun deleteAll()

    @Query("SELECT MAX(id) FROM fuel_records")
    suspend fun getLastInsertedId(): Int
}