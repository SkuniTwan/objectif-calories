package com.legoueix.objectifcalories.historique.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface RepasDao {
    @Transaction
    @Query("SELECT * FROM repas ORDER BY dateHeure DESC")
    fun observerTout(): Flow<List<RepasAvecItems>>

    @Insert
    suspend fun insererRepas(repas: RepasEntity): Long

    @Insert
    suspend fun insererItems(items: List<RepasItemEntity>)

    @Query("SELECT photoPath FROM repas WHERE id = :id")
    suspend fun obtenirCheminPhoto(id: Long): String?

    @Query("DELETE FROM repas WHERE id = :id")
    suspend fun supprimer(id: Long)
}
