package com.legoueix.objectifcalories.hydratation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.legoueix.objectifcalories.R
import com.legoueix.objectifcalories.hydratation.data.HydratationEntity
import com.legoueix.objectifcalories.ui.DateField
import com.legoueix.objectifcalories.ui.theme.Charcoal
import com.legoueix.objectifcalories.ui.theme.EauTexte
import com.legoueix.objectifcalories.ui.theme.SkyBlue
import com.legoueix.objectifcalories.ui.theme.SurfaceAlt
import com.legoueix.objectifcalories.ui.theme.Terracotta
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

// Chaque carré représente cette quantité (voir CarresProgression) ; 5 × 40 cl = objectif du jour.
// Non privé : réutilisé par le menu (AppDrawerViewModel) pour afficher la même progression.
private const val CL_PAR_CARRE = 40
private const val NOMBRE_CARRES = 5
const val OBJECTIF_CL = CL_PAR_CARRE * NOMBRE_CARRES

@Composable
fun HydratationScreen(
    viewModel: HydratationViewModel,
    onEnregistrer: (centilitres: Int, dateHeure: Long) -> Unit,
    onAbandon: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val totalClAujourdhui = uiState.totalClAujourdhui

    var quantite by remember { mutableStateOf(100) }
    var dateSelectionnee by remember { mutableStateOf(System.currentTimeMillis()) }

    Column(modifier = modifier.fillMaxSize().imePadding()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 24.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(SkyBlue.copy(alpha = 0.16f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = SkyBlue,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.eau_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
            }

            CarteProgressionEau(
                totalClAujourdhui = totalClAujourdhui,
                onCarreClique = { cible ->
                    val delta = cible - totalClAujourdhui
                    if (delta > 0) onEnregistrer(delta, System.currentTimeMillis())
                },
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.eau_ajouter_label).uppercase(Locale.FRENCH),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PuceQuantiteRapide(
                        titre = stringResource(R.string.eau_quick_glass_titre),
                        sousTitre = stringResource(R.string.eau_quick_glass_sous_titre),
                        selectionnee = quantite == 20,
                        onClick = { quantite = 20 },
                        modifier = Modifier.weight(1f),
                    )
                    PuceQuantiteRapide(
                        titre = stringResource(R.string.eau_quick_bottle_titre),
                        sousTitre = stringResource(R.string.eau_quick_bottle_sous_titre),
                        selectionnee = quantite == 50,
                        onClick = { quantite = 50 },
                        modifier = Modifier.weight(1f),
                    )
                    PuceQuantiteRapide(
                        titre = stringResource(R.string.eau_quick_large_bottle_titre),
                        sousTitre = stringResource(R.string.eau_quick_large_bottle_sous_titre),
                        selectionnee = quantite == 100,
                        onClick = { quantite = 100 },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            CarteQuantite(
                quantite = quantite,
                onMoins = { quantite = (quantite - 10).coerceAtLeast(0) },
                onPlus = { quantite += 10 },
            )

            if (uiState.entreesAujourdhui.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = stringResource(R.string.eau_deja_bu_label).uppercase(Locale.FRENCH),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        uiState.entreesAujourdhui.forEach { entree ->
                            LigneHydratation(entree = entree)
                        }
                    }
                }
            }
        }

        Surface(shadowElevation = 8.dp) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                DateField(
                    label = stringResource(R.string.eau_date_label),
                    dateHeure = dateSelectionnee,
                    onDateChange = { dateSelectionnee = it },
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(
                        onClick = { onEnregistrer(quantite, dateSelectionnee) },
                        enabled = quantite > 0,
                        shape = RoundedCornerShape(percent = 50),
                        colors = ButtonDefaults.buttonColors(containerColor = Terracotta, contentColor = Color.White),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                    ) {
                        Text(text = stringResource(R.string.eau_validate), fontWeight = FontWeight.Bold)
                    }
                    BoutonFermerEau(onClick = onAbandon, modifier = Modifier.size(52.dp))
                }
            }
        }
    }
}

@Composable
private fun BoutonFermerEau(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(SurfaceAlt)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = stringResource(R.string.abandon_content_description),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun CarteProgressionEau(
    totalClAujourdhui: Int,
    onCarreClique: (cibleCl: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val totalLitres = String.format(Locale.FRANCE, "%.1f", totalClAujourdhui / 100f)
    val objectifLitres = String.format(Locale.FRANCE, "%.1f", OBJECTIF_CL / 100f)
    val pourcentage = (totalClAujourdhui * 100f / OBJECTIF_CL).roundToInt()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SkyBlue.copy(alpha = 0.16f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.bilan_journalier_aujourdhui).uppercase(Locale.FRENCH),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = EauTexte,
                )
                Text(
                    text = stringResource(R.string.eau_objectif_pourcentage_format, pourcentage),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = EauTexte,
                )
            }
            Row {
                Text(
                    text = totalLitres,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.alignByBaseline(),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.eau_objectif_denominateur_format, objectifLitres),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.alignByBaseline(),
                )
            }
            CarresProgression(totalClAujourdhui = totalClAujourdhui, onCarreClique = onCarreClique)
            Text(
                text = stringResource(R.string.eau_carre_caption),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CarresProgression(
    totalClAujourdhui: Int,
    onCarreClique: (cibleCl: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(NOMBRE_CARRES) { index ->
            val debut = index * CL_PAR_CARRE
            val rempli = (totalClAujourdhui - debut).coerceIn(0, CL_PAR_CARRE)
            val fraction = rempli / CL_PAR_CARRE.toFloat()
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .clickable { onCarreClique(debut + CL_PAR_CARRE) },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction)
                        .background(SkyBlue),
                )
            }
        }
    }
}

@Composable
private fun PuceQuantiteRapide(
    titre: String,
    sousTitre: String,
    selectionnee: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selectionnee) Charcoal else MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Text(
                text = titre,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (selectionnee) Color.White else MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = sousTitre,
                style = MaterialTheme.typography.bodySmall,
                color = if (selectionnee) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CarteQuantite(
    quantite: Int,
    onMoins: () -> Unit,
    onPlus: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.eau_quantite_section_label).uppercase(Locale.FRENCH),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.eau_quantite_cl_format, quantite),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            BoutonRond(icone = Icons.Default.Remove, onClick = onMoins)
            Spacer(modifier = Modifier.width(10.dp))
            BoutonRond(icone = Icons.Default.Add, couleurFond = Charcoal, couleurIcone = Color.White, onClick = onPlus)
        }
    }
}

@Composable
private fun BoutonRond(
    icone: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    couleurFond: Color = Color.Transparent,
    couleurIcone: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(couleurFond)
            .then(
                if (couleurFond == Color.Transparent) {
                    Modifier.border(1.dp, MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                } else {
                    Modifier
                },
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(imageVector = icone, contentDescription = null, tint = couleurIcone, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun LigneHydratation(entree: HydratationEntity, modifier: Modifier = Modifier) {
    val formatterHeure = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val heure = remember(entree.dateHeure) {
        Instant.ofEpochMilli(entree.dateHeure).atZone(ZoneId.systemDefault()).format(formatterHeure)
    }
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(SkyBlue),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = libelleQuantite(entree.centilitres),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(R.string.eau_quantite_cl_format, entree.centilitres),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(end = 12.dp),
        )
        Text(
            text = heure,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun libelleQuantite(centilitres: Int): String = when (centilitres) {
    20 -> stringResource(R.string.eau_type_verre)
    50 -> stringResource(R.string.eau_type_bouteille)
    100 -> stringResource(R.string.eau_type_grande_bouteille)
    else -> stringResource(R.string.eau_type_generique)
}
