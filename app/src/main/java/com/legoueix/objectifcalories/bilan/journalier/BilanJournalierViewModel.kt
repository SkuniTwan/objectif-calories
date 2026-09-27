package com.legoueix.objectifcalories.bilan.journalier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.legoueix.objectifcalories.activite.data.ActiviteRepository
import com.legoueix.objectifcalories.historique.data.RepasRepository
import com.legoueix.objectifcalories.hydratation.data.HydratationRepository
import com.legoueix.objectifcalories.poids.data.PoidsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class BilanJournalierViewModel(
    private val repasRepository: RepasRepository,
    private val hydratationRepository: HydratationRepository,
    private val activiteRepository: ActiviteRepository,
    private val poidsRepository: PoidsRepository,
) : ViewModel() {

    private val zoneId = ZoneId.systemDefault()

    private val _dateSelectionnee = MutableStateFlow(LocalDate.now())
    val dateSelectionnee: StateFlow<LocalDate> = _dateSelectionnee.asStateFlow()

    val uiState: StateFlow<BilanJournalierUiState> = combine(
        _dateSelectionnee,
        repasRepository.observerHistorique(),
        hydratationRepository.observerHistorique(),
        activiteRepository.observerHistorique(),
        poidsRepository.observerHistorique(),
    ) { date, repas, hydratations, activites, poids ->
        BilanJournalierUiState(
            date = date,
            repas = repas
                .filter { Instant.ofEpochMilli(it.dateHeure).atZone(zoneId).toLocalDate() == date }
                .sortedBy { it.dateHeure },
            eauCl = hydratations
                .filter { Instant.ofEpochMilli(it.dateHeure).atZone(zoneId).toLocalDate() == date }
                .sumOf { it.centilitres },
            activites = activites
                .filter { Instant.ofEpochMilli(it.dateHeure).atZone(zoneId).toLocalDate() == date }
                .sortedBy { it.dateHeure },
            dernierPoidsKg = poids.firstOrNull()?.poidsKg,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        BilanJournalierUiState(
            date = LocalDate.now(),
            repas = emptyList(),
            eauCl = 0,
            activites = emptyList(),
            dernierPoidsKg = null,
        ),
    )

    fun onJourPrecedent() {
        _dateSelectionnee.value = _dateSelectionnee.value.minusDays(1)
    }

    fun onJourSuivant() {
        _dateSelectionnee.value = _dateSelectionnee.value.plusDays(1)
    }

    fun onSupprimerRepas(id: Long) {
        viewModelScope.launch { repasRepository.supprimer(id) }
    }

    fun onSupprimerActivite(id: Long) {
        viewModelScope.launch { activiteRepository.supprimer(id) }
    }
}
