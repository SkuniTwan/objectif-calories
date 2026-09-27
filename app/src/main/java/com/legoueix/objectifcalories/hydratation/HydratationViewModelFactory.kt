package com.legoueix.objectifcalories.hydratation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.legoueix.objectifcalories.hydratation.data.HydratationRepository

class HydratationViewModelFactory(
    private val hydratationRepository: HydratationRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return HydratationViewModel(hydratationRepository) as T
    }
}
