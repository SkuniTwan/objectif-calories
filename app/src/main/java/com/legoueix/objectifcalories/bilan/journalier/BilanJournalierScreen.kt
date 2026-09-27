package com.legoueix.objectifcalories.bilan.journalier

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.legoueix.objectifcalories.R
import com.legoueix.objectifcalories.activite.CatalogueActivites
import com.legoueix.objectifcalories.bilan.IndicateurEau
import com.legoueix.objectifcalories.activite.POIDS_PAR_DEFAUT_KG
import com.legoueix.objectifcalories.activite.data.ActiviteEntity
import com.legoueix.objectifcalories.analysis.NutritionScoreCalculator
import com.legoueix.objectifcalories.historique.AlimentEnregistre
import com.legoueix.objectifcalories.historique.Repas
import com.legoueix.objectifcalories.historique.glucidesG
import com.legoueix.objectifcalories.historique.kcal
import com.legoueix.objectifcalories.historique.lipidesG
import com.legoueix.objectifcalories.historique.proteinesG
import com.legoueix.objectifcalories.ui.theme.DarkGreen
import com.legoueix.objectifcalories.ui.theme.EauTexte
import com.legoueix.objectifcalories.ui.theme.LightGreen
import com.legoueix.objectifcalories.ui.theme.LightOrange
import com.legoueix.objectifcalories.ui.theme.LightYellow
import com.legoueix.objectifcalories.ui.theme.Orange
import com.legoueix.objectifcalories.ui.theme.Red
import com.legoueix.objectifcalories.ui.theme.SageGreen
import com.legoueix.objectifcalories.ui.theme.SkyBlue
import com.legoueix.objectifcalories.ui.theme.SportFondPale
import com.legoueix.objectifcalories.ui.theme.SportTexte
import com.legoueix.objectifcalories.ui.theme.Terracotta
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private val CouleurGlucides = LightOrange
private val CouleurProteines = LightGreen
private val CouleurLipides = LightYellow

