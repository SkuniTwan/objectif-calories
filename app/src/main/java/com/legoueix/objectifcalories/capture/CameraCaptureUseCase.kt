package com.legoueix.objectifcalories.capture

import android.content.Context
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.core.content.ContextCompat
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Déclenche une capture CameraX et renvoie les octets JPEG bruts (avant tout
 * redimensionnement — voir [com.legoueix.objectifcalories.analysis.ImageResizer]).
 */
suspend fun ImageCapture.takePhotoBytes(context: Context): ByteArray =
    suspendCoroutine { continuation ->
        takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val bytes = image.toJpegByteArray()
                    image.close()
                    continuation.resume(bytes)
                }

                override fun onError(exception: ImageCaptureException) {
                    continuation.resumeWithException(exception)
                }
            },
        )
    }

private fun ImageProxy.toJpegByteArray(): ByteArray {
    val buffer = planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return bytes
}
