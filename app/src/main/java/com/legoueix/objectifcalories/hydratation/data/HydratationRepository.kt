package com.legoueix.objectifcalories.hydratation.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class HydratationRepository(context: Context) {

    private val dao = HydratationDatabase.getInstance(context).hydratationDao()

    fun observerHistorique(): Flow<List<HydratationEntity>> = dao.observerTout()

    suspend fun enregistrer(dateHeure: Long, centilitres: Int) {
        dao.inserer(HydratationEntity(dateHeure = dateHeure, centilitres = centilitres))
    }
}
