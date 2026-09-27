package com.legoueix.objectifcalories.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.legoueix.objectifcalories.activite.data.ActiviteRepository
import com.legoueix.objectifcalories.bilan.agregerJour
import com.legoueix.objectifcalories.historique.data.RepasRepository
import com.legoueix.objectifcalories.hydratation.data.HydratationRepository
import com.legoueix.objectifcalories.poids.data.PoidsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoField

data class AppDrawerUiState(
    val eauClAujourdhui: Int = 0,
    val kcalActiviteAujourdhui: Int = 0,
    val dernierPoidsKg: Double? = null,
    // Écart entre la pesée la plus récente et la toute première : un repère
    // descriptif ("depuis le début du suivi"), pas un objectif de poids.
    val deltaPoidsKg: Double? = null,
    val kcalConsommesSemaine: Int = 0,
)

class AppDrawerViewModel(
    repasRepository: RepasRepository,
    hydratationRepository: HydratationRepository,
    activiteRepository: ActiviteRepository,
    poidsRepository: PoidsRepository,
) : ViewModel() {

    private val zoneId = ZoneId.systemDefault()

    val uiState: StateFlow<AppDrawerUiState> = combine(
        repasRepository.observerHistorique(),
        hydratationRepository.observerHistorique(),
        activiteRepository.observerHistorique(),
        poidsRepository.observerHistorique(),
    ) { repas, hydratations, activites, pesees ->
        val aujourdhui = LocalDate.now(zoneId)
        val debutSemaine = aujourdhui.with(ChronoField.DAY_OF_WEEK, 1)
        val kcalSemaine = (0..6).sumOf { decalage ->
            agregerJour(debutSemaine.plusDays(decalage.toLong()), repas, hydratations, activites, zoneId).kcal
        }
        val peseesTriees = pesees.sortedByDescending { it.dateHeure }
        AppDrawerUiState(
            eauClAujourdhui = hydratations
                .filter { Instant.ofEpochMilli(it.dateHeure).atZone(zoneId).toLocalDate() == aujourdhui }
                .sumOf { it.centilitres },
            kcalActiviteAujourdhui = activites
                .filter { Instant.ofEpochMilli(it.dateHeure).atZone(zoneId).toLocalDate() == aujourdhui }
                .sumOf { it.kcal },
            dernierPoidsKg = peseesTriees.firstOrNull()?.poidsKg,
            deltaPoidsKg = if (peseesTriees.size >= 2) {
                peseesTriees.first().poidsKg - peseesTriees.last().poidsKg
            } else {
                null
            },
            kcalConsommesSemaine = kcalSemaine,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppDrawerUiState())
}

class AppDrawerViewModelFactory(
    private val repasRepository: RepasRepository,
    private val hydratationRepository: HydratationRepository,
    private val activiteRepository: ActiviteRepository,
    private val poidsRepository: PoidsRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return AppDrawerViewModel(repasRepository, hydratationRepository, activiteRepository, poidsRepository) as T
    }
}
