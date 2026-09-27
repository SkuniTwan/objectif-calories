package com.legoueix.objectifcalories.favoris.data

import androidx.room.Embedded
import androidx.room.Relation

data class RepasFavoriAvecItems(
    @Embedded val repas: RepasFavoriEntity,
    @Relation(parentColumn = "id", entityColumn = "repasFavoriId")
    val items: List<RepasFavoriItemEntity>,
)
