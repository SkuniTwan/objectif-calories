package com.legoueix.objectifcalories.analysis

/**
 * Point d'accès unique à un modèle de reconnaissance de repas.
 * Ne jamais appeler l'API d'un fournisseur (Mistral, Gemini...) ailleurs que
 * dans une implémentation de cette interface — voir CLAUDE.md.
 */
interface MealAnalyzer {
    suspend fun analyze(image: ByteArray): AnalysisResult
}
