package com.legoueix.objectifcalories.openfoodfacts

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReponseOpenFoodFacts(
    val status: Int = 0,
    val product: ProduitDto? = null,
)

@Serializable
data class ProduitDto(
    @SerialName("product_name") val nom: String? = null,
    val nutriments: NutrimentsDto? = null,
)

@Serializable
data class NutrimentsDto(
    @SerialName("energy-kcal_100g") val kcal: Double? = null,
    @SerialName("proteins_100g") val proteines: Double? = null,
    @SerialName("carbohydrates_100g") val glucides: Double? = null,
    @SerialName("sugars_100g") val sucres: Double? = null,
    @SerialName("fat_100g") val lipides: Double? = null,
    @SerialName("saturated-fat_100g") val acidesGrasSatures: Double? = null,
    @SerialName("fiber_100g") val fibres: Double? = null,
    // Open Food Facts exprime le sodium en grammes pour 100 g ; converti en mg côté repository.
    @SerialName("sodium_100g") val sodiumGrammes: Double? = null,
)
