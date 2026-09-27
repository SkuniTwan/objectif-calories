package com.legoueix.objectifcalories.bilan.journalier

import com.legoueix.objectifcalories.activite.data.ActiviteEntity
import com.legoueix.objectifcalories.historique.Repas
import java.time.LocalDate

data class BilanJournalierUiState(
    val date: LocalDate,
    val repas: List<Repas>,
    val eauCl: Int,
    val activites: List<ActiviteEntity>,
    val dernierPoidsKg: Double?,
)
