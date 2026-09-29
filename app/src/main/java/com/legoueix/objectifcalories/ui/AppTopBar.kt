package com.legoueix.objectifcalories.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.legoueix.objectifcalories.R
import com.legoueix.objectifcalories.ui.theme.Charcoal
import com.legoueix.objectifcalories.ui.theme.OffWhite

// Image affichée intacte (photo d'assiette comprise) — recadrer/zoomer pour agrandir
// le bouton menu mangeait la photo (déjà essayé, refusé). À la place : un vrai bouton
// ☰ (vectoriel, donc net à toute taille) dessiné par-dessus, sur un disque qui masque
// le minuscule hamburger déjà présent dans l'image. L'asset a aussi une bande de marge
// ajoutée en haut (837 → 927 px de haut, largeur inchangée) pour que "Objectif
// Calories" ne soit plus collé au bord et paraisse plus bas dans le bandeau.
private const val RATIO_BANNIERE = 1879f / 927f

@Composable
fun AppTopBar(onOpenMenu: () -> Unit, modifier: Modifier = Modifier) {
    val descriptionMenu = stringResource(R.string.top_bar_open_menu)
    Surface(modifier = modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.background) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
        ) {
            Image(
                painter = painterResource(R.drawable.header_banner),
                contentDescription = stringResource(R.string.app_name),
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(RATIO_BANNIERE),
            )
            IconButton(
                onClick = onOpenMenu,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .size(56.dp)
                    .background(OffWhite, CircleShape)
                    .semantics { contentDescription = descriptionMenu },
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = null,
                    tint = Charcoal,
                    modifier = Modifier.size(34.dp),
                )
            }
        }
    }
}
