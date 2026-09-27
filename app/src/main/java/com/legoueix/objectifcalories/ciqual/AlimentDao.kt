package com.legoueix.objectifcalories.ciqual

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AlimentDao {

    // Recherche simple par sous-chaîne, triée par longueur de libellé (heuristique de
    // pertinence : "Pomme" doit sortir avant "Pomme de terre, cuite à l'eau"). Un scan
    // complet sur ~3 500 lignes reste de l'ordre de quelques ms sur SQLite ; à revoir
    // (FTS, table de synonymes) seulement si la pertinence des résultats s'avère
    // insuffisante en usage réel.
    @Query("SELECT * FROM aliment WHERE libelle LIKE '%' || :texte || '%' ORDER BY LENGTH(libelle) ASC LIMIT :limite")
    suspend fun rechercher(texte: String, limite: Int = 6): List<AlimentEntity>

    @Query("SELECT * FROM aliment WHERE code = :code LIMIT 1")
    suspend fun parCode(code: String): AlimentEntity?

    @Insert
    suspend fun insererTout(aliments: List<AlimentEntity>)

    @Query("SELECT COUNT(*) FROM aliment")
    suspend fun compter(): Int
}
