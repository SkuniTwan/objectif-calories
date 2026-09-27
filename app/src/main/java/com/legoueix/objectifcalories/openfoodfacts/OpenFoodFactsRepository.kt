package com.legoueix.objectifcalories.openfoodfacts

import com.legoueix.objectifcalories.analysis.NutritionPer100g
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Appelé directement depuis le téléphone de l'utilisateur (pas de relais backend) :
 * Open Food Facts ne demande pas de clé API, et la limite de 15 req/min s'applique par
 * IP, donc par utilisateur — largement suffisant pour un scan occasionnel.
 */
class OpenFoodFactsRepository {

    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun rechercherParCodeBarres(codeBarres: String): ProduitScanne? = withContext(Dispatchers.IO) {
        runCatching {
            val requete = Request.Builder()
                .url(
                    "https://world.openfoodfacts.org/api/v2/product/$codeBarres.json" +
                        "?fields=product_name,nutriments",
                )
                .header("User-Agent", "ObjectifCalories/0.1 (Android) - contact via github.com/legoueix")
                .build()

            client.newCall(requete).execute().use { reponse ->
                if (!reponse.isSuccessful) return@use null
                val corps = reponse.body?.string() ?: return@use null
                val donnees = json.decodeFromString<ReponseOpenFoodFacts>(corps)
                val produit = donnees.product
                val nom = produit?.nom?.trim()
                if (donnees.status != 1 || nom.isNullOrBlank()) {
                    null
                } else {
                    ProduitScanne(
                        nom = nom,
                        nutrition = NutritionPer100g(
                            kcal = (produit.nutriments?.kcal ?: 0.0).toInt(),
                            proteines = produit.nutriments?.proteines ?: 0.0,
                            glucides = produit.nutriments?.glucides ?: 0.0,
                            lipides = produit.nutriments?.lipides ?: 0.0,
                            sucres = produit.nutriments?.sucres ?: 0.0,
                            acidesGrasSatures = produit.nutriments?.acidesGrasSatures ?: 0.0,
                            fibres = produit.nutriments?.fibres ?: 0.0,
                            sodium = (produit.nutriments?.sodiumGrammes ?: 0.0) * 1000,
                        ),
                    )
                }
            }
        }.getOrNull()
    }
}
