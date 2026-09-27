package com.legoueix.objectifcalories.voice

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.legoueix.objectifcalories.R
import com.legoueix.objectifcalories.ui.theme.Charcoal
import com.legoueix.objectifcalories.ui.theme.Red
import com.legoueix.objectifcalories.ui.theme.SurfaceAlt
import com.legoueix.objectifcalories.ui.theme.Terracotta
import com.legoueix.objectifcalories.ui.theme.TextSecondary

private enum class EtatDictee { INACTIF, ECOUTE, TRAITEMENT }

@Composable
fun VoiceRecordingScreen(
    onDicteeTerminee: (List<Pair<String, Int>>) -> Unit,
    onEcrireManuel: () -> Unit,
    onAbandon: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var etat by remember { mutableStateOf(EtatDictee.INACTIF) }
    var niveauAudio by remember { mutableStateOf(0f) }
    // Le service de reconnaissance ignore largement EXTRA_SPEECH_INPUT_*_SILENCE_LENGTH
    // sur la plupart des appareils : on ne peut pas compter dessus pour éviter la coupure.
    // À la place, on relance l'écoute à chaque coupure (pause) et on s'appuie sur les
    // résultats partiels pour ne pas perdre les mots captés juste avant la coupure.
    var ecouteVoulue by remember { mutableStateOf(false) }
    var transcriptAccumule by remember { mutableStateOf("") }
    var dernierPartiel by remember { mutableStateOf("") }
    // Ignore le prochain onError : il correspond à l'annulation volontaire déclenchée par
    // recommencer(), pas à une vraie erreur de reconnaissance.
    var annulationEnCours by remember { mutableStateOf(false) }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }

    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else {
            null
        }
    }
    DisposableEffect(Unit) {
        onDispose { speechRecognizer?.destroy() }
    }

    val intentReconnaissance = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fr-FR")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            // Indications best-effort : honorées sur certains appareils seulement, donc
            // pas fiables seules (voir la boucle de relance ci-dessous).
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5_000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 5_000L)
        }
    }

    val messageAucunResultat = stringResource(R.string.voice_no_result)
    val messageErreur = stringResource(R.string.voice_error)
    val messageIndisponible = stringResource(R.string.voice_unavailable)

    fun finaliser() {
        etat = EtatDictee.INACTIF
        ecouteVoulue = false
        val paires = DictationParser.analyser(transcriptAccumule)
        transcriptAccumule = ""
        if (paires.isNotEmpty()) {
            onDicteeTerminee(paires)
        } else {
            Toast.makeText(context, messageAucunResultat, Toast.LENGTH_SHORT).show()
        }
    }

    val recognitionListener = remember {
        object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                etat = EtatDictee.ECOUTE
            }

            override fun onRmsChanged(rmsdB: Float) {
                niveauAudio = (rmsdB / 10f).coerceIn(0f, 1f)
            }

            override fun onEndOfSpeech() {
                etat = EtatDictee.TRAITEMENT
            }

            override fun onPartialResults(partialResults: Bundle?) {
                // Capté en continu pendant l'écoute : sert de filet de sécurité si la
                // coupure arrive avant qu'un résultat final ne soit produit pour ce segment.
                dernierPartiel = partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    .orEmpty()
            }

            override fun onResults(results: Bundle) {
                val texteFinal = results
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    .orEmpty()
                val segment = texteFinal.ifBlank { dernierPartiel }
                if (segment.isNotBlank()) {
                    transcriptAccumule = (transcriptAccumule + " " + segment).trim()
                }
                dernierPartiel = ""
                if (ecouteVoulue) {
                    // Juste une pause entre deux aliments : on continue d'écouter.
                    speechRecognizer?.startListening(intentReconnaissance)
                } else {
                    finaliser()
                }
            }

            override fun onError(error: Int) {
                if (annulationEnCours) {
                    // Annulation volontaire (recommencer()) : déjà géré là-bas, on ignore.
                    annulationEnCours = false
                    return
                }
                // Coupure avant tout résultat final pour ce segment : on garde quand même
                // ce que les résultats partiels avaient déjà capté.
                if (dernierPartiel.isNotBlank()) {
                    transcriptAccumule = (transcriptAccumule + " " + dernierPartiel).trim()
                    dernierPartiel = ""
                }
                val silenceSimple = error == SpeechRecognizer.ERROR_NO_MATCH ||
                    error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT
                when {
                    silenceSimple && ecouteVoulue -> speechRecognizer?.startListening(intentReconnaissance)
                    !ecouteVoulue -> finaliser()
                    else -> {
                        ecouteVoulue = false
                        etat = EtatDictee.INACTIF
                        Toast.makeText(context, messageErreur, Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onBeginningOfSpeech() {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    fun demarrerEcoute() {
        val recognizer = speechRecognizer
        if (recognizer == null) {
            Toast.makeText(context, messageIndisponible, Toast.LENGTH_SHORT).show()
            return
        }
        ecouteVoulue = true
        transcriptAccumule = ""
        dernierPartiel = ""
        recognizer.setRecognitionListener(recognitionListener)
        recognizer.startListening(intentReconnaissance)
    }

    fun arreterEcoute() {
        ecouteVoulue = false
        speechRecognizer?.stopListening()
    }

    fun recommencer() {
        annulationEnCours = true
        transcriptAccumule = ""
        dernierPartiel = ""
        speechRecognizer?.cancel()
        demarrerEcoute()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasPermission = granted
        if (granted) demarrerEcoute()
    }

    val transcriptAffiche = (transcriptAccumule + " " + dernierPartiel).trim()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Terracotta.copy(alpha = 0.16f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = Terracotta,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.voice_titre),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                BoutonFermerVoix(onClick = onAbandon)
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = stringResource(R.string.voice_instructions_titre),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.voice_instructions_sous_titre),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    BulleExemple(texte = stringResource(R.string.voice_exemple_1))
                    BulleExemple(texte = stringResource(R.string.voice_exemple_2))
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                MicButton(
                    etat = etat,
                    niveauAudio = niveauAudio,
                    onClick = {
                        when (etat) {
                            EtatDictee.INACTIF -> {
                                if (hasPermission) {
                                    demarrerEcoute()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                            EtatDictee.ECOUTE -> arreterEcoute()
                            EtatDictee.TRAITEMENT -> Unit
                        }
                    },
                )

                Text(
                    text = stringResource(
                        when (etat) {
                            EtatDictee.INACTIF -> R.string.voice_tap_to_start
                            EtatDictee.ECOUTE -> R.string.voice_listening
                            EtatDictee.TRAITEMENT -> R.string.voice_processing
                        },
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }

            if (etat == EtatDictee.ECOUTE) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { recommencer() },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(text = stringResource(R.string.voice_restart))
                    }
                    Button(
                        onClick = { arreterEcoute() },
                        colors = ButtonDefaults.buttonColors(containerColor = Red, contentColor = Color.White),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(text = stringResource(R.string.voice_stop))
                    }
                }
            }

            if (transcriptAffiche.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.voice_transcript_label),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = transcriptAffiche,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }

            if (etat == EtatDictee.INACTIF) {
                CarteEcrirePlutot(onClick = onEcrireManuel)
            }
        }
    }
}

