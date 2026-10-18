package com.devphilip.receipt_scanner.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Station::class, StationFuel::class],
    version = 1,
    exportSchema = false
)
abstract class StationsDataBase : RoomDatabase() {
    abstract fun stationsDao(): StationsDao

    companion object {
        @Volatile private var INSTANCE: StationsDataBase? = null

        fun getDataBase(context: Context): StationsDataBase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    StationsDataBase::class.java,
                    "stations_all.db"
                )
                    .createFromAsset("stations_all.db")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}