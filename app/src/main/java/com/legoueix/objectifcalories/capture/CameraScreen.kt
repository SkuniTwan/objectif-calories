package com.legoueix.objectifcalories.capture

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.legoueix.objectifcalories.R
import com.legoueix.objectifcalories.correction.CaptureUiState
import kotlinx.coroutines.launch

@Composable
fun CameraScreen(
    uiState: CaptureUiState,
    onPhotoCaptured: (ByteArray) -> Unit,
    onCodeBarresDetecte: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            CameraPreviewWithCapture(
                enabled = uiState !is CaptureUiState.Analyzing,
                onPhotoCaptured = onPhotoCaptured,
                onCodeBarresDetecte = onCodeBarresDetecte,
            )
        } else {
            PermissionRationale(onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) })
        }

        if (uiState is CaptureUiState.Analyzing) {
            AnalyzingOverlay()
        }
        if (uiState is CaptureUiState.Error) {
            val message = stringResource(R.string.analysis_error)
            LaunchedEffect(uiState) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }

        CloseButton(onClick = onClose, modifier = Modifier.align(Alignment.TopStart))
    }
}

@Composable
private fun CloseButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .statusBarsPadding()
            .padding(16.dp)
            .size(48.dp),
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.35f),
        onClick = onClick,
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.camera_close_content_description),
                tint = Color.White,
            )
        }
    }
}

@Composable
private fun CameraPreviewWithCapture(
    enabled: Boolean,
    onPhotoCaptured: (ByteArray) -> Unit,
    onCodeBarresDetecte: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val imageCapture = remember { ImageCapture.Builder().build() }

    // Le lambda capturé par l'analyseur reste stable pendant toute sa vie (remember) ;
    // rememberUpdatedState garantit qu'il appelle toujours la dernière version reçue.
    val onCodeBarresDetecteActuel by rememberUpdatedState(onCodeBarresDetecte)
    val barcodeAnalyzer = remember {
        BarcodeAnalyzer(onCodeBarresDetecte = { code -> onCodeBarresDetecteActuel(code) })
    }
    val imageAnalysis = remember {
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { it.setAnalyzer(ContextCompat.getMainExecutor(context), barcodeAnalyzer) }
    }

    // Réarme la détection à chaque retour sur un aperçu caméra actif (après une erreur
    // ou un "Reprendre") : évite de rester bloqué sur le verrou anti-double-détection.
    LaunchedEffect(enabled) {
        if (enabled) barcodeAnalyzer.reinitialiser()
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener(
                    {
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageCapture,
                            imageAnalysis,
                        )
                    },
                    ContextCompat.getMainExecutor(ctx),
                )
                previewView
            },
        )

        CaptureButton(
            enabled = enabled,
            onClick = {
                coroutineScope.launch {
                    onPhotoCaptured(imageCapture.takePhotoBytes(context))
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp),
        )
    }
}

@Composable
private fun CaptureButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.size(72.dp),
        shape = CircleShape,
        color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        onClick = onClick,
        enabled = enabled,
    ) {}
}

@Composable
private fun AnalyzingOverlay(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.85f)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Text(
            text = stringResource(R.string.analyzing_meal),
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

@Composable
private fun PermissionRationale(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = stringResource(R.string.camera_permission_rationale))
        Button(
            onClick = onRequestPermission,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
        ) {
            Text(text = stringResource(R.string.camera_permission_request))
        }
    }
}
