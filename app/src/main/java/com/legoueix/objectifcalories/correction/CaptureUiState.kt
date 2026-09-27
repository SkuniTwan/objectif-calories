package com.legoueix.objectifcalories.correction

import com.legoueix.objectifcalories.analysis.Confiance
import com.legoueix.objectifcalories.analysis.NutritionPer100g

sealed interface CaptureUiState {
    data object Idle : CaptureUiState
    data object Analyzing : CaptureUiState
    data class Result(
        val photo: ByteArray?,
        val items: List<ItemCorrige>,
        val confiance: Confiance,
        // "Reprendre" n'a de sens que pour un repas tout juste photographié/importé —
        // pas pour un repas saisi en texte, dicté, ou rappelé depuis les favoris (même
        // si ce favori a lui-même une photo).
        val peutReprendre: Boolean,
        // Pré-remplit le champ nom (ex. nom du favori rappelé) — vide pour laisser
        // l'utilisateur nommer librement, ou le nom par défaut s'appliquer à l'enregistrement.
        val nomInitial: String = "",
    ) : CaptureUiState
    data object Error : CaptureUiState
}

/**
 * Un aliment détecté, avec la valeur proposée par le modèle et la valeur
 * éventuellement corrigée par l'utilisateur. Les deux sont conservées séparément
 * pour préparer le futur apprentissage des corrections (voir CLAUDE.md,
 * `gramsSuggested` vs `grams` dans le modèle Room).
 *
 * [nutrition] vient toujours de Ciqual — saisie texte/vocale, favoris, ou photo/IA
 * (le nom détecté par le modèle est résolu vers une fiche Ciqual exacte, voir
 * [com.legoueix.objectifcalories.correction.MealCaptureViewModel.onPhotoCaptured])
 * — sauf pour le scan code-barres, qui vient d'Open Food Facts. [codeCiqual] n'est
 * renseigné que dans les cas où l'aliment vient de Ciqual.
 */
data class ItemCorrige(
    val nom: String,
    val codeCiqual: String? = null,
    val grammesSuggeres: Int,
    val grammesCorriges: Int,
    val nutrition: NutritionPer100g,
)

val ItemCorrige.kcal: Int
    get() = (nutrition.kcal * grammesCorriges) / 100
