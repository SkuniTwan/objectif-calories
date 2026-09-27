package com.legoueix.objectifcalories.historique.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "repas_item",
    foreignKeys = [
        ForeignKey(
            entity = RepasEntity::class,
            parentColumns = ["id"],
            childColumns = ["repasId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("repasId")],
)
data class RepasItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val repasId: Long,
    val nom: String,
    // Absent pour les aliments qui ne passent pas (encore) par Ciqual : photo IA
    // (table simulée) et scan code-barres (Open Food Facts).
    val codeCiqual: String?,
    val grammesSuggeres: Int,
    val grammes: Int,
    // Valeurs pour 100 g, figées à l'enregistrement — ne jamais recalculer depuis
    // Ciqual à l'affichage, un journal doit rester immuable (voir CLAUDE.md).
    val kcal: Int,
    val proteines: Double,
    val glucides: Double,
    val lipides: Double,
    val sucres: Double,
    val acidesGrasSatures: Double,
    val fibres: Double,
    val sodium: Double,
)
