package com.legoueix.objectifcalories.ciqual

import android.content.Context

class AlimentRepository(context: Context) {

    private val dao = CiqualDatabase.getInstance(context).alimentDao()

    suspend fun rechercher(texte: String): List<AlimentEntity> {
        if (texte.isBlank()) return emptyList()
        // Ciqual n'utilise jamais le ligature "œ" (toujours "oeuf", "boeuf"...) alors que la
        // reconnaissance vocale ou la saisie clavier peuvent la produire : sans cette
        // normalisation, chercher "œuf" ne renvoie jamais "Oeuf, cru".
        val texteNormalise = texte.trim().replace("œ", "oe").replace("Œ", "Oe")
        return dao.rechercher(texteNormalise)
    }

    suspend fun parCode(code: String): AlimentEntity? = dao.parCode(code)
}
