package com.legoueix.objectifcalories.gallery

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.legoueix.objectifcalories.R
import com.legoueix.objectifcalories.correction.CaptureUiState
import com.legoueix.objectifcalories.ui.theme.Charcoal
import com.legoueix.objectifcalories.ui.theme.SkyBlue
import com.legoueix.objectifcalories.ui.theme.SurfaceAlt
import com.legoueix.objectifcalories.ui.theme.Terracotta
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private fun permissionPhotos(): String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

// Correspond aux puces de l'image (pas de "Goûter" ici, contrairement à la suggestion de
// nom utilisée à l'enregistrement — voir MealCaptureViewModel.nomParDefaut).
private fun typeRepasParDefaut(heure: Int): Int = when (heure) {
    in 5..10 -> R.string.gallery_type_petit_dej
    in 11..14 -> R.string.gallery_type_dejeuner
    in 19..22 -> R.string.gallery_type_diner
    else -> R.string.gallery_type_collation
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GalleryPickerScreen(
    uiState: CaptureUiState,
    onImport: (ByteArray, String) -> Unit,
    onAbandon: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var aLaPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, permissionPhotos()) == PackageManager.PERMISSION_GRANTED,
        )
    }
    val lanceurPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { accordee -> aLaPermission = accordee }
    LaunchedEffect(Unit) {
        if (!aLaPermission) lanceurPermission.launch(permissionPhotos())
    }

    var photosRecentes by remember { mutableStateOf<List<Uri>>(emptyList()) }
    LaunchedEffect(aLaPermission) {
        if (aLaPermission) {
            photosRecentes = chargerPhotosRecentes(context, limite = 30)
        }
    }

    var uriSelectionnee by remember { mutableStateOf<Uri?>(null) }
    var photoSelectionnee by remember { mutableStateOf<ByteArray?>(null) }

    fun selectionner(uri: Uri) {
        uriSelectionnee = uri
        coroutineScope.launch {
            photoSelectionnee = chargerPhotoComplete(context, uri)
        }
    }

    val choisirDansTouteLaGalerie = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        if (uri != null) selectionner(uri)
    }
    val ouvrirToutesLesPhotos = {
        choisirDansTouteLaGalerie.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    var typeRepasRes by remember {
        mutableStateOf(typeRepasParDefaut(Instant.now().atZone(ZoneId.systemDefault()).hour))
    }
    val typeRepasLabel = stringResource(typeRepasRes)

    if (uiState is CaptureUiState.Error) {
        val message = stringResource(R.string.analysis_error)
        LaunchedEffect(uiState) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 24.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(SkyBlue.copy(alpha = 0.16f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        tint = SkyBlue,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.gallery_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.gallery_recentes).uppercase(Locale.FRENCH),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(R.string.gallery_toutes_les_photos),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Terracotta,
                        modifier = Modifier.clickable(onClick = ouvrirToutesLesPhotos),
                    )
                }

                if (!aLaPermission) {
                    CarteAutorisationPhotos(onAutoriser = { lanceurPermission.launch(permissionPhotos()) })
                } else if (photosRecentes.isEmpty()) {
                    Text(
                        text = stringResource(R.string.gallery_aucune_photo_recente),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        photosRecentes.take(6).chunked(3).forEach { ligne ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                ligne.forEach { uri ->
                                    VignettePhoto(
                                        uri = uri,
                                        estSelectionnee = uri == uriSelectionnee,
                                        onClick = { selectionner(uri) },
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                                repeat(3 - ligne.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            if (photoSelectionnee != null) {
                CartePhotoSelectionnee(
                    photo = photoSelectionnee,
                    onChanger = ouvrirToutesLesPhotos,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.gallery_ce_repas_est_un).uppercase(Locale.FRENCH),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    listOf(
                        R.string.gallery_type_petit_dej,
                        R.string.gallery_type_dejeuner,
                        R.string.gallery_type_diner,
                        R.string.gallery_type_collation,
                    ).forEach { res ->
                        PuceTypeRepas(
                            label = stringResource(res),
                            selectionnee = res == typeRepasRes,
                            onClick = { typeRepasRes = res },
                        )
                    }
                }
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = { photoSelectionnee?.let { onImport(it, typeRepasLabel) } },
                    enabled = photoSelectionnee != null && uiState !is CaptureUiState.Analyzing,
                    shape = RoundedCornerShape(percent = 50),
                    colors = ButtonDefaults.buttonColors(containerColor = Terracotta, contentColor = Color.White),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                ) {
                    if (uiState is CaptureUiState.Analyzing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = stringResource(R.string.gallery_analyser_la_photo),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                BoutonFermer(onClick = onAbandon, modifier = Modifier.size(52.dp))
            }
        }
    }
}

@Composable
private fun BoutonFermer(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(SurfaceAlt)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = stringResource(R.string.abandon_content_description),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun CarteAutorisationPhotos(onAutoriser: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = stringResource(R.string.gallery_permission_rationale),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.gallery_permission_request),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Terracotta,
                modifier = Modifier.clickable(onClick = onAutoriser),
            )
        }
    }
}

@Composable
private fun VignettePhoto(
    uri: Uri,
    estSelectionnee: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val miniature by produceState<ImageBitmap?>(initialValue = null, uri) {
        value = chargerMiniature(context, uri, tailleCiblePx = 300)
    }
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceAlt)
            .then(
                if (estSelectionnee) {
                    Modifier.border(2.dp, Terracotta, RoundedCornerShape(14.dp))
                } else {
                    Modifier
                },
            )
            .clickable(onClick = onClick),
    ) {
        miniature?.let { bitmap ->
            Image(
                bitmap = bitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        if (estSelectionnee) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(22.dp)
                    .background(Terracotta, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

@Composable
private fun CartePhotoSelectionnee(photo: ByteArray?, onChanger: () -> Unit, modifier: Modifier = Modifier) {
    val bitmap = remember(photo) {
        photo?.let { BitmapFactory.decodeByteArray(it, 0, it.size).asImageBitmap() }
    }
    val formatterHeure = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val maintenant = remember(photo) { Instant.now().atZone(ZoneId.systemDefault()) }
    val libelleJour = if (maintenant.toLocalDate() == LocalDate.now()) {
        stringResource(R.string.bilan_journalier_aujourdhui)
    } else {
        maintenant.toLocalDate().toString()
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceAlt),
            ) {
                bitmap?.let {
                    Image(
                        bitmap = it,
                        contentDescription = stringResource(R.string.gallery_photo_selectionnee),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.gallery_photo_selectionnee),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.gallery_horodatage_format, libelleJour, maintenant.format(formatterHeure)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = stringResource(R.string.gallery_changer),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Terracotta,
                modifier = Modifier.clickable(onClick = onChanger),
            )
        }
    }
}

@Composable
private fun PuceTypeRepas(label: String, selectionnee: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(percent = 50),
        colors = CardDefaults.cardColors(
            containerColor = if (selectionnee) Charcoal else MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (selectionnee) Color.White else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
        )
    }
}
