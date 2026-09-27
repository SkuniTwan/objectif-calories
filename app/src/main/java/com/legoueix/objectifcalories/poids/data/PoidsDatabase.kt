package com.legoueix.objectifcalories.poids.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PoidsEntity::class], version = 1, exportSchema = false)
abstract class PoidsDatabase : RoomDatabase() {

    abstract fun poidsDao(): PoidsDao

    companion object {
        @Volatile
        private var instance: PoidsDatabase? = null

        fun getInstance(context: Context): PoidsDatabase {
            return instance ?: synchronized(this) {
                instance ?: build(context.applicationContext).also { instance = it }
            }
        }

        private fun build(context: Context): PoidsDatabase {
            return Room.databaseBuilder(context, PoidsDatabase::class.java, "poids.db")
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
