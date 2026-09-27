package com.legoueix.objectifcalories.historique.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [RepasEntity::class, RepasItemEntity::class], version = 2, exportSchema = false)
abstract class RepasDatabase : RoomDatabase() {

    abstract fun repasDao(): RepasDao

    companion object {
        @Volatile
        private var instance: RepasDatabase? = null

        fun getInstance(context: Context): RepasDatabase {
            return instance ?: synchronized(this) {
                instance ?: build(context.applicationContext).also { instance = it }
            }
        }

        private fun build(context: Context): RepasDatabase {
            return Room.databaseBuilder(context, RepasDatabase::class.java, "repas.db")
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
