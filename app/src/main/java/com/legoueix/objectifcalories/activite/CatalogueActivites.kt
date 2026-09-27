package com.legoueix.objectifcalories.activite

import kotlin.math.roundToInt

/**
 * Valeurs MET (Metabolic Equivalent of Task) issues du Compendium of Physical
 * Activities (Ainsworth et al.), référentiel académique public. kcal dépensées =
 * MET × poids (kg) × durée (h). Sélection volontairement restreinte aux activités
 * les plus courantes — pas besoin des ~800 entrées du compendium complet.
 */
data class TypeActivite(val nom: String, val met: Double, val emoji: String)

object CatalogueActivites {
    val liste = listOf(
        TypeActivite("Marche", 3.5, "🚶"),
        TypeActivite("Marche rapide", 5.0, "🚶‍♂️"),
        TypeActivite("Course à pied (8 km/h)", 8.3, "🏃"),
        TypeActivite("Course à pied (10 km/h)", 9.8, "🏃"),
        TypeActivite("Course à pied (12 km/h)", 11.8, "🏃"),
        TypeActivite("Vélo loisir", 4.0, "🚴"),
        TypeActivite("Vélo modéré", 6.8, "🚴"),
        TypeActivite("Vélo soutenu", 8.0, "🚴"),
        TypeActivite("Natation loisir", 6.0, "🏊"),
        TypeActivite("Natation soutenue", 9.8, "🏊"),
        TypeActivite("Musculation modérée", 3.5, "🏋️"),
        TypeActivite("Musculation intense", 6.0, "🏋️"),
        TypeActivite("Yoga", 2.5, "🧘"),
        TypeActivite("Football", 7.0, "⚽"),
        TypeActivite("Basketball", 6.5, "🏀"),
        TypeActivite("Tennis", 7.3, "🎾"),
        TypeActivite("Danse", 4.8, "💃"),
        TypeActivite("Randonnée", 6.0, "🥾"),
        TypeActivite("Ski de fond", 9.0, "🎿"),
        TypeActivite("Ski alpin", 6.0, "⛷️"),
        TypeActivite("Escalade", 7.5, "🧗"),
        TypeActivite("Aviron", 7.0, "🚣"),
        TypeActivite("Corde à sauter", 10.0, "🪢"),
        TypeActivite("Boxe (entraînement)", 7.8, "🥊"),
        TypeActivite("Roller / skate", 7.5, "🛼"),
        TypeActivite("Golf", 4.3, "⛳"),
        TypeActivite("Jardinage", 3.8, "🌱"),
        TypeActivite("Ménage", 3.0, "🧹"),
    )
}

// Utilisé tant que l'utilisateur n'a renseigné aucune pesée (voir menu → Poids).
const val POIDS_PAR_DEFAUT_KG = 70.0

fun kcalDepensees(met: Double, poidsKg: Double, dureeMinutes: Int): Int =
    (met * poidsKg * dureeMinutes / 60.0).roundToInt()
