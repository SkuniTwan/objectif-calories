package com.legoueix.objectifcalories.favoris

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.legoueix.objectifcalories.ciqual.AlimentRepository
import com.legoueix.objectifcalories.favoris.data.RepasFavoriRepository

class FavorisViewModelFactory(
    private val repository: RepasFavoriRepository,
    private val alimentRepository: AlimentRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return FavorisViewModel(repository, alimentRepository) as T
    }
}
