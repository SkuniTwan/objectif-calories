package com.legoueix.objectifcalories.bilan

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.legoueix.objectifcalories.ui.theme.SkyBlue

private val LargeurAxeY = 34.dp
private val HauteurAxeX = 18.dp

/**
 * Courbe(s) sur une série de jours, avec deux repères sur l'axe Y (0 et le maximum) et
 * les numéros de jour sur l'axe X. [pasEtiquette] n'affiche qu'un jour sur N en abscisse
 * (le dernier jour est toujours affiché) pour éviter que les libellés se chevauchent
 * quand la série est longue (bilan mensuel).
 */
@Composable
fun LigneChart(
    jours: List<JourAgregat>,
    series: List<Pair<Color, (JourAgregat) -> Int>>,
    modifier: Modifier = Modifier,
    pasEtiquette: Int = 1,
) {
    val maxValeur = jours
        .flatMap { jour -> series.map { (_, valeur) -> valeur(jour) } }
        .maxOrNull()
        ?.coerceAtLeast(1) ?: 1

    val paintAxeY = remember {
        Paint().apply {
            color = 0xFF9E9E9E.toInt()
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
    }
    val paintAxeX = remember {
        Paint().apply {
            color = 0xFF9E9E9E.toInt()
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp),
    ) {
        if (jours.size < 2) return@Canvas
        paintAxeY.textSize = 11.sp.toPx()
        paintAxeX.textSize = 10.sp.toPx()

        val largeurLabel = LargeurAxeY.toPx()
        val hauteurLabelX = HauteurAxeX.toPx()
        val largeurGraphe = size.width - largeurLabel
        val hauteurGraphe = size.height - hauteurLabelX
        val pas = largeurGraphe / (jours.size - 1)

        // Deux repères sur l'axe Y : 0 en bas, le maximum de la série en haut.
        listOf(0 to hauteurGraphe, maxValeur to 0f).forEach { (valeurRepere, y) ->
            drawLine(
                color = Color.LightGray,
                start = Offset(largeurLabel, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx(),
            )
            drawContext.canvas.nativeCanvas.drawText(
                valeurRepere.toString(),
                largeurLabel - 6.dp.toPx(),
                y - (paintAxeY.descent() + paintAxeY.ascent()) / 2,
                paintAxeY,
            )
        }

        series.forEach { (couleur, valeur) ->
            val points = jours.mapIndexed { index, jour ->
                val y = hauteurGraphe - (valeur(jour).toFloat() / maxValeur) * hauteurGraphe
                Offset(largeurLabel + index * pas, y)
            }
            for (i in 0 until points.size - 1) {
                drawLine(
                    color = couleur,
                    start = points[i],
                    end = points[i + 1],
                    strokeWidth = 1.5.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
            points.forEach { point ->
                drawCircle(color = couleur, radius = 2.5.dp.toPx(), center = point)
            }
        }

        jours.forEachIndexed { index, jour ->
            if (index % pasEtiquette == 0 || index == jours.lastIndex) {
                drawContext.canvas.nativeCanvas.drawText(
                    jour.date.dayOfMonth.toString(),
                    largeurLabel + index * pas,
                    size.height - (paintAxeX.descent() + paintAxeX.ascent()) / 2,
                    paintAxeX,
                )
            }
        }
    }
}

@Composable
fun LegendeItem(couleur: Color, label: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(couleur),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 6.dp),
        )
    }
}

/**
 * 4 traits représentant la consommation d'eau, chacun valant [centilitresParSegment].
 * Même indicateur sur les trois bilans (jour, semaine, mois) ; seule l'échelle change
 * (50 cl/trait au jour, 250 cl/trait à la semaine, 1000 cl/trait au mois).
 */
@Composable
fun IndicateurEau(centilitres: Int, centilitresParSegment: Int, modifier: Modifier = Modifier) {
    val segments = 4
    val segmentsRemplis = (centilitres / centilitresParSegment).coerceIn(0, segments)
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(segments) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(if (index < segmentsRemplis) SkyBlue else Color.White.copy(alpha = 0.7f)),
            )
        }
    }
}
