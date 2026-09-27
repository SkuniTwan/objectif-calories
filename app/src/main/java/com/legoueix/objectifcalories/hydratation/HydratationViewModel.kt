package com.legoueix.objectifcalories.hydratation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.legoueix.objectifcalories.hydratation.data.HydratationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class HydratationViewModel(hydratationRepository: HydratationRepository) : ViewModel() {

    private val zoneId = ZoneId.systemDefault()

    val uiState: StateFlow<HydratationUiState> = hydratationRepository.observerHistorique()
        .map { historique ->
            val aujourdhui = LocalDate.now(zoneId)
            HydratationUiState(
                entreesAujourdhui = historique
                    .filter { Instant.ofEpochMilli(it.dateHeure).atZone(zoneId).toLocalDate() == aujourdhui }
                    .sortedBy { it.dateHeure },
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HydratationUiState())
}
