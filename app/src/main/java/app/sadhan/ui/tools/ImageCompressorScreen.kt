package app.sadhan.ui.tools

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.sadhan.core.ImageFiles
import app.sadhan.ui.ErrorText
import app.sadhan.ui.ImageActions
import app.sadhan.ui.ResultCard
import app.sadhan.ui.ToolColumn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ImageCompressorScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var image by remember { mutableStateOf<Bitmap?>(null) }
    var originalSize by remember { mutableStateOf<Long?>(null) }
    var bytes by remember { mutableStateOf<ByteArray?>(null) }
    var quality by remember { mutableStateOf(75f) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) scope.launch {
            busy = true
            error = null
            image = null
            bytes = null
            try {
                val decoded = withContext(Dispatchers.IO) {
                    val resolver = context.contentResolver
                    val size = resolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use {
                        if (it.moveToFirst() && !it.isNull(0)) it.getLong(0) else null
                    }
                    val bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, uri)) { decoder, info, _ ->
                        val scale = (2048.0 / maxOf(info.size.width, info.size.height)).coerceAtMost(1.0)
                        decoder.setTargetSize((info.size.width * scale).toInt().coerceAtLeast(1), (info.size.height * scale).toInt().coerceAtLeast(1))
                        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    }
                    bitmap to size
                }
                image = decoded.first
                originalSize = decoded.second
            } catch (e: IOException) { error = "Cannot read photo: ${e.message}" }
            catch (e: IllegalArgumentException) { error = e.message }
            catch (e: SecurityException) { error = "Photo access denied: ${e.message}" }
            finally { busy = false }
        }
    }
    ToolColumn {
        Text("Private, on-device JPEG compression. Large photos are resized to at most 2048 pixels per side. Metadata is removed; transparency becomes black.")
        Button(enabled = !busy, onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) { Text("Choose photo") }
        image?.let { bitmap ->
            Image(bitmap.asImageBitmap(), "Selected photo", Modifier.fillMaxWidth().height(240.dp))
            ResultCard("Original file", originalSize?.let(::fileSize) ?: "Size unavailable from provider")
            Text("Output: ${bitmap.width} × ${bitmap.height} pixels")
            Text("JPEG quality: ${quality.roundToInt()}%")
            Slider(value = quality, onValueChange = { quality = it; bytes = null }, valueRange = 10f..100f, enabled = !busy)
            Button(enabled = !busy, onClick = {
                scope.launch {
                    busy = true
                    error = null
                    try {
                        bytes = withContext(Dispatchers.Default) { ImageFiles.encode(bitmap, Bitmap.CompressFormat.JPEG, quality.roundToInt()) }
                    } catch (e: IOException) { error = e.message }
                    finally { busy = false }
                }
            }) { Text(if (busy) "Working..." else "Compress") }
        }
        bytes?.let {
            ResultCard("Compressed JPEG", fileSize(it.size.toLong()))
            if (originalSize != null && it.size >= originalSize!!) Text("This image did not get smaller. Try a lower quality.")
            ImageActions(it, "image/jpeg", "jpg")
        }
        ErrorText(error)
    }
}

private fun fileSize(size: Long): String = if (size >= 1_048_576) String.format(Locale.getDefault(), "%.2f MB", size / 1048576.0)
    else String.format(Locale.getDefault(), "%.1f KB", size / 1024.0)
