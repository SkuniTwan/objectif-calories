package com.legoueix.objectifcalories.favoris.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "repas_favori_item",
    foreignKeys = [
        ForeignKey(
            entity = RepasFavoriEntity::class,
            parentColumns = ["id"],
            childColumns = ["repasFavoriId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("repasFavoriId")],
)
data class RepasFavoriItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val repasFavoriId: Long,
    val nomAliment: String,
    // Code Ciqual exact : permet de retrouver les vraies valeurs nutritionnelles au
    // rappel du favori, plutôt que de re-matcher par nom (voir MealCaptureViewModel).
    val codeCiqual: String,
    val grammes: Int,
)
