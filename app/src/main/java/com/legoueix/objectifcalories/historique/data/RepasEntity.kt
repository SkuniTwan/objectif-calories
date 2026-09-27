package com.legoueix.objectifcalories.historique.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "repas")
data class RepasEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nom: String,
    val dateHeure: Long,
    val photoPath: String?,
)
