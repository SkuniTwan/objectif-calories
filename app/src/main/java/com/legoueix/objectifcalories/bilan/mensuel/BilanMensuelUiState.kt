package com.legoueix.objectifcalories.bilan.mensuel

import com.legoueix.objectifcalories.bilan.JourAgregat
import java.time.YearMonth

data class BilanMensuelUiState(
    val mois: YearMonth,
    val jours: List<JourAgregat>,
)

val BilanMensuelUiState.totalKcal: Int
    get() = jours.sumOf { it.kcal }

val BilanMensuelUiState.totalEauCl: Int
    get() = jours.sumOf { it.eauCl }

val BilanMensuelUiState.totalKcalDepense: Int
    get() = jours.sumOf { it.kcalDepense }

val BilanMensuelUiState.totalNombreActivites: Int
    get() = jours.sumOf { it.nombreActivites }

val BilanMensuelUiState.totalDureeActivitesMinutes: Int
    get() = jours.sumOf { it.dureeActivitesMinutes }
