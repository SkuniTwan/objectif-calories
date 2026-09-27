package com.legoueix.objectifcalories.gallery

import android.content.ContentUris
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Les [limite] photos les plus récentes de la galerie de l'appareil (MediaStore), triées du plus récent au plus ancien. */
suspend fun chargerPhotosRecentes(context: Context, limite: Int): List<Uri> = withContext(Dispatchers.IO) {
    val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    val uris = mutableListOf<Uri>()
    context.contentResolver.query(
        collection,
        arrayOf(MediaStore.Images.Media._ID),
        null,
        null,
        "${MediaStore.Images.Media.DATE_ADDED} DESC",
    )?.use { cursor ->
        // Le "LIMIT" en fin de sortOrder est rejeté par certains fournisseurs de contenu
        // ("Invalid token LIMIT") : on arrête simplement la lecture du curseur nous-mêmes.
        val colonneId = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        while (uris.size < limite && cursor.moveToNext()) {
            uris.add(ContentUris.withAppendedId(collection, cursor.getLong(colonneId)))
        }
    }
    uris
}

/**
 * Miniature décodée pour une vignette de la grille. `null` si la photo a été supprimée entre
 * la requête et le chargement, ou si le contenu est illisible — cas limites d'un accès à un
 * contenu externe, pas d'une erreur de programmation.
 */
suspend fun chargerMiniature(context: Context, uri: Uri, tailleCiblePx: Int): ImageBitmap? =
    withContext(Dispatchers.IO) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.contentResolver.loadThumbnail(uri, Size(tailleCiblePx, tailleCiblePx), null).asImageBitmap()
            } else {
                context.contentResolver.openInputStream(uri)?.use { flux ->
                    BitmapFactory.decodeStream(flux, null, BitmapFactory.Options().apply { inSampleSize = 4 })
                }?.asImageBitmap()
            }
        } catch (erreur: Exception) {
            null
        }
    }

/** Octets bruts de la photo sélectionnée, pour l'analyse — `null` en cas d'échec de lecture. */
suspend fun chargerPhotoComplete(context: Context, uri: Uri): ByteArray? = withContext(Dispatchers.IO) {
    try {
        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
    } catch (erreur: Exception) {
        null
    }
}
