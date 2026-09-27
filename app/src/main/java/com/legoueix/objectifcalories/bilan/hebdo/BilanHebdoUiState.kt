package com.legoueix.objectifcalories.bilan.hebdo

import com.legoueix.objectifcalories.bilan.JourAgregat
import java.time.LocalDate

data class BilanHebdoUiState(
    val debutSemaine: LocalDate,
    val jours: List<JourAgregat>,
)

val BilanHebdoUiState.finSemaine: LocalDate
    get() = debutSemaine.plusDays(6)

val BilanHebdoUiState.totalKcal: Int
    get() = jours.sumOf { it.kcal }

val BilanHebdoUiState.totalEauCl: Int
    get() = jours.sumOf { it.eauCl }

val BilanHebdoUiState.totalKcalDepense: Int
    get() = jours.sumOf { it.kcalDepense }

val BilanHebdoUiState.totalNombreActivites: Int
    get() = jours.sumOf { it.nombreActivites }

val BilanHebdoUiState.totalDureeActivitesMinutes: Int
    get() = jours.sumOf { it.dureeActivitesMinutes }
