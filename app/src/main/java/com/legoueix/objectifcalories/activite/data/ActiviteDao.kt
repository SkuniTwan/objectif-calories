package com.legoueix.objectifcalories.activite.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ActiviteDao {
    @Query("SELECT * FROM activite ORDER BY dateHeure DESC")
    fun observerTout(): Flow<List<ActiviteEntity>>

    @Insert
    suspend fun inserer(activite: ActiviteEntity)

    @Query("DELETE FROM activite WHERE id = :id")
    suspend fun supprimer(id: Long)
}
