package com.legoueix.objectifcalories.poids

import android.graphics.Paint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.legoueix.objectifcalories.R
import com.legoueix.objectifcalories.poids.data.PoidsEntity
import com.legoueix.objectifcalories.ui.AbandonButton
import com.legoueix.objectifcalories.ui.DateField
import com.legoueix.objectifcalories.ui.theme.Charcoal
import com.legoueix.objectifcalories.ui.theme.DarkGreen
import com.legoueix.objectifcalories.ui.theme.Red
import com.legoueix.objectifcalories.ui.theme.SurfaceAlt
import com.legoueix.objectifcalories.ui.theme.Terracotta
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class PeriodePoids(val labelRes: Int) {
    MOIS(R.string.poids_periode_mois),
    SIX_MOIS(R.string.poids_periode_six_mois),
    TOUT(R.string.poids_periode_tout),
}

private const val PAS_POIDS_KG = 0.1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PoidsScreen(
    dernierPoidsKg: Double?,
    historique: List<PoidsEntity>,
    onEnregistrer: (poidsKg: Double, dateHeure: Long) -> Unit,
    onSupprimer: (id: Long) -> Unit,
    onAbandon: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val zoneId = remember { ZoneId.systemDefault() }
    var periode by remember { mutableStateOf(PeriodePoids.SIX_MOIS) }
    var poidsKg by remember(dernierPoidsKg) { mutableStateOf(dernierPoidsKg ?: 70.0) }
    var dateSelectionnee by remember { mutableStateOf(System.currentTimeMillis()) }

    val historiqueTrieDesc = remember(historique) { historique.sortedByDescending { it.dateHeure } }
    val aujourdhui = remember { LocalDate.now(zoneId) }
    val historiquePeriode = remember(historique, periode) {
        val debut = when (periode) {
            PeriodePoids.MOIS -> aujourdhui.minusMonths(1)
            PeriodePoids.SIX_MOIS -> aujourdhui.minusMonths(6)
            PeriodePoids.TOUT -> LocalDate.MIN
        }
        historique
            .filter { Instant.ofEpochMilli(it.dateHeure).atZone(zoneId).toLocalDate() >= debut }
            .sortedBy { it.dateHeure }
    }

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
                        .background(DarkGreen.copy(alpha = 0.16f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .border(2.dp, DarkGreen, CircleShape),
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.poids_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            if (historiquePeriode.isNotEmpty()) {
                CartePeseeEtEvolution(
                    historiquePeriode = historiquePeriode,
                    periode = periode,
                    onPeriodeChange = { periode = it },
                    zoneId = zoneId,
                )
            }

            CarteNouvellePesee(
                poidsKg = poidsKg,
                onPoidsChange = { poidsKg = it },
                onMoins = { poidsKg = arrondir((poidsKg - PAS_POIDS_KG).coerceAtLeast(0.0)) },
                onPlus = { poidsKg = arrondir(poidsKg + PAS_POIDS_KG) },
            )

            if (historiqueTrieDesc.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = stringResource(R.string.poids_historique_title).uppercase(Locale.FRENCH),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        historiqueTrieDesc.forEachIndexed { index, pesee ->
                            val precedent = historiqueTrieDesc.getOrNull(index + 1)
                            PoidsLigneSwipeable(
                                pesee = pesee,
                                delta = precedent?.let { pesee.poidsKg - it.poidsKg },
                                onSupprimer = { onSupprimer(pesee.id) },
                            )
                        }
                    }
                    Text(
                        text = stringResource(R.string.poids_glisser_supprimer),
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
                    label = stringResource(R.string.poids_date_label),
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
                        onClick = { onEnregistrer(poidsKg, dateSelectionnee) },
                        enabled = poidsKg > 0,
                        shape = RoundedCornerShape(percent = 50),
                        colors = ButtonDefaults.buttonColors(containerColor = Terracotta, contentColor = Color.White),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                    ) {
                        Text(text = stringResource(R.string.poids_validate), fontWeight = FontWeight.Bold)
                    }
                    AbandonButton(onClick = onAbandon)
                }
            }
        }
    }
}

private fun arrondir(poidsKg: Double): Double = Math.round(poidsKg * 10) / 10.0

