package com.legoueix.objectifcalories.activite

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.legoueix.objectifcalories.R

private const val DUREE_REFERENCE_MINUTES = 30

@Composable
fun CaloriesParSportScreen(
    dernierPoidsKg: Double?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val poidsUtilise = dernierPoidsKg ?: POIDS_PAR_DEFAUT_KG

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.calories_sport_title),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        )
        Text(
            text = stringResource(R.string.calories_sport_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(items = CatalogueActivites.liste, key = { it.nom }) { activite ->
                CarteSport(
                    activite = activite,
                    kcal = kcalDepensees(activite.met, poidsUtilise, DUREE_REFERENCE_MINUTES),
                )
            }
        }

        TextButton(onClick = onBack, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text(text = stringResource(R.string.back_to_home))
        }
    }
}

@Composable
private fun CarteSport(activite: TypeActivite, kcal: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.aspectRatio(0.82f),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(text = activite.emoji, fontSize = 26.sp)
            Text(
                text = activite.nom,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                maxLines = 2,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = stringResource(R.string.calories_sport_kcal_format, kcal),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
