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
import app.sadhan.data.SessionIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.LifecycleEventEffect

@Composable
fun rememberDocumentIndex(folder: String?, enabled: Boolean): SessionIndex<LibraryItem> {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val lifecycle by owner.lifecycle.currentStateFlow.collectAsState()
    val index = remember(folder) { SessionIndex<LibraryItem>() }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (folder != null && context.contentResolver.persistedUriPermissions.none { it.uri.toString() == folder && it.isReadPermission }) index.accessDenied()
    }
    DisposableEffect(folder, index) {
        val observer = object : android.database.ContentObserver(android.os.Handler(android.os.Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) { index.invalidate() }
        }
        var registered = false
        if (folder != null) {
            try { context.contentResolver.registerContentObserver(Uri.parse(folder), true, observer); registered = true }
            catch (failure: SecurityException) { index.accessDenied() }
        }
        onDispose { if (registered) context.contentResolver.unregisterContentObserver(observer) }
    }
    LaunchedEffect(folder, enabled, index.generation, lifecycle) {
        if (folder != null && enabled && lifecycle.isAtLeast(Lifecycle.State.STARTED) && index.needsScan &&
            context.contentResolver.persistedUriPermissions.any { it.uri.toString() == folder && it.isReadPermission }) {
            index.refresh(StorageLibrary.documentUpdates(context, Uri.parse(folder)))
        }
    }
    return index
}

@Composable
fun DocumentLibrary(store: SettingsStore, index: SessionIndex<LibraryItem> = rememberDocumentIndex(store.value.documentFolder, true), openPdf: (Uri) -> Unit) {
    val context = LocalContext.current
    val folder = store.value.documentFolder
    var query by remember { mutableStateOf("") }
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
    Text("Document library", style = MaterialTheme.typography.titleMedium)
    Text("Choose a folder to preload its PDFs and documents. The folder choice is a Setting; the index and file history are never saved. PDFs open here; other formats open in an installed viewer.")
    OutlinedButton(onClick = { picker.launch(null) }) { Text("Choose document folder") }
    if (folder != null) {
        TextButton(onClick = { index.invalidate() }) { Text("Rescan folder") }
        Input("Search documents", query, { query = it })
        IndexProgress(index)
        val visible = rememberLibraryResults(index.snapshot.items, query)
        if (!index.loading && visible.isEmpty()) Text(if (query.isBlank()) "No supported documents in this folder." else "No documents match this search.")
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
    ErrorText(error ?: index.error)
}
