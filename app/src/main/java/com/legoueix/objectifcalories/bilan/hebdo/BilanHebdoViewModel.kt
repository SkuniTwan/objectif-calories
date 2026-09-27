package com.legoueix.objectifcalories.bilan.hebdo

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
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoField

class BilanHebdoViewModel(
    repasRepository: RepasRepository,
    hydratationRepository: HydratationRepository,
    activiteRepository: ActiviteRepository,
) : ViewModel() {

    private val zoneId = ZoneId.systemDefault()

    private val _debutSemaine = MutableStateFlow(lundiDe(LocalDate.now()))
    val debutSemaine: StateFlow<LocalDate> = _debutSemaine.asStateFlow()

    val uiState: StateFlow<BilanHebdoUiState> = combine(
        _debutSemaine,
        repasRepository.observerHistorique(),
        hydratationRepository.observerHistorique(),
        activiteRepository.observerHistorique(),
    ) { debut, repas, hydratations, activites ->
        val jours = (0..6).map { decalage ->
            agregerJour(debut.plusDays(decalage.toLong()), repas, hydratations, activites, zoneId)
        }
        BilanHebdoUiState(debutSemaine = debut, jours = jours)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        BilanHebdoUiState(debutSemaine = _debutSemaine.value, jours = emptyList()),
    )

    fun onSemainePrecedente() {
        _debutSemaine.value = _debutSemaine.value.minusWeeks(1)
    }

    fun onSemaineSuivante() {
        _debutSemaine.value = _debutSemaine.value.plusWeeks(1)
    }
}

private fun lundiDe(date: LocalDate): LocalDate = date.with(ChronoField.DAY_OF_WEEK, 1)
