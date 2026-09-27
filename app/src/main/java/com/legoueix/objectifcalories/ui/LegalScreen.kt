package com.legoueix.objectifcalories.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.legoueix.objectifcalories.BuildConfig
import com.legoueix.objectifcalories.R
import com.legoueix.objectifcalories.ui.theme.Terracotta

private const val URL_POLITIQUE_CONFIDENTIALITE = "https://skunitwan.github.io/objectif-calories/"
private const val URL_OPEN_FOOD_FACTS = "https://openfoodfacts.org"
private const val URL_CIQUAL = "https://ciqual.anses.fr"

@Composable
fun LegalScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    fun ouvrirLien(url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.legal_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = stringResource(R.string.legal_version_format, BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        CarteMention(
            titre = stringResource(R.string.legal_donnees_titre),
            texte = stringResource(R.string.legal_donnees_texte),
            lienTexte = stringResource(R.string.legal_politique_confidentialite),
            onLienClick = { ouvrirLien(URL_POLITIQUE_CONFIDENTIALITE) },
        )

        CarteMention(
            titre = stringResource(R.string.legal_open_food_facts_titre),
            texte = stringResource(R.string.legal_open_food_facts_texte),
            lienTexte = stringResource(R.string.legal_open_food_facts_lien),
            onLienClick = { ouvrirLien(URL_OPEN_FOOD_FACTS) },
        )

        CarteMention(
            titre = stringResource(R.string.legal_ciqual_titre),
            texte = stringResource(R.string.legal_ciqual_texte),
            lienTexte = stringResource(R.string.legal_ciqual_lien),
            onLienClick = { ouvrirLien(URL_CIQUAL) },
        )
    }
}

@Composable
private fun CarteMention(
    titre: String,
    texte: String,
    lienTexte: String,
    onLienClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = titre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = texte,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = lienTexte,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Terracotta,
                modifier = Modifier.clickable(onClick = onLienClick),
            )
        }
    }
}
