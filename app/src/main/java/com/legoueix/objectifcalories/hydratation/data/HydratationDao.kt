package com.legoueix.objectifcalories.hydratation.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HydratationDao {
    @Query("SELECT * FROM hydratation ORDER BY dateHeure DESC")
    fun observerTout(): Flow<List<HydratationEntity>>

    @Insert
    suspend fun inserer(hydratation: HydratationEntity)
}