@Composable
fun BilanJournalierScreen(
    viewModel: BilanJournalierViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    var repasDetail by remember { mutableStateOf<Repas?>(null) }
    var repasASupprimer by remember { mutableStateOf<Repas?>(null) }
    var activiteASupprimer by remember { mutableStateOf<ActiviteEntity?>(null) }

    val tousLesAliments = uiState.repas.flatMap { it.items }
    val glucidesG = tousLesAliments.sumOf { it.glucidesG }
    val proteinesG = tousLesAliments.sumOf { it.proteinesG }
    val lipidesG = tousLesAliments.sumOf { it.lipidesG }
    val totalKcal = uiState.repas.sumOf { it.kcal }

    val kcalSport = uiState.activites.sumOf { it.kcal }
    val sousTitreSport = when (uiState.activites.size) {
        0 -> null
        1 -> stringResource(
            R.string.bilan_journalier_sport_activite_format,
            uiState.activites.first().nom,
            uiState.activites.first().dureeMinutes,
        )
        else -> stringResource(
            R.string.bilan_journalier_sport_activites_format,
            uiState.activites.size,
            uiState.activites.sumOf { it.dureeMinutes },
        )
    }

    // Moyenne des notes nutritionnelles des repas du jour (voir NutritionScoreCalculator) :
    // même heuristique que celle affichée à l'enregistrement, pas une nouvelle donnée stockée.
    val noteDuJour = uiState.repas
        .map { repas -> NutritionScoreCalculator.calculer(repas.items.map { it.nutrition to it.grammes }) }
        .let { notes -> if (notes.isEmpty()) 0 else notes.average().roundToInt() }
    // Mêmes seuils que la note affichée à l'enregistrement (voir CorrectionScreen.NutritionScoreCard).
    val couleurNote = when {
        noteDuJour >= 70 -> DarkGreen
        noteDuJour >= 40 -> Orange
        else -> Red
    }

    val poidsAffiche = uiState.dernierPoidsKg ?: POIDS_PAR_DEFAUT_KG

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
                            if (glissementCumule < 0) viewModel.onJourSuivant() else viewModel.onJourPrecedent()
                        }
                    },
                )
            }
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        NavigationJour(
            date = uiState.date,
            onJourPrecedent = viewModel::onJourPrecedent,
            onJourSuivant = viewModel::onJourSuivant,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MacroRingChart(
                    totalKcal = totalKcal,
                    glucidesG = glucidesG,
                    proteinesG = proteinesG,
                    lipidesG = lipidesG,
                    modifier = Modifier.size(120.dp),
                )
                Spacer(modifier = Modifier.width(24.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    MacroLigne(
                        couleur = CouleurGlucides,
                        label = stringResource(R.string.correction_macro_glucides),
                        grammes = glucidesG,
                    )
                    MacroLigne(
                        couleur = CouleurProteines,
                        label = stringResource(R.string.correction_macro_proteines),
                        grammes = proteinesG,
                    )
                    MacroLigne(
                        couleur = CouleurLipides,
                        label = stringResource(R.string.correction_macro_lipides),
                        grammes = lipidesG,
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatTile(
                titre = stringResource(R.string.bilan_journalier_eau_label),
                couleurFond = SkyBlue.copy(alpha = 0.16f),
                couleurTitre = EauTexte,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                Text(
                    text = stringResource(
                        R.string.bilan_journalier_eau_format,
                        String.format(Locale.FRANCE, "%.1f", uiState.eauCl / 100f),
                    ),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                IndicateurEau(centilitres = uiState.eauCl, centilitresParSegment = 50)
            }
            StatTile(
                titre = stringResource(R.string.activite_sport_label),
                couleurFond = Terracotta.copy(alpha = 0.16f),
                couleurTitre = SportTexte,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                Text(
                    text = kcalSport.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (sousTitreSport != null) {
                    Text(
                        text = sousTitreSport,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatTile(
                titre = stringResource(R.string.bilan_journalier_note_titre),
                couleurFond = couleurNote.copy(alpha = 0.16f),
                couleurTitre = couleurNote,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                Row {
                    Text(
                        text = noteDuJour.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = couleurNote,
                        modifier = Modifier.alignByBaseline(),
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = stringResource(R.string.bilan_journalier_note_suffixe),
                        style = MaterialTheme.typography.bodyMedium,
                        color = couleurNote.copy(alpha = 0.7f),
                        modifier = Modifier.alignByBaseline(),
                    )
                }
            }
            StatTile(
                titre = stringResource(R.string.poids_title),
                couleurFond = MaterialTheme.colorScheme.surface,
                couleurTitre = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                Text(
                    text = String.format(Locale.FRANCE, "%.1f", poidsAffiche),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        if (uiState.repas.isEmpty()) {
            Text(
                text = stringResource(R.string.bilan_journalier_vide),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.bilan_journalier_repas_titre, uiState.repas.size),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Terracotta,
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    uiState.repas.forEach { repas ->
                        RepasCard(
                            repas = repas,
                            onClick = { repasDetail = repas },
                            onSupprimer = { repasASupprimer = repas },
                        )
                    }
                }
            }
        }

        if (uiState.activites.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = if (uiState.activites.size == 1) {
                        stringResource(R.string.bilan_journalier_activites_titre_singulier, uiState.activites.size)
                    } else {
                        stringResource(R.string.bilan_journalier_activites_titre_pluriel, uiState.activites.size)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Terracotta,
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    uiState.activites.forEach { activite ->
                        ActiviteCard(
                            activite = activite,
                            onSupprimer = { activiteASupprimer = activite },
                        )
                    }
                }
            }
        }
    }

    val repasPourDetail = repasDetail
    if (repasPourDetail != null) {
        AlertDialog(
            onDismissRequest = { repasDetail = null },
            title = {
                Text(text = repasPourDetail.nom, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    repasPourDetail.items.forEach { item -> AlimentLigne(item = item) }
                }
            },
            confirmButton = {
                TextButton(onClick = { repasDetail = null }) {
                    Text(text = stringResource(R.string.bilan_journalier_fermer))
                }
            },
        )
    }

    val cible = repasASupprimer
    if (cible != null) {
        AlertDialog(
            onDismissRequest = { repasASupprimer = null },
            title = { Text(text = stringResource(R.string.bilan_supprimer_repas_titre)) },
            text = { Text(text = stringResource(R.string.bilan_supprimer_repas_message, cible.nom)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onSupprimerRepas(cible.id)
                        repasASupprimer = null
                    },
                ) {
                    Text(text = stringResource(R.string.bilan_supprimer_repas_confirmer), color = Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { repasASupprimer = null }) {
                    Text(text = stringResource(R.string.bilan_supprimer_repas_annuler))
                }
            },
        )
    }

    val activiteCible = activiteASupprimer
    if (activiteCible != null) {
        AlertDialog(
            onDismissRequest = { activiteASupprimer = null },
            title = { Text(text = stringResource(R.string.bilan_supprimer_activite_titre)) },
            text = { Text(text = stringResource(R.string.bilan_supprimer_activite_message, activiteCible.nom)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onSupprimerActivite(activiteCible.id)
                        activiteASupprimer = null
                    },
                ) {
                    Text(text = stringResource(R.string.bilan_supprimer_repas_confirmer), color = Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { activiteASupprimer = null }) {
                    Text(text = stringResource(R.string.bilan_supprimer_repas_annuler))
                }
            },
        )
    }
}

@Composable
private fun NavigationJour(
    date: LocalDate,
    onJourPrecedent: () -> Unit,
    onJourSuivant: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val formatter = remember { DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH) }
    val libelle = if (date == LocalDate.now()) {
        stringResource(R.string.bilan_journalier_aujourdhui)
    } else {
        date.format(formatter)
    }

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
                .clickable(onClick = onJourPrecedent)
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
                .clickable(onClick = onJourSuivant)
                .padding(12.dp),
        )
    }
}

@Composable
private fun MacroRingChart(
    totalKcal: Int,
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

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val epaisseur = size.minDimension * 0.16f
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
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = totalKcal.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.bilan_journalier_kcal_label),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MacroLigne(couleur: Color, label: String, grammes: Int, modifier: Modifier = Modifier) {
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
            text = stringResource(R.string.correction_grams_format, grammes),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun StatTile(
    titre: String,
    couleurFond: Color,
    couleurTitre: Color,
    modifier: Modifier = Modifier,
    contenu: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = couleurFond),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = titre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = couleurTitre,
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp), content = contenu)
        }
    }
}

