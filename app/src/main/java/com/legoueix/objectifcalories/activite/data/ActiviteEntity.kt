package com.legoueix.objectifcalories.activite.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activite")
data class ActiviteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nom: String,
    val met: Double,
    val dureeMinutes: Int,
    // Figées à l'enregistrement, comme les repas : le poids peut changer plus tard
    // sans modifier rétroactivement l'historique.
    val kcal: Int,
    val dateHeure: Long,
)
