package com.legoueix.objectifcalories.poids.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PoidsDao {
    @Query("SELECT * FROM poids ORDER BY dateHeure DESC")
    fun observerTout(): Flow<List<PoidsEntity>>

    @Insert
    suspend fun inserer(poids: PoidsEntity)

    @Query("DELETE FROM poids WHERE id = :id")
    suspend fun supprimer(id: Long)
}
