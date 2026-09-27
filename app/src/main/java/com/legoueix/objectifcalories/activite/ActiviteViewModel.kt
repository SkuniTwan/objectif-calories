package com.legoueix.objectifcalories.activite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.legoueix.objectifcalories.activite.data.ActiviteEntity
import com.legoueix.objectifcalories.activite.data.ActiviteRepository
import com.legoueix.objectifcalories.poids.data.PoidsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class ActiviteViewModel(
    private val activiteRepository: ActiviteRepository,
    poidsRepository: PoidsRepository,
) : ViewModel() {

    private val zoneId = ZoneId.systemDefault()

    // observerHistorique() trie déjà par dateHeure décroissante : le premier élément
    // est la pesée la plus récente. Reste à null tant qu'aucune pesée n'a été enregistrée
    // (menu → Poids) — les appelants retombent alors sur POIDS_PAR_DEFAUT_KG.
    val dernierPoidsKg: StateFlow<Double?> = poidsRepository.observerHistorique()
        .map { historique -> historique.firstOrNull()?.poidsKg }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val activitesAujourdhui: StateFlow<List<ActiviteEntity>> = activiteRepository.observerHistorique()
        .map { historique ->
            val aujourdhui = LocalDate.now(zoneId)
            historique
                .filter { Instant.ofEpochMilli(it.dateHeure).atZone(zoneId).toLocalDate() == aujourdhui }
                .sortedByDescending { it.dateHeure }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onEnregistrerActivite(activite: TypeActivite, dureeMinutes: Int, dateHeure: Long) {
        val poids = dernierPoidsKg.value ?: POIDS_PAR_DEFAUT_KG
        val kcal = kcalDepensees(activite.met, poids, dureeMinutes)
        viewModelScope.launch {
            activiteRepository.enregistrer(
                nom = activite.nom,
                met = activite.met,
                dureeMinutes = dureeMinutes,
                kcal = kcal,
                dateHeure = dateHeure,
            )
        }
    }

    fun onSupprimerActivite(id: Long) {
        viewModelScope.launch { activiteRepository.supprimer(id) }
    }
}
