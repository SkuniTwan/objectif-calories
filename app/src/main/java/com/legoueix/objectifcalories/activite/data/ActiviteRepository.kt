package com.legoueix.objectifcalories.activite.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class ActiviteRepository(context: Context) {

    private val dao = ActiviteDatabase.getInstance(context).activiteDao()

    fun observerHistorique(): Flow<List<ActiviteEntity>> = dao.observerTout()

    suspend fun enregistrer(nom: String, met: Double, dureeMinutes: Int, kcal: Int, dateHeure: Long) {
        dao.inserer(
            ActiviteEntity(
                nom = nom,
                met = met,
                dureeMinutes = dureeMinutes,
                kcal = kcal,
                dateHeure = dateHeure,
            ),
        )
    }

    suspend fun supprimer(id: Long) {
        dao.supprimer(id)
    }
}
