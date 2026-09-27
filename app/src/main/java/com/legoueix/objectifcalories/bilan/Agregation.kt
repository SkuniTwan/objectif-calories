package com.legoueix.objectifcalories.bilan

import com.legoueix.objectifcalories.activite.data.ActiviteEntity
import com.legoueix.objectifcalories.historique.Repas
import com.legoueix.objectifcalories.historique.glucidesG
import com.legoueix.objectifcalories.historique.kcal
import com.legoueix.objectifcalories.historique.lipidesG
import com.legoueix.objectifcalories.historique.proteinesG
import com.legoueix.objectifcalories.hydratation.data.HydratationEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Agrège les repas, l'eau et le sport d'une journée précise (bilans hebdo et mensuel). */
fun agregerJour(
    date: LocalDate,
    repas: List<Repas>,
    hydratations: List<HydratationEntity>,
    activites: List<ActiviteEntity>,
    zoneId: ZoneId,
): JourAgregat {
    val repasDuJour = repas.filter { Instant.ofEpochMilli(it.dateHeure).atZone(zoneId).toLocalDate() == date }
    val aliments = repasDuJour.flatMap { it.items }
    val activitesDuJour = activites.filter { Instant.ofEpochMilli(it.dateHeure).atZone(zoneId).toLocalDate() == date }
    return JourAgregat(
        date = date,
        glucidesG = aliments.sumOf { it.glucidesG },
        proteinesG = aliments.sumOf { it.proteinesG },
        lipidesG = aliments.sumOf { it.lipidesG },
        kcal = repasDuJour.sumOf { it.kcal },
        eauCl = hydratations
            .filter { Instant.ofEpochMilli(it.dateHeure).atZone(zoneId).toLocalDate() == date }
            .sumOf { it.centilitres },
        kcalDepense = activitesDuJour.sumOf { it.kcal },
        nombreActivites = activitesDuJour.size,
        dureeActivitesMinutes = activitesDuJour.sumOf { it.dureeMinutes },
    )
}
