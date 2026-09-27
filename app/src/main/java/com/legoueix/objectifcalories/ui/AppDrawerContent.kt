package com.legoueix.objectifcalories.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.legoueix.objectifcalories.R
import com.legoueix.objectifcalories.hydratation.OBJECTIF_CL
import com.legoueix.objectifcalories.ui.theme.Charcoal
import com.legoueix.objectifcalories.ui.theme.Chevron
import com.legoueix.objectifcalories.ui.theme.DarkGreen
import com.legoueix.objectifcalories.ui.theme.SkyBlue
import com.legoueix.objectifcalories.ui.theme.Terracotta
import java.util.Locale

@Composable
fun AppDrawerContent(
    eauClAujourdhui: Int,
    kcalSeanceAujourdhui: Int,
    dernierPoidsKg: Double?,
    deltaPoidsKg: Double?,
    kcalConsommesSemaine: Int,
    onOpenEnregistrerRepas: () -> Unit,
    onOpenEau: () -> Unit,
    onOpenBilan: () -> Unit,
    onOpenCaloriesSport: () -> Unit,
    onOpenPoids: () -> Unit,
    onOpenActivitesSportives: () -> Unit,
    onOpenMentionsLegales: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.background,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            CarteEnregistrerRepas(onClick = onOpenEnregistrerRepas)

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.menu_suivre).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CarteStat(
                        icone = Icons.Default.WaterDrop,
                        couleurFond = SkyBlue.copy(alpha = 0.16f),
                        couleurIcone = SkyBlue,
                        titre = stringResource(R.string.menu_eau),
                        valeur = stringResource(
                            R.string.menu_eau_format,
                            formaterLitres(eauClAujourdhui),
                            formaterLitres(OBJECTIF_CL),
                        ),
                        onClick = onOpenEau,
                        modifier = Modifier.weight(1f),
                    )
                    CarteStat(
                        icone = Icons.Default.Timer,
                        couleurFond = Terracotta.copy(alpha = 0.16f),
                        couleurIcone = Terracotta,
                        titre = stringResource(R.string.menu_seance),
                        valeur = stringResource(R.string.menu_seance_format, kcalSeanceAujourdhui),
                        onClick = onOpenActivitesSportives,
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CarteStat(
                        icone = Icons.Default.MonitorWeight,
                        couleurFond = DarkGreen.copy(alpha = 0.14f),
                        couleurIcone = DarkGreen,
                        titre = stringResource(R.string.menu_poids),
                        valeur = formaterPoidsMenu(dernierPoidsKg, deltaPoidsKg),
                        onClick = onOpenPoids,
                        modifier = Modifier.weight(1f),
                    )
                    CarteStatSombre(
                        icone = Icons.Default.BarChart,
                        titre = stringResource(R.string.menu_bilan),
                        valeur = stringResource(R.string.menu_bilan_format, kcalConsommesSemaine),
                        onClick = onOpenBilan,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.menu_consulter).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                CartePillConsulter(
                    icone = Icons.Default.LocalFireDepartment,
                    titre = stringResource(R.string.menu_calories_sport),
                    onClick = onOpenCaloriesSport,
                )
            }

            Text(
                text = stringResource(R.string.menu_mentions_legales),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenMentionsLegales)
                    .padding(vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun CarteEnregistrerRepas(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Terracotta),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.menu_enregistrer_repas),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Text(
                    text = stringResource(R.string.menu_enregistrer_repas_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                )
            }
            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White)
        }
    }
}

@Composable
private fun CarteStat(
    icone: ImageVector,
    couleurFond: Color,
    couleurIcone: Color,
    titre: String,
    valeur: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = couleurFond),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.White.copy(alpha = 0.55f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(imageVector = icone, contentDescription = null, tint = couleurIcone)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = titre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = valeur,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                // Réserve toujours la place de 2 lignes : sinon une valeur qui passe sur 2
                // lignes (ex. "Semaine · xxx kcal") rend sa card plus haute que les 3 autres.
                minLines = 2,
            )
        }
    }
}

@Composable
private fun CarteStatSombre(
    icone: ImageVector,
    titre: String,
    valeur: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Charcoal),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.White.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(imageVector = icone, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = titre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Text(
                text = valeur,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                minLines = 2,
            )
        }
    }
}

@Composable
private fun CartePillConsulter(icone: ImageVector, titre: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(percent = 50),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(imageVector = icone, contentDescription = null, tint = Terracotta)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = titre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Chevron)
        }
    }
}

private fun formaterLitres(centilitres: Int): String =
    String.format(Locale.FRANCE, "%.1f", centilitres / 100f)

@Composable
private fun formaterPoidsMenu(dernierPoidsKg: Double?, deltaPoidsKg: Double?): String {
    if (dernierPoidsKg == null) return stringResource(R.string.menu_poids_vide)
    val poidsTexte = String.format(Locale.FRANCE, "%.1f", dernierPoidsKg)
    if (deltaPoidsKg == null) return stringResource(R.string.menu_poids_format, poidsTexte)
    val signe = if (deltaPoidsKg > 0) "+" else "-"
    val deltaTexte = signe + String.format(Locale.FRANCE, "%.1f", kotlin.math.abs(deltaPoidsKg))
    return stringResource(R.string.menu_poids_avec_delta_format, poidsTexte, deltaTexte)
}
