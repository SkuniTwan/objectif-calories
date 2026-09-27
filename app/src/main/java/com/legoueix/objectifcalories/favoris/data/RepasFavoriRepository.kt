package com.legoueix.objectifcalories.favoris.data

import android.content.Context
import com.legoueix.objectifcalories.ciqual.AlimentEntity
import com.legoueix.objectifcalories.favoris.AlimentFavori
import com.legoueix.objectifcalories.favoris.RepasFavori
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.io.File
import java.util.UUID

class RepasFavoriRepository(private val context: Context) {

    private val dao = FavorisDatabase.getInstance(context).repasFavoriDao()

    fun observerFavoris(): Flow<List<RepasFavori>> {
        return dao.observerTout()
            .map { liste ->
                liste.map { avecItems ->
                    RepasFavori(
                        id = avecItems.repas.id.toString(),
                        nom = avecItems.repas.nom,
                        photo = avecItems.repas.photoPath
                            ?.let { chemin -> File(chemin) }
                            ?.takeIf { it.exists() }
                            ?.readBytes(),
                        items = avecItems.items.map {
                            AlimentFavori(nom = it.nomAliment, codeCiqual = it.codeCiqual, grammes = it.grammes)
                        },
                    )
                }
            }
            .flowOn(Dispatchers.IO)
    }

    suspend fun enregistrer(nom: String, photo: ByteArray?, items: List<Pair<AlimentEntity, Int>>) {
        val photoPath = photo?.let { sauvegarderPhoto(it) }
        val idRepas = dao.insererRepas(RepasFavoriEntity(nom = nom, photoPath = photoPath))
        dao.insererItems(
            items.map { (aliment, grammes) ->
                RepasFavoriItemEntity(
                    repasFavoriId = idRepas,
                    nomAliment = aliment.libelle,
                    codeCiqual = aliment.code,
                    grammes = grammes,
                )
            },
        )
    }

    suspend fun supprimer(id: String) {
        val idLong = id.toLongOrNull() ?: return
        val chemin = dao.obtenirCheminPhoto(idLong)
        dao.supprimer(idLong)
        chemin?.let { File(it).delete() }
    }

    private fun sauvegarderPhoto(photo: ByteArray): String {
        val fichier = File(context.filesDir, "favori_${UUID.randomUUID()}.jpg")
        fichier.writeBytes(photo)
        return fichier.absolutePath
    }
}
