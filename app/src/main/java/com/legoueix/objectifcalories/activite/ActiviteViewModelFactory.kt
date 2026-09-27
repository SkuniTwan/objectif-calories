package com.legoueix.objectifcalories.activite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.legoueix.objectifcalories.activite.data.ActiviteRepository
import com.legoueix.objectifcalories.poids.data.PoidsRepository

class ActiviteViewModelFactory(
    private val activiteRepository: ActiviteRepository,
    private val poidsRepository: PoidsRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return ActiviteViewModel(activiteRepository, poidsRepository) as T
    }
}
