package com.legoueix.objectifcalories.bilan.mensuel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.legoueix.objectifcalories.activite.data.ActiviteRepository
import com.legoueix.objectifcalories.bilan.agregerJour
import com.legoueix.objectifcalories.historique.data.RepasRepository
import com.legoueix.objectifcalories.hydratation.data.HydratationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth
import java.time.ZoneId

class BilanMensuelViewModel(
    repasRepository: RepasRepository,
    hydratationRepository: HydratationRepository,
    activiteRepository: ActiviteRepository,
) : ViewModel() {

    private val zoneId = ZoneId.systemDefault()

    private val _mois = MutableStateFlow(YearMonth.now())
    val mois: StateFlow<YearMonth> = _mois.asStateFlow()

    val uiState: StateFlow<BilanMensuelUiState> = combine(
        _mois,
        repasRepository.observerHistorique(),
        hydratationRepository.observerHistorique(),
        activiteRepository.observerHistorique(),
    ) { mois, repas, hydratations, activites ->
        val jours = (1..mois.lengthOfMonth()).map { jourDuMois ->
            agregerJour(mois.atDay(jourDuMois), repas, hydratations, activites, zoneId)
        }
        BilanMensuelUiState(mois = mois, jours = jours)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        BilanMensuelUiState(mois = _mois.value, jours = emptyList()),
    )

    fun onMoisPrecedent() {
        _mois.value = _mois.value.minusMonths(1)
    }

    fun onMoisSuivant() {
        _mois.value = _mois.value.plusMonths(1)
    }
}
