package com.legoueix.objectifcalories.ciqual

import android.content.Context
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Charge la table Ciqual complète embarquée dans les assets (voir [AlimentEntity]).
 */
object CiqualSeeder {

    private val json = Json { ignoreUnknownKeys = true }

    fun charger(context: Context): List<AlimentEntity> {
        val texte = context.assets.open("ciqual_seed.json").use { it.readBytes().decodeToString() }
        return json.decodeFromString(texte)
    }
}
