package com.legoueix.objectifcalories.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.legoueix.objectifcalories.ui.theme.SageGreen

/**
 * Miniature d'un repas (favori ou historique) : la photo si elle existe, sinon une
 * icône par défaut — jamais de miniature vide.
 */
@Composable
fun PhotoThumbnail(photo: ByteArray?, modifier: Modifier = Modifier, taille: Dp = 56.dp) {
    if (photo != null) {
        val bitmap = remember(photo) {
            BitmapFactory.decodeByteArray(photo, 0, photo.size).asImageBitmap()
        }
        Image(
            bitmap = bitmap,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(taille)
                .clip(RoundedCornerShape(12.dp)),
        )
    } else {
        Surface(
            modifier = modifier.size(taille),
            shape = RoundedCornerShape(12.dp),
            color = SageGreen.copy(alpha = 0.14f),
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(text = "🍽️", fontSize = 22.sp)
            }
        }
    }
}
