package com.legoueix.objectifcalories.bilan.journalier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.legoueix.objectifcalories.activite.data.ActiviteRepository
import com.legoueix.objectifcalories.historique.data.RepasRepository
import com.legoueix.objectifcalories.hydratation.data.HydratationRepository
import com.legoueix.objectifcalories.poids.data.PoidsRepository

class BilanJournalierViewModelFactory(
    private val repasRepository: RepasRepository,
    private val hydratationRepository: HydratationRepository,
    private val activiteRepository: ActiviteRepository,
    private val poidsRepository: PoidsRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return BilanJournalierViewModel(repasRepository, hydratationRepository, activiteRepository, poidsRepository) as T
    }
}
