package app.sadhan.core

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

object ImageFiles {
    suspend fun save(context: Context, bytes: ByteArray, mime: String, extension: String): Uri =
        withContext(Dispatchers.IO) {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "SADHAN-${System.currentTimeMillis()}.$extension")
                put(MediaStore.Images.Media.MIME_TYPE, mime)
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/SADHAN")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: throw IOException("Could not create image")
            try {
                (resolver.openOutputStream(uri) ?: throw IOException("Could not open image")).use { it.write(bytes) }
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                if (resolver.update(uri, values, null, null) != 1) throw IOException("Could not finish saving image")
                uri
            } catch (e: Exception) {
                resolver.delete(uri, null, null)
                throw e
            }
        }

    suspend fun share(context: Context, bytes: ByteArray, mime: String, extension: String) {
        val uri = withContext(Dispatchers.IO) {
            val directory = File(context.cacheDir, "shared_images")
            if (!directory.exists() && !directory.mkdirs()) throw IOException("Could not create sharing directory")
            val file = File(directory, "sadhan-${System.currentTimeMillis()}.$extension")
            file.outputStream().use { it.write(bytes) }
            FileProvider.getUriForFile(context, "${context.packageName}.files", file)
        }
        withContext(Dispatchers.Main.immediate) {
            context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = mime
                putExtra(Intent.EXTRA_STREAM, uri)
                clipData = android.content.ClipData.newRawUri("SADHAN image", uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }, "Share image"))
        }
    }

    fun encode(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int): ByteArray =
        java.io.ByteArrayOutputStream().use { stream ->
            if (!bitmap.compress(format, quality, stream)) throw IOException("Could not compress image")
            stream.toByteArray()
        }
}
