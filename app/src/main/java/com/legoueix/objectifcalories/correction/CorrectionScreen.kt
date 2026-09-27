package com.legoueix.objectifcalories.correction

import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import com.legoueix.objectifcalories.R
import com.legoueix.objectifcalories.analysis.Confiance
import com.legoueix.objectifcalories.analysis.NutritionScoreCalculator
import com.legoueix.objectifcalories.ui.AbandonButton
import com.legoueix.objectifcalories.ui.DateField
import com.legoueix.objectifcalories.ui.theme.Amber
import com.legoueix.objectifcalories.ui.theme.DarkGreen
import com.legoueix.objectifcalories.ui.theme.Orange
import com.legoueix.objectifcalories.ui.theme.Red

private const val GRAMMES_MIN = 0f
private const val GRAMMES_MAX = 500f

@Composable
fun CorrectionScreen(
    result: CaptureUiState.Result,
    onGrammesCorriges: (nom: String, grammes: Int) -> Unit,
    onValidate: (nom: String, dateHeure: Long) -> Unit,
    onRetakePhoto: () -> Unit,
    onAbandon: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val totalKcal = result.items.sumOf { it.kcal }
    val nutritionScore = NutritionScoreCalculator.calculer(result.items.map { it.nutrition to it.grammesCorriges })
    val totalProteines = result.items.sumOf { it.nutrition.proteines * it.grammesCorriges / 100.0 }.roundToInt()
    val totalGlucides = result.items.sumOf { it.nutrition.glucides * it.grammesCorriges / 100.0 }.roundToInt()
    val totalLipides = result.items.sumOf { it.nutrition.lipides * it.grammesCorriges / 100.0 }.roundToInt()
    var dateSelectionnee by remember { mutableStateOf(System.currentTimeMillis()) }
    var nomRepas by remember { mutableStateOf(result.nomInitial) }

    Column(modifier = modifier.fillMaxSize().imePadding()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextField(
                value = nomRepas,
                onValueChange = { texte -> nomRepas = texte },
                placeholder = {
                    Text(
                        text = stringResource(R.string.correction_nom_placeholder),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                textStyle = MaterialTheme.typography.titleLarge,
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                modifier = Modifier.weight(1f),
            )
            // N'a de sens que pour un repas tout juste photographié/importé : rien à
            // "reprendre" pour un repas saisi en texte, dicté, ou choisi en favori (même
            // si ce favori a lui-même une photo).
            if (result.peutReprendre) {
                TextButton(onClick = onRetakePhoto) {
                    Text(text = stringResource(R.string.correction_retake_photo))
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            result.photo?.let { photo -> item { MealPhoto(photo = photo) } }

            item {
                SummaryCard(
                    totalKcal = totalKcal,
                    itemCount = result.items.size,
                    confiance = result.confiance,
                    proteines = totalProteines,
                    glucides = totalGlucides,
                    lipides = totalLipides,
                )
            }

            item { NutritionScoreCard(score = nutritionScore) }

            item {
                Text(
                    text = stringResource(R.string.correction_section_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            items(items = result.items, key = { it.nom }) { item ->
                CorrectionItemCard(
                    item = item,
                    onGrammesChange = { nouveauxGrammes -> onGrammesCorriges(item.nom, nouveauxGrammes) },
                )
            }
        }

        Surface(shadowElevation = 8.dp) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                DateField(
                    label = stringResource(R.string.correction_date_label),
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
                        onClick = { onValidate(nomRepas.trim(), dateSelectionnee) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(text = stringResource(R.string.correction_validate))
                    }
                    AbandonButton(onClick = onAbandon)
                }
            }
        }
    }
}

@Composable
private fun MealPhoto(photo: ByteArray, modifier: Modifier = Modifier) {
    val bitmap = remember(photo) {
        BitmapFactory.decodeByteArray(photo, 0, photo.size).asImageBitmap()
    }
    Image(
        bitmap = bitmap,
        contentDescription = stringResource(R.string.correction_photo_content_description),
        contentScale = ContentScale.Crop,
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(24.dp)),
    )
}

@Composable
private fun SummaryCard(
    totalKcal: Int,
    itemCount: Int,
    confiance: Confiance,
    proteines: Int,
    glucides: Int,
    lipides: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.correction_total_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(R.string.correction_kcal_format, totalKcal),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                ConfidenceChip(confiance = confiance)
            }
            Text(
                text = pluralStringResource(R.plurals.correction_item_count, itemCount, itemCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            MacroSummaryRow(
                proteines = proteines,
                glucides = glucides,
                lipides = lipides,
                modifier = Modifier.padding(top = 16.dp),
            )
            if (confiance == Confiance.FAIBLE) {
                Text(
                    text = stringResource(R.string.confiance_faible),
                    style = MaterialTheme.typography.bodySmall,
                    color = Amber,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun MacroSummaryRow(
    proteines: Int,
    glucides: Int,
    lipides: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        MacroStat(
            label = stringResource(R.string.correction_macro_proteines),
            grammes = proteines,
        )
        MacroStat(
            label = stringResource(R.string.correction_macro_glucides),
            grammes = glucides,
        )
        MacroStat(
            label = stringResource(R.string.correction_macro_lipides),
            grammes = lipides,
        )
    }
}

@Composable
private fun MacroStat(label: String, grammes: Int, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.correction_grams_format, grammes),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun NutritionScoreCard(score: Int, modifier: Modifier = Modifier) {
    val color = when {
        score >= 70 -> DarkGreen
        score >= 40 -> Orange
        else -> Red
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.10f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.correction_nutrition_score_label),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.correction_nutrition_score_format, score),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color,
            )
        }
    }
}

@Composable
private fun ConfidenceChip(confiance: Confiance, modifier: Modifier = Modifier) {
    val (labelRes, tint) = when (confiance) {
        Confiance.ELEVEE -> R.string.confiance_badge_elevee to MaterialTheme.colorScheme.primary
        Confiance.MOYENNE -> R.string.confiance_badge_moyenne to MaterialTheme.colorScheme.secondary
        Confiance.FAIBLE -> R.string.confiance_badge_faible to Amber
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(percent = 50),
        color = tint.copy(alpha = 0.14f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(tint),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(labelRes),
                style = MaterialTheme.typography.labelMedium,
                color = tint,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CorrectionItemCard(
    item: ItemCorrige,
    onGrammesChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Le slider seul manque de précision sur une plage 0-500 : la valeur affichée
    // reste cliquable pour saisir un grammage exact au clavier.
    var enEdition by remember { mutableStateOf(false) }
    var texteEdition by remember { mutableStateOf("") }
    var aEuLeFocus by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    fun validerEdition() {
        texteEdition.toIntOrNull()
            ?.coerceIn(GRAMMES_MIN.toInt(), GRAMMES_MAX.toInt())
            ?.let(onGrammesChange)
        enEdition = false
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = item.nom.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = stringResource(R.string.correction_kcal_format, item.kcal),
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkGreen,
                    fontWeight = FontWeight.Bold,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Slider(
                    value = item.grammesCorriges.toFloat(),
                    onValueChange = { onGrammesChange(it.toInt()) },
                    valueRange = GRAMMES_MIN..GRAMMES_MAX,
                    modifier = Modifier.weight(1f),
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
                                    .background(MaterialTheme.colorScheme.primary),
                            )
                        }
                    },
                    thumb = {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .shadow(elevation = 2.dp, shape = CircleShape)
                                .clip(CircleShape)
                                .background(Color.White),
                        )
                    },
                )
                Spacer(modifier = Modifier.width(12.dp))
                if (enEdition) {
                    OutlinedTextField(
                        value = texteEdition,
                        onValueChange = { texte -> texteEdition = texte.filter(Char::isDigit).take(4) },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.labelLarge.copy(textAlign = TextAlign.End),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                validerEdition()
                                focusManager.clearFocus()
                            },
                        ),
                        modifier = Modifier
                            .width(90.dp)
                            .focusRequester(focusRequester)
                            .onFocusChanged { etat ->
                                if (etat.isFocused) {
                                    aEuLeFocus = true
                                } else if (aEuLeFocus) {
                                    validerEdition()
                                }
                            },
                    )
                    LaunchedEffect(Unit) { focusRequester.requestFocus() }
                } else {
                    Text(
                        text = stringResource(R.string.correction_grams_format, item.grammesCorriges),
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .widthIn(min = 44.dp)
                            .clickable {
                                texteEdition = item.grammesCorriges.toString()
                                aEuLeFocus = false
                                enEdition = true
                            },
                    )
                }
            }
        }
    }
}
