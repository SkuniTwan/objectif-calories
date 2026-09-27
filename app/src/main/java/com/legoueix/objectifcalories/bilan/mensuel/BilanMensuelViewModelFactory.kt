package com.legoueix.objectifcalories.bilan.mensuel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.legoueix.objectifcalories.activite.data.ActiviteRepository
import com.legoueix.objectifcalories.historique.data.RepasRepository
import com.legoueix.objectifcalories.hydratation.data.HydratationRepository

class BilanMensuelViewModelFactory(
    private val repasRepository: RepasRepository,
    private val hydratationRepository: HydratationRepository,
    private val activiteRepository: ActiviteRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return BilanMensuelViewModel(repasRepository, hydratationRepository, activiteRepository) as T
    }
}
