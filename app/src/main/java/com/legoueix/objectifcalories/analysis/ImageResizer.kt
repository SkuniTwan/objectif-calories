package com.legoueix.objectifcalories.analysis

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/**
 * Redimensionnement côté client — premier levier de coût du modèle (voir CLAUDE.md).
 * Non négociable : toute image doit passer par ici avant d'être envoyée à un [MealAnalyzer].
 */
object ImageResizer {

    private const val MAX_DIMENSION = 1000
    private const val JPEG_QUALITY = 85

    fun resizeForAnalysis(raw: ByteArray): ByteArray {
        val orientation = readOrientation(raw)
        val original = BitmapFactory.decodeByteArray(raw, 0, raw.size)
            ?: throw IllegalArgumentException("Image illisible")

        val rotated = applyOrientation(original, orientation)
        val resized = scaleDownToLongSide(rotated, MAX_DIMENSION)

        return ByteArrayOutputStream().use { output ->
            resized.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
            output.toByteArray()
        }
    }

    private fun readOrientation(raw: ByteArray): Int {
        val exif = ExifInterface(ByteArrayInputStream(raw))
        return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    }

    private fun applyOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            else -> return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun scaleDownToLongSide(bitmap: Bitmap, maxLongSide: Int): Bitmap {
        val longSide = maxOf(bitmap.width, bitmap.height)
        if (longSide <= maxLongSide) return bitmap

        val scale = maxLongSide.toFloat() / longSide
        val targetWidth = (bitmap.width * scale).toInt().coerceAtLeast(1)
        val targetHeight = (bitmap.height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }
}
