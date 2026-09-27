package com.legoueix.objectifcalories.favoris.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface RepasFavoriDao {

    @Transaction
    @Query("SELECT * FROM repas_favori ORDER BY id DESC")
    fun observerTout(): Flow<List<RepasFavoriAvecItems>>

    @Insert
    suspend fun insererRepas(repas: RepasFavoriEntity): Long

    @Insert
    suspend fun insererItems(items: List<RepasFavoriItemEntity>)

    @Query("SELECT photoPath FROM repas_favori WHERE id = :id")
    suspend fun obtenirCheminPhoto(id: Long): String?

    @Query("DELETE FROM repas_favori WHERE id = :id")
    suspend fun supprimer(id: Long)
}
