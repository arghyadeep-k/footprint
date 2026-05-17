package com.footprint.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [LocationPoint::class],
    version = 1,
    exportSchema = false
)
abstract class FootprintDatabase : RoomDatabase() {
    abstract fun locationPointDao(): LocationPointDao
}
