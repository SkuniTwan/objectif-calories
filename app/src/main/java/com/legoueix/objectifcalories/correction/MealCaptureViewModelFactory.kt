package com.legoueix.objectifcalories.correction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.legoueix.objectifcalories.analysis.MealAnalyzer
import com.legoueix.objectifcalories.ciqual.AlimentRepository
import com.legoueix.objectifcalories.historique.data.RepasRepository
import com.legoueix.objectifcalories.openfoodfacts.OpenFoodFactsRepository

class MealCaptureViewModelFactory(
    private val analyzer: MealAnalyzer,
    private val alimentRepository: AlimentRepository,
    private val openFoodFactsRepository: OpenFoodFactsRepository,
    private val repasRepository: RepasRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return MealCaptureViewModel(analyzer, alimentRepository, openFoodFactsRepository, repasRepository) as T
    }
}
