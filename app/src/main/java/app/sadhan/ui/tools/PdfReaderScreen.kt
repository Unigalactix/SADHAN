package app.sadhan.ui.tools

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.sadhan.media.PdfDocument
import app.sadhan.media.documentName
import app.sadhan.ui.ErrorText
import app.sadhan.ui.DocumentLibrary
import app.sadhan.ui.rememberDocumentIndex
import app.sadhan.data.SettingsStore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

@Composable
fun PdfReaderScreen(store: SettingsStore) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var document by remember { mutableStateOf<PdfDocument?>(null) }
    var title by remember { mutableStateOf("No document selected") }
    var page by remember { mutableIntStateOf(0) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var library by remember { mutableStateOf(false) }
    val documentIndex = rememberDocumentIndex(store.value.documentFolder, library)
    fun display(index: Int) {
        val current = document ?: return
        scope.launch {
            busy = true
            error = null
            try {
                bitmap = withContext(Dispatchers.IO) { current.render(index) }
                page = index
                scale = 1f
                offset = Offset.Zero
            } catch (e: IllegalStateException) { error = "Cannot render PDF: ${e.message}" }
            catch (e: IllegalArgumentException) { error = "Cannot render PDF: ${e.message}" }
            finally { busy = false }
        }
    }
    fun open(uri: android.net.Uri) {
        scope.launch {
            busy = true
            error = null
            bitmap = null
            document?.close()
            document = null
            var pending: PdfDocument? = null
            try {
                val result = withContext(Dispatchers.IO) {
                    val opened = PdfDocument.open(context, uri)
                    pending = opened
                    require(opened.pageCount > 0) { "PDF contains no pages" }
                    Triple(opened, opened.render(0), documentName(context, uri))
                }
                document = result.first
                bitmap = result.second
                title = result.third
                pending = null
                page = 0
                scale = 1f
                offset = Offset.Zero
            } catch (e: IOException) { error = "Cannot open PDF: ${e.message}" }
            catch (e: SecurityException) { error = "PDF is password-protected or access was denied" }
            catch (e: IllegalArgumentException) { error = "Unsupported PDF or non-seekable file. Choose a downloaded local PDF. ${e.message.orEmpty()}" }
            finally { pending?.close(); busy = false }
        }
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> if (uri != null) open(uri) }
    DisposableEffect(Unit) { onDispose { document?.close() } }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Read a local PDF. Pinch to zoom and drag to pan. No recent files or bookmarks are stored.")
        Button(enabled = !busy, onClick = { picker.launch(arrayOf("application/pdf")) }) { Text("Open PDF") }
        OutlinedButton(enabled = !busy, onClick = { library = true }) { Text("PDF & document library") }
        Text(title)
        ErrorText(error)
        document?.let { pdf ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedButton(enabled = !busy && page > 0, onClick = { display(page - 1) }) { Text("Previous") }
                Text("${page + 1} / ${pdf.pageCount}")
                OutlinedButton(enabled = !busy && page + 1 < pdf.pageCount, onClick = { display(page + 1) }) { Text("Next") }
            }
            if (library) AlertDialog(
                onDismissRequest = { library = false }, title = { Text("Your documents") },
                text = { Column(Modifier.verticalScroll(rememberScrollState())) { DocumentLibrary(store, documentIndex) { library = false; open(it) } } },
                confirmButton = { TextButton(onClick = { library = false }) { Text("Done") } },
            )
        }
        if (busy) Text("Rendering...")
        Box(Modifier.weight(1f).fillMaxWidth().clipToBounds().pointerInput(Unit) {
            detectTransformGestures { _, pan, zoom, _ ->
                scale = (scale * zoom).coerceIn(1f, 4f)
                offset = if (scale == 1f) Offset.Zero else offset + pan
            }
        }) {
            bitmap?.let {
                Image(it.asImageBitmap(), "PDF page ${page + 1}", Modifier.fillMaxSize().graphicsLayer {
                    scaleX = scale; scaleY = scale; translationX = offset.x; translationY = offset.y
                })
            }
        }
    }
}
