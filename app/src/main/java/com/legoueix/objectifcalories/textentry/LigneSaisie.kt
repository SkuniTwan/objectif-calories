package com.legoueix.objectifcalories.textentry

import com.legoueix.objectifcalories.ciqual.AlimentEntity
import java.util.UUID

data class LigneSaisie(
    val id: String = UUID.randomUUID().toString(),
    val recherche: String = "",
    val suggestions: List<AlimentEntity> = emptyList(),
    val alimentSelectionne: AlimentEntity? = null,
    val grammes: Int = 100,
)
