package com.legoueix.objectifcalories.favoris.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "repas_favori")
data class RepasFavoriEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nom: String,
    // Chemin vers un fichier dans le stockage interne de l'app — pas de BLOB en base.
    val photoPath: String?,
)
