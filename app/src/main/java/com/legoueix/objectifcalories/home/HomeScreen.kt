package com.legoueix.objectifcalories.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.legoueix.objectifcalories.ui.theme.Charcoal
import com.legoueix.objectifcalories.ui.theme.DarkGreen
import com.legoueix.objectifcalories.ui.theme.Gold
import com.legoueix.objectifcalories.ui.theme.SkyBlue
import com.legoueix.objectifcalories.ui.theme.Terracotta

@Composable
fun HomeScreen(
    onOpenPhotoMode: () -> Unit,
    onOpenVoiceMode: () -> Unit,
    onOpenTextMode: () -> Unit,
    onOpenGalleryMode: () -> Unit,
    onOpenFavorisMode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.menu_enregistrer_repas),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        CartePhotoPrincipale(onClick = onOpenPhotoMode)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.height(IntrinsicSize.Min)) {
            CarteModeCompacte(
                icone = Icons.Default.PhotoLibrary,
                couleurFond = SkyBlue.copy(alpha = 0.16f),
                couleurIcone = SkyBlue,
                titre = stringResource(R.string.home_mode_gallery_title),
                description = stringResource(R.string.home_mode_gallery_description),
                onClick = onOpenGalleryMode,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            CarteModeCompacte(
                icone = Icons.Default.Mic,
                couleurFond = Terracotta.copy(alpha = 0.16f),
                couleurIcone = Terracotta,
                titre = stringResource(R.string.home_mode_voice_title),
                description = stringResource(R.string.home_mode_voice_description),
                onClick = onOpenVoiceMode,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.height(IntrinsicSize.Min)) {
            CarteModeCompacte(
                icone = Icons.Default.Notes,
                couleurFond = DarkGreen.copy(alpha = 0.16f),
                couleurIcone = DarkGreen,
                titre = stringResource(R.string.home_mode_text_title),
                description = stringResource(R.string.home_mode_text_description),
                onClick = onOpenTextMode,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            CarteModeCompacte(
                icone = Icons.Default.Star,
                couleurFond = Gold.copy(alpha = 0.16f),
                couleurIcone = Gold,
                titre = stringResource(R.string.home_mode_favoris_title),
                description = stringResource(R.string.home_mode_favoris_description),
                onClick = onOpenFavorisMode,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }
    }
}

@Composable
private fun CartePhotoPrincipale(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Charcoal),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .background(Charcoal),
            ) {
                Text(
                    text = stringResource(R.string.home_mode_photo_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 8.dp),
                )
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.home_mode_photo_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.75f),
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    BoutonPhotoPrincipale(
                        texte = stringResource(R.string.home_bouton_photographier),
                        couleurFond = Terracotta,
                        onClick = onClick,
                        modifier = Modifier.weight(1f),
                    )
                    BoutonPhotoPrincipale(
                        texte = stringResource(R.string.home_bouton_code_barres),
                        couleurFond = Color.White.copy(alpha = 0.14f),
                        onClick = onClick,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun BoutonPhotoPrincipale(
    texte: String,
    couleurFond: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(percent = 50),
        colors = CardDefaults.cardColors(containerColor = couleurFond),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = texte,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun CarteModeCompacte(
    icone: ImageVector,
    couleurFond: Color,
    couleurIcone: Color,
    titre: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(couleurFond, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(imageVector = icone, contentDescription = null, tint = couleurIcone)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = titre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

