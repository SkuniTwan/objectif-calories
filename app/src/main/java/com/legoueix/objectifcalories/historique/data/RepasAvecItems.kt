package com.legoueix.objectifcalories.historique.data

import androidx.room.Embedded
import androidx.room.Relation

data class RepasAvecItems(
    @Embedded val repas: RepasEntity,
    @Relation(parentColumn = "id", entityColumn = "repasId")
    val items: List<RepasItemEntity>,
)
