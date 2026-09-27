package com.legoueix.objectifcalories.bilan.hebdo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.legoueix.objectifcalories.R
import com.legoueix.objectifcalories.bilan.IndicateurEau
import com.legoueix.objectifcalories.bilan.JourAgregat
import com.legoueix.objectifcalories.ui.theme.EauTexte
import com.legoueix.objectifcalories.ui.theme.LightGreen
import com.legoueix.objectifcalories.ui.theme.LightOrange
import com.legoueix.objectifcalories.ui.theme.LightYellow
import com.legoueix.objectifcalories.ui.theme.SkyBlue
import com.legoueix.objectifcalories.ui.theme.SportTexte
import com.legoueix.objectifcalories.ui.theme.Terracotta
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JourTextStyle
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private val CouleurGlucides = LightOrange
private val CouleurProteines = LightGreen
private val CouleurLipides = LightYellow

private val formatterJourMois = DateTimeFormatter.ofPattern("d MMM", Locale.FRENCH)

private fun libelleSemaine(debutSemaine: LocalDate): String {
    val finSemaine = debutSemaine.plusDays(6)
    return if (debutSemaine.month == finSemaine.month) {
        "${debutSemaine.dayOfMonth} - ${finSemaine.format(formatterJourMois)}"
    } else {
        "${debutSemaine.format(formatterJourMois)} - ${finSemaine.format(formatterJourMois)}"
    }
}

