package com.legoueix.objectifcalories.poids.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

/** Historique des pesées : chaque pesée est une entrée datée, comme les repas ou l'eau. */
class PoidsRepository(context: Context) {

    private val dao = PoidsDatabase.getInstance(context).poidsDao()

    fun observerHistorique(): Flow<List<PoidsEntity>> = dao.observerTout()

    suspend fun enregistrer(poidsKg: Double, dateHeure: Long) {
        dao.inserer(PoidsEntity(poidsKg = poidsKg, dateHeure = dateHeure))
    }

    suspend fun supprimer(id: Long) {
        dao.supprimer(id)
    }
}
