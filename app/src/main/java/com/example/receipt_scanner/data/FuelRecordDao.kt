package com.example.receipt_scanner.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelRecordDao{
    @Insert
    suspend fun  insert(record: FuelRecord)

    @Query("SELECT * FROM fuel_records ORDER BY date DESC")
    fun getAllRecords(): Flow<List<FuelRecord>>

    @Query("SELECT * FROM fuel_records WHERE id = :id")
    fun getRecordById(id: Int): Flow<FuelRecord?>

    @Delete
    suspend fun delete(record: FuelRecord)

    @Query("DELETE FROM fuel_records")
    suspend fun deleteAll()
}