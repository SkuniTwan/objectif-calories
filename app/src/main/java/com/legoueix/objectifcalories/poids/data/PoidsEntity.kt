package com.legoueix.objectifcalories.poids.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "poids")
data class PoidsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val poidsKg: Double,
    val dateHeure: Long,
)
