package com.legoueix.objectifcalories.hydratation.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hydratation")
data class HydratationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateHeure: Long,
    val centilitres: Int,
)
