package com.legoueix.objectifcalories.activite.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ActiviteEntity::class], version = 1, exportSchema = false)
abstract class ActiviteDatabase : RoomDatabase() {

    abstract fun activiteDao(): ActiviteDao

    companion object {
        @Volatile
        private var instance: ActiviteDatabase? = null

        fun getInstance(context: Context): ActiviteDatabase {
            return instance ?: synchronized(this) {
                instance ?: build(context.applicationContext).also { instance = it }
            }
        }

        private fun build(context: Context): ActiviteDatabase {
            return Room.databaseBuilder(context, ActiviteDatabase::class.java, "activite.db")
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