private fun formaterPoids(poidsKg: Double): String = String.format(Locale.FRANCE, "%.1f", poidsKg)

@Composable
private fun CartePeseeEtEvolution(
    historiquePeriode: List<PoidsEntity>,
    periode: PeriodePoids,
    onPeriodeChange: (PeriodePoids) -> Unit,
    zoneId: ZoneId,
    modifier: Modifier = Modifier,
) {
    val formatterMois = remember { DateTimeFormatter.ofPattern("MMMM", Locale.FRENCH) }
    val premier = historiquePeriode.first()
    val dernier = historiquePeriode.last()
    val delta = dernier.poidsKg - premier.poidsKg
    val moisDepart = Instant.ofEpochMilli(premier.dateHeure).atZone(zoneId).toLocalDate()
        .format(formatterMois)
        .replaceFirstChar { it.uppercase() }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.poids_derniere_pesee).uppercase(Locale.FRENCH),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row {
                        Text(
                            text = formaterPoids(dernier.poidsKg),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.alignByBaseline(),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "kg",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.alignByBaseline(),
                        )
                    }
                }
                if (historiquePeriode.size > 1) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(R.string.poids_depuis_format, moisDepart).uppercase(Locale.FRENCH),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "${if (delta > 0) "+" else ""}${formaterPoids(delta)} kg",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (delta > 0) Red else DarkGreen,
                        )
                    }
                }
            }

            SelecteurPeriode(periode = periode, onPeriodeChange = onPeriodeChange)

            if (historiquePeriode.size >= 2) {
                PoidsChart(historique = historiquePeriode, zoneId = zoneId)
            }
        }
    }
}

@Composable
private fun SelecteurPeriode(periode: PeriodePoids, onPeriodeChange: (PeriodePoids) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceAlt, RoundedCornerShape(percent = 50))
            .padding(4.dp),
    ) {
        PeriodePoids.values().forEach { valeur ->
            val selectionnee = valeur == periode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(if (selectionnee) MaterialTheme.colorScheme.surface else Color.Transparent)
                    .clickable { onPeriodeChange(valeur) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(valeur.labelRes),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (selectionnee) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CarteNouvellePesee(
    poidsKg: Double,
    onPoidsChange: (Double) -> Unit,
    onMoins: () -> Unit,
    onPlus: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Terracotta.copy(alpha = 0.4f)),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.poids_nouvelle_pesee).uppercase(Locale.FRENCH),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ChampPoidsSaisissable(poidsKg = poidsKg, onPoidsChange = onPoidsChange)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "kg",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            BoutonRondPoids(icone = Icons.Default.Remove, onClick = onMoins)
            Spacer(modifier = Modifier.width(10.dp))
            BoutonRondPoids(icone = Icons.Default.Add, couleurFond = Charcoal, couleurIcone = Color.White, onClick = onPlus)
        }
    }
}

// Le poids reste modifiable au clavier en plus des boutons +/- : on touche le nombre pour le
// retaper directement, plus rapide qu'une série de clics pour un grand écart. Chaque frappe
// valide est propagée immédiatement au parent (pas seulement à la perte de focus : sinon un
// tap direct sur "Enregistrer" ou sur les boutons +/- sans passer par le clavier repartait de
// l'ancienne valeur, ou pire, écrasait un +/- entre-temps). L'affichage ne se resynchronise
// sur poidsKg que hors focus, pour ne pas couper la frappe en cours.
@Composable
private fun ChampPoidsSaisissable(poidsKg: Double, onPoidsChange: (Double) -> Unit, modifier: Modifier = Modifier) {
    val focusManager = LocalFocusManager.current
    var texte by remember { mutableStateOf(formaterPoids(poidsKg)) }
    var aLeFocus by remember { mutableStateOf(false) }

    LaunchedEffect(poidsKg, aLeFocus) {
        if (!aLeFocus) texte = formaterPoids(poidsKg)
    }

    BasicTextField(
        value = texte,
        onValueChange = { nouveauTexte ->
            val filtre = nouveauTexte.filter { it.isDigit() || it == ',' || it == '.' }
            texte = filtre
            filtre.replace(",", ".").toDoubleOrNull()?.takeIf { it > 0 }?.let(onPoidsChange)
        },
        modifier = modifier
            .width(90.dp)
            .onFocusChanged { etat -> aLeFocus = etat.isFocused },
        textStyle = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
        ),
        singleLine = true,
        cursorBrush = SolidColor(Terracotta),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
    )
}

