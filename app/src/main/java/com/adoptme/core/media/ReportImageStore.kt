package com.adoptme.core.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.max

/**
 * Guarda una sola foto de reporte optimizada para el mapa y la base de datos de AdoptMe.
 *
 * - Reduce a máximo 1280 px.
 * - Corrige orientación EXIF.
 * - Comprime a WebP (~600 KB máximo).
 * - Devuelve solo la ruta local; el binario nunca va a DataStore ni a la base de datos.
 */
object ReportImageStore {

    private const val MAX_DIMENSION = 1280
    private const val MAX_BYTES = 600_000L
    private const val IMAGE_DIR = "report_images"

    suspend fun save(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            error("No se pudo leer la imagen seleccionada")
        }

        val sampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight)
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val original = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: error("No se pudo abrir la imagen seleccionada")

        val oriented = applyExifOrientation(resolver, uri, original)
        val scaled = scaleDown(oriented)

        val directory = File(context.filesDir, IMAGE_DIR).apply { mkdirs() }
        val destination = File(directory, "report_${UUID.randomUUID()}.webp")

        try {
            var quality = 82
            do {
                FileOutputStream(destination).use { output ->
                    scaled.compress(webpFormat(), quality, output)
                }
                quality -= 8
            } while (destination.length() > MAX_BYTES && quality >= 50)

            require(destination.length() in 1..MAX_BYTES) {
                "No se pudo comprimir la imagen del reporte"
            }
        } catch (error: Exception) {
            destination.delete()
            throw error
        } finally {
            if (scaled !== oriented) scaled.recycle()
            if (oriented !== original) oriented.recycle()
            original.recycle()
        }

        Uri.fromFile(destination).toString()
    }

    private fun calculateInSampleSize(width: Int, height: Int): Int {
        var sample = 1
        while (max(width / sample, height / sample) > MAX_DIMENSION * 2) {
            sample *= 2
        }
        return sample
    }

    private fun applyExifOrientation(
        resolver: android.content.ContentResolver,
        uri: Uri,
        bitmap: Bitmap
    ): Bitmap {
        val orientation = runCatching {
            resolver.openInputStream(uri)?.use { input ->
                ExifInterface(input).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } ?: ExifInterface.ORIENTATION_NORMAL
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(270f)
                matrix.postScale(-1f, 1f)
            }
        }

        if (matrix.isIdentity) return bitmap
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun scaleDown(bitmap: Bitmap): Bitmap {
        val largest = max(bitmap.width, bitmap.height)
        if (largest <= MAX_DIMENSION) return bitmap
        val scale = MAX_DIMENSION.toFloat() / largest
        val width = (bitmap.width * scale).toInt().coerceAtLeast(1)
        val height = (bitmap.height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    @Suppress("DEPRECATION")
    private fun webpFormat(): Bitmap.CompressFormat =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Bitmap.CompressFormat.WEBP_LOSSY
        } else {
            Bitmap.CompressFormat.WEBP
        }
}
