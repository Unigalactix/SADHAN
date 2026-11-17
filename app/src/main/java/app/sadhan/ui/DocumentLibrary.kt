package app.sadhan.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import app.sadhan.data.SettingsStore
import app.sadhan.media.LibraryItem
import app.sadhan.media.StorageLibrary
import java.io.IOException

@Composable
fun DocumentLibrary(store: SettingsStore, openPdf: (Uri) -> Unit) {
    val context = LocalContext.current
    val folder = store.value.documentFolder
    var entries by remember { mutableStateOf<List<LibraryItem>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var revision by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                store.value.documentFolder?.takeIf { it != uri.toString() }?.let { previous ->
                    try { context.contentResolver.releasePersistableUriPermission(Uri.parse(previous), Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                    catch (e: SecurityException) { android.util.Log.w("SADHAN", "Previous document-folder permission was already unavailable", e) }
                }
                store.update(store.value.copy(documentFolder = uri.toString()))
                error = null
            } catch (e: SecurityException) { error = "Folder permission could not be retained: ${e.message}" }
        }
    }
    LaunchedEffect(folder, revision) {
        entries = emptyList()
        if (folder != null) {
            loading = true
            error = null
            try { entries = StorageLibrary.documents(context, Uri.parse(folder)) }
            catch (e: IOException) { error = "Folder scan failed: ${e.message}" }
            catch (e: SecurityException) { error = "Folder access was revoked. Choose the folder again." }
            catch (e: IllegalArgumentException) { error = "Invalid document folder: ${e.message}" }
            finally { loading = false }
        }
    }
    Text("Document library", style = MaterialTheme.typography.titleMedium)
    Text("Choose a folder to preload its PDFs and documents. The folder choice is a Setting; the index and file history are never saved. PDFs open here; other formats open in an installed viewer.")
    OutlinedButton(onClick = { picker.launch(null) }) { Text("Choose document folder") }
    if (folder != null) {
        TextButton(onClick = { revision++ }) { Text("Rescan folder") }
        Input("Search documents", query, { query = it })
        if (loading) LinearProgressIndicator()
        val visible = entries.filter { it.title.contains(query, true) }
        if (!loading && visible.isEmpty()) Text("No matching supported documents in this folder.")
        LibraryList(visible) { items, index ->
            val item = items[index]
            if (item.mime == "application/pdf" || item.title.endsWith(".pdf", true)) openPdf(item.uri)
            else {
                try {
                    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(item.uri, item.mime.ifBlank { "application/octet-stream" })
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }, "Open document"))
                } catch (_: ActivityNotFoundException) { error = "No installed app supports this document type" }
                catch (e: SecurityException) { error = "Document access denied: ${e.message}" }
            }
        }
    }
    ErrorText(error)
}