@Composable
private fun BoutonRondPoids(
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
            .background(if (couleurFond == Color.Transparent) MaterialTheme.colorScheme.background else couleurFond)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(imageVector = icone, contentDescription = null, tint = couleurIcone, modifier = Modifier.size(18.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PoidsLigneSwipeable(
    pesee: PoidsEntity,
    delta: Double?,
    onSupprimer: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.poids_delete_content_description),
                    tint = Color.White,
                )
            }
        },
    ) {
        PoidsLigne(pesee = pesee, delta = delta, onSupprimer = onSupprimer)
    }
}

@Composable
private fun PoidsLigne(pesee: PoidsEntity, delta: Double?, onSupprimer: () -> Unit, modifier: Modifier = Modifier) {
    val formatterDate = remember { DateTimeFormatter.ofPattern("d MMMM", Locale.FRENCH) }
    val zoneId = remember { ZoneId.systemDefault() }
    val dateAffichee = remember(pesee.dateHeure) {
        Instant.ofEpochMilli(pesee.dateHeure).atZone(zoneId).toLocalDate().format(formatterDate)
    }

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
            Text(
                text = dateAffichee,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.poids_kg_format, formaterPoids(pesee.poidsKg)),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (delta != null) {
                Text(
                    text = "${if (delta > 0) "+" else ""}${formaterPoids(delta)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (delta > 0) Red else DarkGreen,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            // En plus du glisser-supprimer (pas toujours découvert par tout le monde) : une
            // icône visible fait la même chose en un tap direct.
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.poids_delete_content_description),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(start = 12.dp)
                    .size(20.dp)
                    .clickable(onClick = onSupprimer),
            )
        }
    }
}

private val LargeurAxeY = 34.dp

@Composable
private fun PoidsChart(historique: List<PoidsEntity>, zoneId: ZoneId, modifier: Modifier = Modifier) {
    val points = remember(historique) { historique.sortedBy { it.dateHeure } }
    val minPoids = points.minOf { it.poidsKg }
    val maxPoids = points.maxOf { it.poidsKg }
    val ecart = (maxPoids - minPoids).coerceAtLeast(0.5)
    val formatterDate = remember { DateTimeFormatter.ofPattern("MMM", Locale.FRENCH) }

    val paintAxeY = remember {
        Paint().apply {
            color = 0xFF9E9E9E.toInt()
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
        ) {
            paintAxeY.textSize = 11.sp.toPx()

            val largeurLabel = LargeurAxeY.toPx()
            val largeurGraphe = size.width - largeurLabel
            val pas = if (points.size > 1) largeurGraphe / (points.size - 1) else 0f

            listOf(minPoids to size.height, maxPoids to 0f).forEach { (valeurRepere, y) ->
                drawLine(
                    color = Color.LightGray,
                    start = Offset(largeurLabel, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx(),
                )
                drawContext.canvas.nativeCanvas.drawText(
                    formaterPoids(valeurRepere),
                    largeurLabel - 6.dp.toPx(),
                    y - (paintAxeY.descent() + paintAxeY.ascent()) / 2,
                    paintAxeY,
                )
            }

            val coordonnees = points.mapIndexed { index, point ->
                val ratio = (point.poidsKg - minPoids) / ecart
                val y = size.height - (ratio.toFloat() * size.height)
                Offset(largeurLabel + index * pas, y)
            }
            for (i in 0 until coordonnees.size - 1) {
                drawLine(
                    color = Terracotta,
                    start = coordonnees[i],
                    end = coordonnees[i + 1],
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
            coordonnees.forEachIndexed { index, point ->
                val estDernier = index == coordonnees.lastIndex
                drawCircle(
                    color = Terracotta,
                    radius = if (estDernier) 4.dp.toPx() else 2.5.dp.toPx(),
                    center = point,
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(start = LargeurAxeY), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = Instant.ofEpochMilli(points.first().dateHeure).atZone(zoneId).toLocalDate().format(formatterDate),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = Instant.ofEpochMilli(points.last().dateHeure).atZone(zoneId).toLocalDate().format(formatterDate),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