@Composable
private fun BoutonFermerVoix(onClick: () -> Unit, modifier: Modifier = Modifier) {
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
private fun BulleExemple(texte: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceAlt)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(
            text = texte,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CarteEcrirePlutot(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(SurfaceAlt, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(imageVector = Icons.Default.Notes, contentDescription = null, tint = TextSecondary)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.voice_ecrire_plutot_titre),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.voice_ecrire_plutot_sous_titre),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MicButton(
    etat: EtatDictee,
    niveauAudio: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val echelle by animateFloatAsState(
        targetValue = if (etat == EtatDictee.ECOUTE) 1f + (niveauAudio * 0.15f) else 1f,
        animationSpec = tween(150),
        label = "micPulse",
    )

    Box(
        modifier = modifier
            .size(180.dp)
            .scale(echelle)
            .clip(CircleShape)
            .clickable(enabled = etat != EtatDictee.TRAITEMENT, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape)
                .background(Terracotta.copy(alpha = 0.12f)),
        )
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(Terracotta.copy(alpha = 0.20f)),
        )
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(if (etat == EtatDictee.TRAITEMENT) Charcoal else Terracotta),
            contentAlignment = Alignment.Center,
        ) {
            if (etat == EtatDictee.TRAITEMENT) {
                CircularProgressIndicator(color = Color.White)
            } else {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp),
                )
            }
        }
    }
}
