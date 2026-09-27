package com.legoueix.objectifcalories.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Sélecteur de date (jour uniquement) : ouvre le sélecteur natif Android, en conservant
 * l'heure d'origine de [dateHeure] — seul le jour change, l'heure enregistrée reste
 * l'heure réelle de la saisie.
 */
@Composable
fun DateField(
    label: String,
    dateHeure: Long,
    onDateChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val zoneId = remember { ZoneId.systemDefault() }
    val formatter = remember { DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH) }
    val instantSelectionne = remember(dateHeure) { Instant.ofEpochMilli(dateHeure).atZone(zoneId) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                DatePickerDialog(
                    context,
                    { _, annee, mois, jour ->
                        val nouvelleDate = LocalDate.of(annee, mois + 1, jour)
                            .atTime(instantSelectionne.toLocalTime())
                            .atZone(zoneId)
                        onDateChange(nouvelleDate.toInstant().toEpochMilli())
                    },
                    instantSelectionne.year,
                    instantSelectionne.monthValue - 1,
                    instantSelectionne.dayOfMonth,
                ).show()
            }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = instantSelectionne.toLocalDate().format(formatter),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