@Composable
fun BilanHebdoScreen(
    viewModel: BilanHebdoViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    val glucidesG = uiState.jours.sumOf { it.glucidesG }
    val proteinesG = uiState.jours.sumOf { it.proteinesG }
    val lipidesG = uiState.jours.sumOf { it.lipidesG }
    val moyenneKcalParJour = if (uiState.jours.isEmpty()) {
        0
    } else {
        (uiState.totalKcal.toDouble() / uiState.jours.size).roundToInt()
    }
    val nombreActivites = uiState.totalNombreActivites
    val resumeSport = if (nombreActivites == 0) {
        null
    } else if (nombreActivites == 1) {
        stringResource(R.string.bilan_sport_resume_singulier, nombreActivites, uiState.totalDureeActivitesMinutes)
    } else {
        stringResource(R.string.bilan_sport_resume_pluriel, nombreActivites, uiState.totalDureeActivitesMinutes)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                var glissementCumule = 0f
                detectHorizontalDragGestures(
                    onDragStart = { glissementCumule = 0f },
                    onHorizontalDrag = { _, dragAmount -> glissementCumule += dragAmount },
                    onDragEnd = {
                        if (abs(glissementCumule) > 80f) {
                            if (glissementCumule < 0) viewModel.onSemaineSuivante() else viewModel.onSemainePrecedente()
                        }
                    },
                )
            }
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        NavigationSemaine(
            debutSemaine = uiState.debutSemaine,
            onSemainePrecedente = viewModel::onSemainePrecedente,
            onSemaineSuivante = viewModel::onSemaineSuivante,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(
                        R.string.bilan_hebdo_calories_range_format,
                        libelleSemaine(uiState.debutSemaine),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.correction_kcal_format, uiState.totalKcal),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.bilan_moyenne_kcal_jour_format, moyenneKcalParJour),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Terracotta,
                )
                BarresCaloriesSemaine(
                    jours = uiState.jours,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TuileHebdo(
                titre = stringResource(R.string.bilan_journalier_eau_label),
                couleurFond = SkyBlue.copy(alpha = 0.16f),
                couleurTitre = EauTexte,
                valeur = stringResource(
                    R.string.bilan_journalier_eau_format,
                    String.format(Locale.FRANCE, "%.1f", uiState.totalEauCl / 100f),
                ),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                IndicateurEau(centilitres = uiState.totalEauCl, centilitresParSegment = 250)
            }
            TuileHebdo(
                titre = stringResource(R.string.activite_sport_label),
                couleurFond = Terracotta.copy(alpha = 0.16f),
                couleurTitre = SportTexte,
                valeur = uiState.totalKcalDepense.toString(),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contenu = resumeSport?.let { texte ->
                    {
                        Text(
                            text = texte,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.bilan_assiette_moyenne_titre),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AnneauMacros(
                        glucidesG = glucidesG,
                        proteinesG = proteinesG,
                        lipidesG = lipidesG,
                        modifier = Modifier.size(100.dp),
                    )
                    Spacer(modifier = Modifier.width(24.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        val kcalGlucides = glucidesG * 4
                        val kcalProteines = proteinesG * 4
                        val kcalLipides = lipidesG * 9
                        val totalMacroKcal = (kcalGlucides + kcalProteines + kcalLipides).coerceAtLeast(1)
                        LignePourcentageMacro(
                            couleur = CouleurGlucides,
                            label = stringResource(R.string.correction_macro_glucides),
                            pourcentage = (kcalGlucides * 100f / totalMacroKcal).roundToInt(),
                        )
                        LignePourcentageMacro(
                            couleur = CouleurProteines,
                            label = stringResource(R.string.correction_macro_proteines),
                            pourcentage = (kcalProteines * 100f / totalMacroKcal).roundToInt(),
                        )
                        LignePourcentageMacro(
                            couleur = CouleurLipides,
                            label = stringResource(R.string.correction_macro_lipides),
                            pourcentage = (kcalLipides * 100f / totalMacroKcal).roundToInt(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TuileHebdo(
    titre: String,
    couleurFond: Color,
    couleurTitre: Color,
    valeur: String,
    modifier: Modifier = Modifier,
    contenu: (@Composable ColumnScope.() -> Unit)? = null,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = couleurFond),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = titre,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = couleurTitre,
            )
            Text(
                text = valeur,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 12.dp),
            )
            if (contenu != null) {
                Column(modifier = Modifier.padding(top = 10.dp), content = contenu)
            }
        }
    }
}

@Composable
private fun BarresCaloriesSemaine(jours: List<JourAgregat>, modifier: Modifier = Modifier) {
    val maxKcal = jours.maxOfOrNull { it.kcal }?.coerceAtLeast(1) ?: 1
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        jours.forEach { jour ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    val fraction = (jour.kcal.toFloat() / maxKcal).coerceIn(0.04f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(fraction)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(Terracotta),
                    )
                }
                Text(
                    text = jour.date.dayOfWeek.getDisplayName(JourTextStyle.NARROW, Locale.FRENCH).uppercase(Locale.FRENCH),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun AnneauMacros(
    glucidesG: Int,
    proteinesG: Int,
    lipidesG: Int,
    modifier: Modifier = Modifier,
) {
    val kcalGlucides = glucidesG * 4
    val kcalProteines = proteinesG * 4
    val kcalLipides = lipidesG * 9
    val totalMacroKcal = kcalGlucides + kcalProteines + kcalLipides
    val couleurPiste = MaterialTheme.colorScheme.background

    Canvas(modifier = modifier) {
        val epaisseur = size.minDimension * 0.18f
        val decalage = epaisseur / 2
        val tailleArc = Size(size.width - epaisseur, size.height - epaisseur)
        val topLeft = Offset(decalage, decalage)

        drawArc(
            color = couleurPiste,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = tailleArc,
            style = Stroke(width = epaisseur, cap = StrokeCap.Butt),
        )

        if (totalMacroKcal > 0) {
            var angleDepart = -90f
            listOf(
                kcalGlucides to CouleurGlucides,
                kcalProteines to CouleurProteines,
                kcalLipides to CouleurLipides,
            ).forEach { (kcal, couleur) ->
                val sweep = kcal * 360f / totalMacroKcal
                if (sweep > 0f) {
                    drawArc(
                        color = couleur,
                        startAngle = angleDepart,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = tailleArc,
                        style = Stroke(width = epaisseur, cap = StrokeCap.Butt),
                    )
                    angleDepart += sweep
                }
            }
        }
    }
}

@Composable
private fun LignePourcentageMacro(
    couleur: Color,
    label: String,
    pourcentage: Int,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(couleur),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(R.string.bilan_pourcentage_format, pourcentage),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun NavigationSemaine(
    debutSemaine: LocalDate,
    onSemainePrecedente: () -> Unit,
    onSemaineSuivante: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val libelle = libelleSemaine(debutSemaine)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "‹",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .clickable(onClick = onSemainePrecedente)
                .padding(12.dp),
        )
        Text(
            text = libelle,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "›",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .clickable(onClick = onSemaineSuivante)
                .padding(12.dp),
        )
    }
}
