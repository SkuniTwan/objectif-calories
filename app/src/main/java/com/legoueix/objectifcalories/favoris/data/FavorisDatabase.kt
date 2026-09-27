package com.legoueix.objectifcalories.favoris.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [RepasFavoriEntity::class, RepasFavoriItemEntity::class], version = 2, exportSchema = false)
abstract class FavorisDatabase : RoomDatabase() {

    abstract fun repasFavoriDao(): RepasFavoriDao

    companion object {
        @Volatile
        private var instance: FavorisDatabase? = null

        fun getInstance(context: Context): FavorisDatabase {
            return instance ?: synchronized(this) {
                instance ?: build(context.applicationContext).also { instance = it }
            }
        }

        private fun build(context: Context): FavorisDatabase {
            return Room.databaseBuilder(context, FavorisDatabase::class.java, "favoris.db")
                // Pas de favoris fictifs à préserver, pas encore d'utilisateurs réels :
                // un changement de schéma repart simplement d'une base vide.
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
