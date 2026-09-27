package com.legoueix.objectifcalories.activite

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.legoueix.objectifcalories.R
import com.legoueix.objectifcalories.activite.data.ActiviteEntity
import com.legoueix.objectifcalories.ui.AbandonButton
import com.legoueix.objectifcalories.ui.DateField
import com.legoueix.objectifcalories.ui.theme.Amber
import com.legoueix.objectifcalories.ui.theme.Charcoal
import com.legoueix.objectifcalories.ui.theme.Red
import com.legoueix.objectifcalories.ui.theme.Terracotta

private const val DUREE_MIN = 5f
private const val DUREE_MAX = 100f

// Les 5 sports les plus dictés, en accès direct ; le reste passe par "Tous les sports".
private val NOMS_SPORTS_VEDETTES = listOf(
    "Vélo modéré", "Marche", "Course à pied (10 km/h)", "Natation loisir", "Musculation modérée",
)
private val SPORTS_VEDETTES = NOMS_SPORTS_VEDETTES.mapNotNull { nom -> CatalogueActivites.liste.find { it.nom == nom } }

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ActiviteScreen(
    dernierPoidsKg: Double?,
    activitesAujourdhui: List<ActiviteEntity>,
    onEnregistrerActivite: (activite: TypeActivite, dureeMinutes: Int, dateHeure: Long) -> Unit,
    onSupprimerActivite: (id: Long) -> Unit,
    onAbandon: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var sportSelectionne by remember { mutableStateOf<TypeActivite?>(null) }
    var menuSportOuvert by remember { mutableStateOf(false) }
    var duree by remember { mutableStateOf(30) }
    var dateActiviteSelectionnee by remember { mutableStateOf(System.currentTimeMillis()) }

    val poidsUtilise = dernierPoidsKg ?: POIDS_PAR_DEFAUT_KG
    val sport = sportSelectionne
    val kcalEstimees = if (sport != null && duree > 0) kcalDepensees(sport.met, poidsUtilise, duree) else 0
    val peutValiderActivite = sport != null && duree > 0
    val kcalTotalAujourdhui = activitesAujourdhui.sumOf { it.kcal }

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
                        .background(Terracotta.copy(alpha = 0.16f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = Terracotta)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.activite_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.activite_votre_sport).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SPORTS_VEDETTES.forEach { activite ->
                        PuceSport(
                            texte = activite.nom,
                            selectionnee = sport?.nom == activite.nom,
                            onClick = { sportSelectionne = activite },
                        )
                    }
                    Box {
                        PuceSport(
                            texte = stringResource(R.string.activite_tous_les_sports) + " ›",
                            selectionnee = false,
                            onClick = { menuSportOuvert = true },
                        )
                        DropdownMenu(
                            expanded = menuSportOuvert,
                            onDismissRequest = { menuSportOuvert = false },
                        ) {
                            CatalogueActivites.liste.forEach { activite ->
                                DropdownMenuItem(
                                    text = { Text(text = "${activite.emoji} ${activite.nom}") },
                                    onClick = {
                                        sportSelectionne = activite
                                        menuSportOuvert = false
                                    },
                                )
                            }
                        }
                    }
                }
                if (dernierPoidsKg == null) {
                    Text(
                        text = stringResource(R.string.activite_poids_par_defaut_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = Amber,
                    )
                }
            }

            CarteDuree(
                poidsUtilise = poidsUtilise,
                duree = duree,
                kcalEstimees = kcalEstimees,
                onDureeChange = { duree = it },
            )

            if (activitesAujourdhui.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = stringResource(R.string.activite_seances_du_jour).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = stringResource(R.string.correction_kcal_format, kcalTotalAujourdhui),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Terracotta,
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        activitesAujourdhui.forEach { activite ->
                            SeanceLigneSwipeable(
                                activite = activite,
                                onSupprimer = { onSupprimerActivite(activite.id) },
                            )
                        }
                    }
                    Text(
                        text = stringResource(R.string.activite_glisser_supprimer),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
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
                    label = stringResource(R.string.activite_date_label),
                    dateHeure = dateActiviteSelectionnee,
                    onDateChange = { dateActiviteSelectionnee = it },
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(
                        onClick = { onEnregistrerActivite(sport!!, duree, dateActiviteSelectionnee) },
                        enabled = peutValiderActivite,
                        shape = RoundedCornerShape(percent = 50),
                        colors = ButtonDefaults.buttonColors(containerColor = Terracotta, contentColor = Color.White),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                    ) {
                        Text(text = stringResource(R.string.activite_validate), fontWeight = FontWeight.Bold)
                    }
                    AbandonButton(onClick = onAbandon)
                }
            }
        }
    }
}

@Composable
private fun PuceSport(texte: String, selectionnee: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(percent = 50),
        colors = CardDefaults.cardColors(
            containerColor = if (selectionnee) Charcoal else MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Text(
            text = texte,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (selectionnee) Color.White else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CarteDuree(
    poidsUtilise: Double,
    duree: Int,
    kcalEstimees: Int,
    onDureeChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Terracotta.copy(alpha = 0.14f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.activite_duree_section).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.activite_poids_actuel_format, formaterPoids(poidsUtilise)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row {
                    Text(
                        text = duree.toString(),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.alignByBaseline(),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.activite_minutes_label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.alignByBaseline(),
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = kcalEstimees.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Terracotta,
                    )
                    Text(
                        text = stringResource(R.string.activite_kcal_estimees_label),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Slider(
                value = duree.toFloat(),
                onValueChange = { onDureeChange(it.toInt()) },
                valueRange = DUREE_MIN..DUREE_MAX,
                track = { sliderState ->
                    val etendue = (sliderState.valueRange.endInclusive - sliderState.valueRange.start).coerceAtLeast(0.0001f)
                    val fraction = ((sliderState.value - sliderState.valueRange.start) / etendue).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(percent = 50))
                            .background(Color.White),
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
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Charcoal),
                    )
                },
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = stringResource(R.string.bilan_journalier_duree_format, DUREE_MIN.toInt()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "1 h 40",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PuceDuree(texte = stringResource(R.string.activite_duree_15), onClick = { onDureeChange(15) })
                PuceDuree(texte = stringResource(R.string.activite_duree_30), onClick = { onDureeChange(30) })
                PuceDuree(texte = stringResource(R.string.activite_duree_60), onClick = { onDureeChange(60) })
            }
        }
    }
}

@Composable
private fun PuceDuree(texte: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(percent = 50),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Text(
            text = texte,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeanceLigneSwipeable(activite: ActiviteEntity, onSupprimer: () -> Unit, modifier: Modifier = Modifier) {
    val etatSwipe = rememberSwipeToDismissBoxState(
        confirmValueChange = { valeur ->
            if (valeur == SwipeToDismissBoxValue.EndToStart) {
                onSupprimer()
                true
            } else {
                false
            }
        },
    )
    SwipeToDismissBox(
        state = etatSwipe,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Red)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color.White)
            }
        },
    ) {
        SeanceLigne(activite = activite)
    }
}

@Composable
private fun SeanceLigne(activite: ActiviteEntity, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Terracotta),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = activite.nom,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.bilan_journalier_duree_format, activite.dureeMinutes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 12.dp),
            )
            Text(
                text = activite.kcal.toString(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

private fun formaterPoids(poidsKg: Double): String =
    if (poidsKg == poidsKg.toInt().toDouble()) poidsKg.toInt().toString() else poidsKg.toString()
