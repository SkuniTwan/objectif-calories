package com.legoueix.objectifcalories.ciqual

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [AlimentEntity::class], version = 1, exportSchema = false)
abstract class CiqualDatabase : RoomDatabase() {

    abstract fun alimentDao(): AlimentDao

    companion object {
        @Volatile
        private var instance: CiqualDatabase? = null

        fun getInstance(context: Context): CiqualDatabase {
            return instance ?: synchronized(this) {
                instance ?: build(context.applicationContext).also { instance = it }
            }
        }

        private fun build(context: Context): CiqualDatabase {
            return Room.databaseBuilder(context, CiqualDatabase::class.java, "ciqual.db")
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            getInstance(context).alimentDao().insererTout(CiqualSeeder.charger(context))
                        }
                    }
                })
                .build()
        }
    }
}
