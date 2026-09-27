package com.legoueix.objectifcalories.openfoodfacts

import com.legoueix.objectifcalories.analysis.NutritionPer100g

data class ProduitScanne(
    val nom: String,
    val nutrition: NutritionPer100g,
)
