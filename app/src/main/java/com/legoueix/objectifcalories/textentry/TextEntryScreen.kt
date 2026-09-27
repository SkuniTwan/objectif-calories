package com.legoueix.objectifcalories.textentry

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.legoueix.objectifcalories.R
import com.legoueix.objectifcalories.ciqual.AlimentEntity
import com.legoueix.objectifcalories.ui.AbandonButton
import com.legoueix.objectifcalories.ui.theme.Charcoal
import com.legoueix.objectifcalories.ui.theme.SurfaceAlt
import com.legoueix.objectifcalories.ui.theme.Terracotta
import com.legoueix.objectifcalories.ui.theme.TextSecondary
import com.legoueix.objectifcalories.ui.theme.TextTertiary

private const val GRAMMES_MIN = 0f
private const val GRAMMES_MAX = 500f
private const val PAS_GRAMMES = 10

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TextEntryScreen(
    viewModel: TextEntryViewModel,
    onValidated: (List<Pair<AlimentEntity, Int>>) -> Unit,
    onAbandon: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lignes by viewModel.lignes.collectAsState()
    val ligneRecherche = lignes.firstOrNull { it.alimentSelectionne == null } ?: LigneSaisie()
    val lignesConfirmees = lignes.filter { it.alimentSelectionne != null }
    val peutValider = lignesConfirmees.isNotEmpty()

    Column(modifier = modifier.fillMaxSize().imePadding()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(SurfaceAlt, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(imageVector = Icons.Default.Notes, contentDescription = null, tint = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.text_entry_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    BarreRecherche(
                        texte = ligneRecherche.recherche,
                        onTexteChange = { texte -> viewModel.onRechercheChangee(ligneRecherche.id, texte) },
                    )
                    if (ligneRecherche.suggestions.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            ligneRecherche.suggestions.forEach { suggestion ->
                                PuceSuggestion(
                                    libelle = suggestion.libelle,
                                    onClick = { viewModel.onAlimentChoisi(ligneRecherche.id, suggestion) },
                                )
                            }
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.text_entry_votre_repas).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = if (lignesConfirmees.size == 1) {
                            stringResource(R.string.text_entry_aliment_singulier, lignesConfirmees.size)
                        } else {
                            stringResource(R.string.text_entry_aliment_pluriel, lignesConfirmees.size)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            items(items = lignesConfirmees, key = { it.id }) { ligne ->
                CarteAlimentConfirme(
                    ligne = ligne,
                    onGrammesChange = { grammes -> viewModel.onGrammesChanges(ligne.id, grammes) },
                    onSupprimer = { viewModel.onSupprimerLigne(ligne.id) },
                )
            }
        }

        Surface(shadowElevation = 8.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = {
                        val items = lignesConfirmees.mapNotNull { ligne ->
                            ligne.alimentSelectionne?.let { aliment -> aliment to ligne.grammes }
                        }
                        onValidated(items)
                    },
                    enabled = peutValider,
                    shape = RoundedCornerShape(percent = 50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Terracotta,
                        contentColor = Color.White,
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                ) {
                    Text(text = stringResource(R.string.text_entry_validate), fontWeight = FontWeight.Bold)
                }
                AbandonButton(onClick = onAbandon)
            }
        }
    }
}

@Composable
private fun BarreRecherche(texte: String, onTexteChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = texte,
        onValueChange = onTexteChange,
        placeholder = {
            Text(
                text = stringResource(R.string.text_entry_search_placeholder),
                color = TextTertiary,
            )
        },
        leadingIcon = {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, TextTertiary, CircleShape),
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(percent = 50),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Color.Transparent,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun PuceSuggestion(libelle: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(percent = 50),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Text(
            text = libelle,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CarteAlimentConfirme(
    ligne: LigneSaisie,
    onGrammesChange: (Int) -> Unit,
    onSupprimer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val aliment = ligne.alimentSelectionne ?: return
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = aliment.libelle.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onSupprimer, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.text_entry_remove_line),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                BoutonRondEtape(
                    icone = Icons.Default.Remove,
                    onClick = { onGrammesChange((ligne.grammes - PAS_GRAMMES).coerceAtLeast(0)) },
                )
                Slider(
                    value = ligne.grammes.toFloat(),
                    onValueChange = { onGrammesChange(it.toInt()) },
                    valueRange = GRAMMES_MIN..GRAMMES_MAX,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp),
                    track = { sliderState ->
                        val etendue = (sliderState.valueRange.endInclusive - sliderState.valueRange.start)
                            .coerceAtLeast(0.0001f)
                        val fraction = ((sliderState.value - sliderState.valueRange.start) / etendue).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(percent = 50))
                                .background(MaterialTheme.colorScheme.background),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(fraction)
                                    .background(Terracotta),
                            )
                        }
                    },
                    thumb = {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                        )
                    },
                )
                Text(
                    text = stringResource(R.string.correction_grams_format, ligne.grammes),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(end = 8.dp),
                )
                BoutonRondEtape(
                    icone = Icons.Default.Add,
                    couleurFond = Charcoal,
                    couleurIcone = Color.White,
                    onClick = { onGrammesChange((ligne.grammes + PAS_GRAMMES).coerceAtMost(GRAMMES_MAX.toInt())) },
                )
            }
        }
    }
}

@Composable
private fun BoutonRondEtape(
    icone: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    couleurFond: Color = Color.Transparent,
    couleurIcone: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(if (couleurFond == Color.Transparent) MaterialTheme.colorScheme.background else couleurFond)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(imageVector = icone, contentDescription = null, tint = couleurIcone, modifier = Modifier.size(16.dp))
    }
}
