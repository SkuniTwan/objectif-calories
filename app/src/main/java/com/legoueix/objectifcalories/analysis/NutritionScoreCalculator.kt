package com.legoueix.objectifcalories.analysis

import kotlin.math.roundToInt

/**
 * Note nutritionnelle simplifiée (0 à 100), calculée à partir des valeurs pour 100 g du
 * repas dans son ensemble : pénalités sur la densité calorique, les sucres, les acides
 * gras saturés et le sodium ; bonus sur les fibres et les protéines.
 *
 * Heuristique provisoire indicative, pas l'algorithme officiel du Nutri-Score — à
 * revoir une fois Ciqual branché (voir CLAUDE.md, étape 2 de l'ordre de construction).
 */
object NutritionScoreCalculator {

    fun calculer(items: List<Pair<NutritionPer100g, Int>>): Int {
        val poidsTotal = items.sumOf { (_, grammes) -> grammes }.coerceAtLeast(1)

        fun pour100g(select: (NutritionPer100g) -> Double): Double {
            val total = items.sumOf { (nutrition, grammes) -> select(nutrition) * grammes / 100.0 }
            return total * 100.0 / poidsTotal
        }

        val kcalPour100g = pour100g { it.kcal.toDouble() }
        val sucresPour100g = pour100g { it.sucres }
        val acidesGrasPour100g = pour100g { it.acidesGrasSatures }
        val sodiumPour100g = pour100g { it.sodium }
        val fibresPour100g = pour100g { it.fibres }
        val proteinesPour100g = pour100g { it.proteines }

        // Pénalités proportionnelles (pas de seuil à dépasser) : un repas courant doit
        // pouvoir atterrir n'importe où sur l'échelle, pas seulement au-delà d'un excès.
        var score = 100.0
        score -= kcalPour100g * 0.06
        score -= sucresPour100g * 1.5
        score -= acidesGrasPour100g * 4.0
        score -= sodiumPour100g * 0.05
        score += (fibresPour100g * 2.5).coerceAtMost(12.0)
        score += (proteinesPour100g * 0.6).coerceAtMost(12.0)

        return score.coerceIn(0.0, 100.0).roundToInt()
    }
}
