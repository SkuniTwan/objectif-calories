package com.legoueix.objectifcalories.textentry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.legoueix.objectifcalories.ciqual.AlimentRepository

class TextEntryViewModelFactory(
    private val repository: AlimentRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return TextEntryViewModel(repository) as T
    }
}
