package com.footprint.app.data.local

import android.content.Context
import androidx.room.Room

object FootprintDatabaseProvider {
    @Volatile
    private var instance: FootprintDatabase? = null

    fun getDatabase(context: Context): FootprintDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                FootprintDatabase::class.java,
                "footprint.db"
            ).build().also { instance = it }
        }
    }
}
