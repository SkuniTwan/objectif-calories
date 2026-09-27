package com.legoueix.objectifcalories.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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

// Ratio réel du bandeau (1879 × 837 px) : affiché intact, sans recadrage, pour ne
// perdre ni le logo ni la photo — demandé explicitement tel quel malgré la place
// que ça prend sur un écran de téléphone.
private const val RATIO_BANNIERE = 1879f / 837f

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
            // Le bouton menu est déjà dessiné dans l'image (coin haut-gauche) : cette zone
            // transparente le rend cliquable au même endroit sans le dessiner une deuxième fois.
            IconButton(
                onClick = onOpenMenu,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 4.dp)
                    .size(52.dp)
                    .semantics { contentDescription = descriptionMenu },
            ) {}
        }
    }
}
