package com.adoptme.core.media

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * Copia una foto elegida por el usuario al almacenamiento privado de la app.
 *
 * Se guarda una copia para que la publicación siga mostrando la imagen aunque
 * el permiso temporal del selector de fotos expire o el archivo original se mueva.
 * No se solicita permiso de almacenamiento: se usa el Photo Picker del sistema.
 */
object PetImageStore {

    private const val MAX_BYTES = 15L * 1024 * 1024
    private const val IMAGE_DIR = "pet_images"

    suspend fun copyToInternalStorage(context: Context, uri: Uri): String =
        withContext(Dispatchers.IO) {
            val resolver = context.contentResolver
            val mimeType = resolver.getType(uri) ?: "image/jpeg"
            val extension = when (mimeType) {
                "image/png" -> "png"
                "image/webp" -> "webp"
                else -> "jpg"
            }

            val directory = File(context.filesDir, IMAGE_DIR).apply { mkdirs() }
            val destination = File(directory, "pet_${UUID.randomUUID()}.$extension")

            try {
                val copiedBytes = resolver.openInputStream(uri)?.use { input ->
                    destination.outputStream().use { output ->
                        input.copyTo(output)
                    }
                } ?: error("No se pudo abrir la imagen seleccionada")

                require(copiedBytes in 1..MAX_BYTES) {
                    "La imagen debe pesar menos de 15 MB"
                }
            } catch (error: Exception) {
                destination.delete()
                throw error
            }

            Uri.fromFile(destination).toString()
        }
}
