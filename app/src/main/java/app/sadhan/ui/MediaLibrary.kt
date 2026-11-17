package app.sadhan.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.lifecycle.compose.LifecycleEventEffect
import app.sadhan.media.LibraryItem
import app.sadhan.media.StorageLibrary
import java.io.IOException

@Composable
fun MediaLibrary(video: Boolean, onSelect: (List<LibraryItem>, Int) -> Unit) {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(false) }
    var revision by remember { mutableIntStateOf(0) }
    var entries by remember { mutableStateOf<List<LibraryItem>>(emptyList()) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    val permissions = remember(video) {
        when {
            Build.VERSION.SDK_INT >= 34 && video -> arrayOf(Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
            Build.VERSION.SDK_INT >= 33 -> arrayOf(if (video) Manifest.permission.READ_MEDIA_VIDEO else Manifest.permission.READ_MEDIA_AUDIO)
            else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
    fun refreshPermission() {
        granted = permissions.any { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        refreshPermission()
        if (!granted) error = "Library access denied. Choose files manually, or enable access in Android app settings."
        revision++
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { refreshPermission(); revision++ }
    LaunchedEffect(granted, revision) {
        if (granted) {
            busy = true
            error = null
            try { entries = StorageLibrary.media(context, video) }
            catch (e: IOException) { error = e.message }
            catch (e: SecurityException) { entries = emptyList(); error = "Media access was denied. Allow access or choose files individually." }
            finally { busy = false }
        } else entries = emptyList()
    }
    Text("On-device library", style = MaterialTheme.typography.titleMedium)
    if (!granted) {
        Text("Allow access to preload ${if (video) "videos" else "songs"}, or use Choose ${if (video) "video" else "audio"} to select files without library access.")
        OutlinedButton(onClick = { launcher.launch(permissions) }) { Text("Allow ${if (video) "video" else "music"} library access") }
        TextButton(onClick = { openAppSettings(context) }) { Text("Android app settings") }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("${entries.size} accessible files", Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
            TextButton(onClick = { revision++ }) { Text("Rescan") }
            if (video && Build.VERSION.SDK_INT >= 34) TextButton(onClick = { launcher.launch(permissions) }) { Text("Manage access") }
        }
        Input("Search library", query, { query = it })
        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        val visible = entries.filter { it.title.contains(query, true) }
        if (!busy && visible.isEmpty()) Text("No matching files. Check access, rescan, or choose files manually.")
        LibraryList(visible, onSelect)
    }
    ErrorText(error)
}

@Composable
fun LibraryList(items: List<LibraryItem>, onSelect: (List<LibraryItem>, Int) -> Unit) {
    val focus = androidx.compose.ui.platform.LocalFocusManager.current
    LazyColumn(Modifier.fillMaxWidth().heightIn(max = 320.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
