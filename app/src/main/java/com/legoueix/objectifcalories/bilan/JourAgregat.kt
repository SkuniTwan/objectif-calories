package com.legoueix.objectifcalories.bilan

import java.time.LocalDate

/** Totaux d'un jour (repas + eau + sport), utilisés par les bilans hebdo et mensuel. */
data class JourAgregat(
    val date: LocalDate,
    val glucidesG: Int,
    val proteinesG: Int,
    val lipidesG: Int,
    val kcal: Int,
    val eauCl: Int,
    val kcalDepense: Int,
    val nombreActivites: Int,
    val dureeActivitesMinutes: Int,
)
