package com.devphilip.receipt_scanner.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FuelRecord::class], version = 2, exportSchema = false)
abstract class AppDataBase : RoomDatabase(){

    abstract fun fuelRecordDao(): FuelRecordDao

    companion object{
        @Volatile
        private var INSTANCE: AppDataBase? = null

        fun getDataBase(context: Context): AppDataBase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDataBase::class.java,
                    "fuel_records_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}