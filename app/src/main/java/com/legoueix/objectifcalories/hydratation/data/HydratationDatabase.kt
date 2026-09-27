package com.legoueix.objectifcalories.hydratation.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [HydratationEntity::class], version = 2, exportSchema = false)
abstract class HydratationDatabase : RoomDatabase() {

    abstract fun hydratationDao(): HydratationDao

    companion object {
        @Volatile
        private var instance: HydratationDatabase? = null

        fun getInstance(context: Context): HydratationDatabase {
            return instance ?: synchronized(this) {
                instance ?: build(context.applicationContext).also { instance = it }
            }
        }

        private fun build(context: Context): HydratationDatabase {
            return Room.databaseBuilder(context, HydratationDatabase::class.java, "hydratation.db")
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
