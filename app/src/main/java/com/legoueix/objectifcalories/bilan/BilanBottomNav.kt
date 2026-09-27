package com.legoueix.objectifcalories.bilan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.legoueix.objectifcalories.R
import com.legoueix.objectifcalories.ui.theme.Charcoal

enum class BilanPeriode(val labelRes: Int) {
    JOUR(R.string.bilan_periode_jour),
    SEMAINE(R.string.bilan_periode_semaine),
    MOIS(R.string.bilan_periode_mois),
}

@Composable
fun BilanBottomNav(
    selection: BilanPeriode,
    onSelectionChange: (BilanPeriode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            // Sous le bandeau système de navigation gestuelle (transparent, superposé en bas
            // sur Android) : sans ça, ses boutons sont visuellement là mais inatteignables au
            // tap, capté par le geste système à la place.
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        shape = RoundedCornerShape(percent = 50),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            BilanPeriode.values().forEach { periode ->
                val estSelectionnee = periode == selection
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(if (estSelectionnee) Charcoal else Color.Transparent)
                        .clickable { onSelectionChange(periode) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(periode.labelRes),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (estSelectionnee) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
