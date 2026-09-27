package com.legoueix.objectifcalories.favoris

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.legoueix.objectifcalories.R
import com.legoueix.objectifcalories.ui.AbandonButton
import com.legoueix.objectifcalories.ui.PhotoThumbnail
import com.legoueix.objectifcalories.ui.theme.Charcoal
import com.legoueix.objectifcalories.ui.theme.Gold
import com.legoueix.objectifcalories.ui.theme.Red
import com.legoueix.objectifcalories.ui.theme.SurfaceAlt
import com.legoueix.objectifcalories.ui.theme.Terracotta

@Composable
fun FavorisScreen(
    favoris: List<FavoriAffiche>,
    onRepasChoisi: (RepasFavori) -> Unit,
    onSupprimer: (RepasFavori) -> Unit,
    onAjouterRepas: () -> Unit,
    onAbandon: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var repasASupprimer by remember { mutableStateOf<RepasFavori?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Gold.copy(alpha = 0.16f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Gold)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.favoris_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                text = stringResource(R.string.favoris_instruction).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(items = favoris, key = { it.favori.id }) { favoriAffiche ->
                FavoriCard(
                    favoriAffiche = favoriAffiche,
                    onClick = { onRepasChoisi(favoriAffiche.favori) },
                    onSupprimer = { repasASupprimer = favoriAffiche.favori },
                )
            }
        }

        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = onAjouterRepas,
                    shape = RoundedCornerShape(percent = 50),
                    colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = Color.White),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                ) {
                    Text(text = stringResource(R.string.favoris_ajouter), fontWeight = FontWeight.Bold)
                }
                AbandonButton(onClick = onAbandon)
            }
        }
    }

    val cible = repasASupprimer
    if (cible != null) {
        AlertDialog(
            onDismissRequest = { repasASupprimer = null },
            title = { Text(text = stringResource(R.string.favoris_supprimer_titre)) },
            text = { Text(text = stringResource(R.string.favoris_supprimer_message, cible.nom)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSupprimer(cible)
                        repasASupprimer = null
                    },
                ) {
                    Text(text = stringResource(R.string.favoris_supprimer_confirmer), color = Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { repasASupprimer = null }) {
                    Text(text = stringResource(R.string.favoris_supprimer_annuler))
                }
            },
        )
    }
}

@Composable
private fun FavoriCard(
    favoriAffiche: FavoriAffiche,
    onClick: () -> Unit,
    onSupprimer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val repas = favoriAffiche.favori
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PhotoThumbnail(photo = repas.photo)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = repas.nom,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = repas.items.joinToString(" · ") { "${it.nom} ${it.grammes} g" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
                Row(modifier = Modifier.padding(top = 4.dp)) {
                    Text(
                        text = stringResource(R.string.correction_kcal_format, favoriAffiche.kcalTotal),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Terracotta,
                    )
                    Text(
                        text = " · " + if (repas.items.size == 1) {
                            stringResource(R.string.text_entry_aliment_singulier, repas.items.size)
                        } else {
                            stringResource(R.string.text_entry_aliment_pluriel, repas.items.size)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Terracotta.copy(alpha = 0.16f), CircleShape)
                    .clickable(onClick = onSupprimer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.favori_delete_content_description),
                    tint = Terracotta,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
