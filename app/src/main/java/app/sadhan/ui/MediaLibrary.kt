package app.sadhan.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.LifecycleEventEffect
import app.sadhan.data.SessionIndex
import app.sadhan.media.LibraryItem
import app.sadhan.media.StorageLibrary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class MediaLibraryState(private val context: Context, val video: Boolean) {
    val index = SessionIndex<LibraryItem>()
    val permissions: Array<String> = when {
        Build.VERSION.SDK_INT >= 34 && video -> arrayOf(Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
        Build.VERSION.SDK_INT >= 33 -> arrayOf(if (video) Manifest.permission.READ_MEDIA_VIDEO else Manifest.permission.READ_MEDIA_AUDIO)
        else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    var permissionMask by mutableIntStateOf(-1)
        private set
    var permissionError by mutableStateOf<String?>(null)
        private set
    var query by mutableStateOf("")
    var sort by mutableStateOf("Title")
    val granted get() = permissionMask > 0
    val partial get() = video && Build.VERSION.SDK_INT >= 34 && permissionMask == 2

    fun refreshAccess(fromRequest: Boolean = false) {
        val mask = permissions.withIndex().fold(0) { result, (index, permission) ->
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) result or (1 shl index) else result
        }
        if (mask != permissionMask) { permissionMask = mask; index.clear() }
        if (fromRequest) permissionError = if (granted) null else "Library access denied. Choose files manually, or allow access in Android app settings."
    }
}

@Composable
fun rememberMediaLibrary(video: Boolean): MediaLibraryState {
    val context = LocalContext.current.applicationContext
    val owner = LocalLifecycleOwner.current
    val state = remember(context, video) { MediaLibraryState(context, video) }
    val lifecycle by owner.lifecycle.currentStateFlow.collectAsState()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { state.refreshAccess() }
    DisposableEffect(state) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) { state.index.invalidate() }
        }
        var registered = false
        try {
            context.contentResolver.registerContentObserver(StorageLibrary.mediaUri(video), true, observer)
            registered = true
        } catch (failure: SecurityException) { android.util.Log.w("SADHAN", "Media change observation unavailable; manual rescan remains available") }
        onDispose { if (registered) context.contentResolver.unregisterContentObserver(observer) }
    }
    val active = LocalToolVisible.current && lifecycle.isAtLeast(Lifecycle.State.STARTED)
    LaunchedEffect(state.permissionMask, state.index.generation, active) {
        if (active && state.granted && state.index.needsScan) {
            delay(200)
            state.index.refresh(StorageLibrary.mediaUpdates(context, video))
        }
    }
    return state
}

@Composable
fun MediaLibrary(video: Boolean, state: MediaLibraryState = rememberMediaLibrary(video), onSelect: (List<LibraryItem>, Int) -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { state.refreshAccess(fromRequest = true) }
    Text("On-device library", style = MaterialTheme.typography.titleMedium)
    if (!state.granted) {
        Text("Allow access to preload ${if (video) "videos" else "songs"}, or choose individual files without library access.")
        OutlinedButton(onClick = { launcher.launch(state.permissions) }) { Text("Allow ${if (video) "video" else "music"} library access") }
        TextButton(onClick = { openAppSettings(context) }) { Text("Android app settings") }
    } else {
        Text(if (state.partial) "Selected-video access · ${state.index.snapshot.items.size} files" else "${state.index.snapshot.items.size} accessible files", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(onClick = { state.index.invalidate() }) { Text("Rescan") }
            if (video && Build.VERSION.SDK_INT >= 34) TextButton(onClick = { launcher.launch(state.permissions) }) { Text("Manage access") }
        }
        Input("Search library", state.query, { state.query = it })
        Choice("Sort library", state.sort, listOf("Title", "Details", "Duration")) { state.sort = it }
        IndexProgress(state.index)
        val visible = rememberLibraryResults(state.index.snapshot.items, state.query, state.sort)
        if (!state.index.loading && visible.isEmpty()) Text(if (state.query.isBlank()) "No accessible files yet. Rescan, manage access, or choose files manually." else "No files match this search.")
        LibraryList(visible, onSelect)
    }
    ErrorText(state.permissionError ?: state.index.error)
}

@Composable
fun rememberLibraryResults(items: List<LibraryItem>, query: String, sort: String = "Title"): List<LibraryItem> {
    var visible by remember { mutableStateOf<List<LibraryItem>>(emptyList()) }
    LaunchedEffect(items, query, sort) {
        if (query.isNotBlank()) delay(100)
        visible = withContext(Dispatchers.Default) {
            val matches = items.filter { it.title.contains(query, true) || it.detail.contains(query, true) }
            when (sort) {
                "Duration" -> matches.sortedByDescending { it.duration }
                "Details" -> matches.sortedWith(compareBy<LibraryItem> { it.detail.lowercase() }.thenBy { it.title.lowercase() })
                else -> matches.sortedBy { it.title.lowercase() }
            }
        }
    }
    return visible
}

@Composable
fun IndexProgress(index: SessionIndex<LibraryItem>) {
    if (index.loading) {
        val snapshot = index.snapshot
        Text("Scanning ${snapshot.inspected}${snapshot.total?.let { " of $it" } ?: " items"}…", style = MaterialTheme.typography.labelMedium)
        LinearProgressIndicator(Modifier.fillMaxWidth())
    } else if (index.error != null && index.snapshot.items.isNotEmpty()) Text("Partial results — rescan to retry", color = MaterialTheme.colorScheme.error)
}

@Composable
fun LibraryList(items: List<LibraryItem>, onSelect: (List<LibraryItem>, Int) -> Unit) {
    val focus = androidx.compose.ui.platform.LocalFocusManager.current
    LazyColumn(Modifier.fillMaxWidth().heightIn(max = 360.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        itemsIndexed(items, key = { _, item -> item.uri.toString() }) { index, item ->
            Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = .7f)) {
                Column(Modifier.fillMaxWidth().clickable { focus.clearFocus(); onSelect(items, index) }.padding(16.dp)) {
                    Text(item.title, style = MaterialTheme.typography.bodyMedium)
                    if (item.detail.isNotBlank()) Text(item.detail, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