private val LargeurCarteRepas = 128.dp

@Composable
private fun RepasCard(
    repas: Repas,
    onClick: () -> Unit,
    onSupprimer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .width(LargeurCarteRepas)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column {
            PhotoCarteRepas(photo = repas.photo, modifier = Modifier.fillMaxWidth().height(84.dp))
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = repas.nom,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.correction_kcal_format, repas.kcal),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onSupprimer, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.bilan_repas_delete_content_description),
                            tint = Terracotta,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoCarteRepas(photo: ByteArray?, modifier: Modifier = Modifier) {
    val forme = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    if (photo != null) {
        val bitmap = remember(photo) {
            BitmapFactory.decodeByteArray(photo, 0, photo.size).asImageBitmap()
        }
        Image(
            bitmap = bitmap,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(forme),
        )
    } else {
        Surface(modifier = modifier, shape = forme, color = SageGreen.copy(alpha = 0.14f)) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(text = "🍽️", style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}

@Composable
private fun AlimentLigne(item: AlimentEnregistre, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = item.nom.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(R.string.correction_grams_format, item.grammes),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.width(40.dp),
        )
        MacroBadge(lettre = "g", valeur = item.glucidesG, couleur = CouleurGlucides)
        MacroBadge(lettre = "p", valeur = item.proteinesG, couleur = CouleurProteines)
        MacroBadge(lettre = "l", valeur = item.lipidesG, couleur = CouleurLipides)
    }
}

@Composable
private fun MacroBadge(lettre: String, valeur: Int, couleur: Color, modifier: Modifier = Modifier) {
    Text(
        text = "$lettre $valeur",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = couleur,
        textAlign = TextAlign.End,
        modifier = modifier.width(34.dp),
    )
}

@Composable
private fun ActiviteCard(activite: ActiviteEntity, onSupprimer: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.width(LargeurCarteRepas),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(SportFondPale),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = emojiPourActivite(activite.nom), style = MaterialTheme.typography.headlineLarge)
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = activite.nom,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.bilan_journalier_duree_format, activite.dureeMinutes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.correction_kcal_format, activite.kcal),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onSupprimer, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.bilan_activite_delete_content_description),
                            tint = Terracotta,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}

private fun emojiPourActivite(nom: String): String =
    CatalogueActivites.liste.find { it.nom == nom }?.emoji ?: "🏃"
