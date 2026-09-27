package com.legoueix.objectifcalories.historique.data

import android.content.Context
import com.legoueix.objectifcalories.analysis.NutritionPer100g
import com.legoueix.objectifcalories.historique.AlimentEnregistre
import com.legoueix.objectifcalories.historique.Repas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.io.File
import java.util.UUID

class RepasRepository(private val context: Context) {

    private val dao = RepasDatabase.getInstance(context).repasDao()

    fun observerHistorique(): Flow<List<Repas>> {
        return dao.observerTout()
            .map { liste ->
                liste.map { avecItems ->
                    Repas(
                        id = avecItems.repas.id,
                        nom = avecItems.repas.nom,
                        dateHeure = avecItems.repas.dateHeure,
                        photo = avecItems.repas.photoPath
                            ?.let { chemin -> File(chemin) }
                            ?.takeIf { it.exists() }
                            ?.readBytes(),
                        items = avecItems.items.map {
                            AlimentEnregistre(
                                nom = it.nom,
                                codeCiqual = it.codeCiqual,
                                grammesSuggeres = it.grammesSuggeres,
                                grammes = it.grammes,
                                nutrition = NutritionPer100g(
                                    kcal = it.kcal,
                                    proteines = it.proteines,
                                    glucides = it.glucides,
                                    lipides = it.lipides,
                                    sucres = it.sucres,
                                    acidesGrasSatures = it.acidesGrasSatures,
                                    fibres = it.fibres,
                                    sodium = it.sodium,
                                ),
                            )
                        },
                    )
                }
            }
            .flowOn(Dispatchers.IO)
    }

    suspend fun enregistrer(nom: String, dateHeure: Long, photo: ByteArray?, items: List<AlimentEnregistre>) {
        val photoPath = photo?.let { sauvegarderPhoto(it) }
        val idRepas = dao.insererRepas(RepasEntity(nom = nom, dateHeure = dateHeure, photoPath = photoPath))
        dao.insererItems(
            items.map { item ->
                RepasItemEntity(
                    repasId = idRepas,
                    nom = item.nom,
                    codeCiqual = item.codeCiqual,
                    grammesSuggeres = item.grammesSuggeres,
                    grammes = item.grammes,
                    kcal = item.nutrition.kcal,
                    proteines = item.nutrition.proteines,
                    glucides = item.nutrition.glucides,
                    lipides = item.nutrition.lipides,
                    sucres = item.nutrition.sucres,
                    acidesGrasSatures = item.nutrition.acidesGrasSatures,
                    fibres = item.nutrition.fibres,
                    sodium = item.nutrition.sodium,
                )
            },
        )
    }

    suspend fun supprimer(id: Long) {
        val chemin = dao.obtenirCheminPhoto(id)
        dao.supprimer(id)
        chemin?.let { File(it).delete() }
    }

    private fun sauvegarderPhoto(photo: ByteArray): String {
        val fichier = File(context.filesDir, "repas_${UUID.randomUUID()}.jpg")
        fichier.writeBytes(photo)
        return fichier.absolutePath
    }
}
