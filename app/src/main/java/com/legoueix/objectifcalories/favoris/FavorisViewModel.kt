package com.legoueix.objectifcalories.favoris

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.legoueix.objectifcalories.ciqual.AlimentRepository
import com.legoueix.objectifcalories.favoris.data.RepasFavoriRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Un favori avec son total de kcal calculé depuis Ciqual (jamais stocké — voir CLAUDE.md). */
data class FavoriAffiche(val favori: RepasFavori, val kcalTotal: Int)

class FavorisViewModel(
    private val repository: RepasFavoriRepository,
    private val alimentRepository: AlimentRepository,
) : ViewModel() {

    val favoris: StateFlow<List<FavoriAffiche>> = repository.observerFavoris()
        .map { liste ->
            liste.map { favori ->
                val kcalTotal = favori.items.sumOf { item ->
                    val aliment = alimentRepository.parCode(item.codeCiqual)
                    ((aliment?.kcal ?: 0) * item.grammes) / 100
                }
                FavoriAffiche(favori, kcalTotal)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onSupprimer(id: String) {
        viewModelScope.launch { repository.supprimer(id) }
    }
}
