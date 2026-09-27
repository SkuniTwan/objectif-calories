package com.legoueix.objectifcalories.analysis

/**
 * Valeurs nutritionnelles pour 100 g de partie comestible : kcal, protéines (g),
 * glucides (g), lipides (g), sucres (g, sous-catégorie des glucides), acides gras
 * saturés (g, sous-catégorie des lipides), fibres (g), sodium (mg).
 *
 * Toujours issues de Ciqual (voir [com.legoueix.objectifcalories.ciqual.versNutrition])
 * ou d'Open Food Facts pour le scan code-barres — jamais inventées par le modèle de
 * reconnaissance (voir CLAUDE.md, « Le calcul »).
 */
data class NutritionPer100g(
    val kcal: Int,
    val proteines: Double,
    val glucides: Double,
    val lipides: Double,
    val sucres: Double,
    val acidesGrasSatures: Double,
    val fibres: Double,
    val sodium: Double,
)
