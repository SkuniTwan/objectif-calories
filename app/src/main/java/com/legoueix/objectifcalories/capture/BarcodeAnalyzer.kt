package com.legoueix.objectifcalories.capture

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Détection de code-barres en continu sur le flux caméra. Un verrou interne évite de
 * déclencher plusieurs fois pour la même étiquette (elle reste visible sur des
 * dizaines de frames) — [reinitialiser] réarme la détection une fois l'écran caméra
 * de nouveau actif (après une erreur ou un "Reprendre").
 */
class BarcodeAnalyzer(
    private val onCodeBarresDetecte: (String) -> Unit,
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient()
    private val dejaDetecte = AtomicBoolean(false)

    fun reinitialiser() {
        dejaDetecte.set(false)
    }

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null || dejaDetecte.get()) {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(image)
            .addOnSuccessListener { codesBarres ->
                val valeur = codesBarres.firstOrNull { !it.rawValue.isNullOrBlank() }?.rawValue
                if (valeur != null && dejaDetecte.compareAndSet(false, true)) {
                    onCodeBarresDetecte(valeur)
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}
