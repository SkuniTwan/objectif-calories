package com.legoueix.objectifcalories.historique

import com.legoueix.objectifcalories.analysis.NutritionPer100g
import kotlin.math.roundToInt

data class Repas(
    val id: Long,
    val nom: String,
    val dateHeure: Long,
    val photo: ByteArray?,
    val items: List<AlimentEnregistre>,
)

/**
 * Valeurs nutritionnelles figées à l'enregistrement (pour 100 g) : un journal doit
 * rester immuable même si Ciqual est mis à jour plus tard (voir CLAUDE.md).
 */
data class AlimentEnregistre(
    val nom: String,
    val codeCiqual: String?,
    val grammesSuggeres: Int,
    val grammes: Int,
    val nutrition: NutritionPer100g,
)

val AlimentEnregistre.kcal: Int
    get() = (nutrition.kcal * grammes) / 100

// Grammages arrondis par aliment. Les totaux d'un repas ou d'une journée doivent être la
// somme de CES valeurs déjà arrondies (pas l'arrondi de la somme brute), sans quoi l'addition
// affichée à l'écran (un arrondi par ligne) ne retombe pas sur le total affiché ailleurs.
val AlimentEnregistre.glucidesG: Int
    get() = (nutrition.glucides * grammes / 100.0).roundToInt()

val AlimentEnregistre.proteinesG: Int
    get() = (nutrition.proteines * grammes / 100.0).roundToInt()

val AlimentEnregistre.lipidesG: Int
    get() = (nutrition.lipides * grammes / 100.0).roundToInt()

val Repas.kcal: Int
    get() = items.sumOf { it.kcal }

