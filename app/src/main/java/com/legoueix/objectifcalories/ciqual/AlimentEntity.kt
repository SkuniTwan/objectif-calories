package com.legoueix.objectifcalories.ciqual

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.legoueix.objectifcalories.analysis.NutritionPer100g
import kotlinx.serialization.Serializable

/**
 * Entrée nutritionnelle locale au format Ciqual (code, libellé, groupe, valeurs pour
 * 100 g de partie comestible). Peuplée au premier lancement depuis
 * assets/ciqual_seed.json (voir [CiqualDatabase]) — export complet ANSES (3 484
 * aliments), récupéré via l'API Elasticsearch publique de ciqual.anses.fr
 * (endpoint `esearch/aliments/_search`, pas de fichier XLS/XML téléchargé).
 */
@Entity(tableName = "aliment")
@Serializable
data class AlimentEntity(
    @PrimaryKey val code: String,
    val libelle: String,
    val groupe: String,
    val kcal: Int,
    val proteines: Double,
    val glucides: Double,
    val lipides: Double,
    val sucres: Double,
    val acidesGrasSatures: Double,
    val fibres: Double,
    val sodium: Double,
)

fun AlimentEntity.versNutrition(): NutritionPer100g = NutritionPer100g(
    kcal = kcal,
    proteines = proteines,
    glucides = glucides,
    lipides = lipides,
    sucres = sucres,
    acidesGrasSatures = acidesGrasSatures,
    fibres = fibres,
    sodium = sodium,
)
