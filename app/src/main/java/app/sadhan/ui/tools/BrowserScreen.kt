package app.sadhan.ui.tools

import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebStorage
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.sadhan.core.browserDestination
import app.sadhan.media.PrivateBrowserTab
import app.sadhan.ui.ErrorText
import app.sadhan.ui.Input
import app.sadhan.ui.BetaChip

@Composable
fun BrowserScreen() {
    val context = LocalContext.current
    val focus = androidx.compose.ui.platform.LocalFocusManager.current
    val owner = LocalLifecycleOwner.current
    val tabs = remember {
        CookieManager.getInstance().removeAllCookies(null)
        WebStorage.getInstance().deleteAllData()
        mutableStateListOf(PrivateBrowserTab(context))
    }
    var active by remember { mutableIntStateOf(0) }
    var address by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val current = tabs[active]
    fun act(action: () -> Unit) {
        try { action() } catch (failure: IllegalStateException) { error = failure.message }
    }
    LaunchedEffect(current, current.url) { address = current.url }
    DisposableEffect(current, owner) {
        current.resume()
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) current.pause()
            if (event == Lifecycle.Event.ON_RESUME) current.resume()
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer); current.pause() }
    }
    DisposableEffect(Unit) {
        onDispose {
            tabs.forEach(PrivateBrowserTab::close)
            CookieManager.getInstance().removeAllCookies(null)
            WebStorage.getInstance().deleteAllData()
        }
    }
    BackHandler(!current.stopped && current.back) { act(current::goBack) }
    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BetaChip()
        Text("PRIVATE SESSION · Not anonymous", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(tabs, key = { _, tab -> tab.id }) { index, tab ->
                InputChip(
                    selected = index == active,
                    onClick = { active = index },
                    label = { Text(tab.title, Modifier.widthIn(max = 110.dp), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    trailingIcon = {
                        IconButton(onClick = {
                            val removed = tabs.removeAt(index)
                            if (tabs.isEmpty()) tabs.add(PrivateBrowserTab(context))
                            active = when {
                                index < active -> active - 1
                                active > tabs.lastIndex -> tabs.lastIndex
                                else -> active
                            }
                            removed.close()
                        }) { Icon(Icons.Default.Close, "Close private tab") }
                    },
                )
            }
            item {
                IconButton(enabled = tabs.size < 8, onClick = { tabs.add(PrivateBrowserTab(context)); active = tabs.lastIndex }) {
                    Icon(Icons.Default.Add, "New private tab")
                }
            }
        }
        if (tabs.size == 8) Text("8-tab memory limit reached. Close a tab to open another.", style = MaterialTheme.typography.labelSmall)
        Input("HTTPS address or search", address, { address = it })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(enabled = !current.stopped && current.back, onClick = { act(current::goBack) }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Browser back") }
            IconButton(enabled = !current.stopped && current.forward, onClick = { act(current::goForward) }) { Icon(Icons.AutoMirrored.Filled.ArrowForward, "Browser forward") }
            IconButton(enabled = !current.stopped && current.url.isNotEmpty(), onClick = { act(current::reload) }) { Icon(Icons.Default.Refresh, "Reload page") }
            Button(enabled = !current.stopped, onClick = {
                try { current.load(browserDestination(address)); error = null; focus.clearFocus() }
                catch (e: IllegalArgumentException) { error = e.message }
                catch (e: IllegalStateException) { error = e.message }
            }) { Text("Go") }
        }
        ErrorText(if (current.stopped) current.error else error ?: current.error)
        if (current.stopped) {
            Text("This tab is stopped. Its old renderer cannot receive navigation commands.")
            Button(onClick = {
                val destination = current.url
                current.close()
                val replacement = PrivateBrowserTab(context)
                tabs[active] = replacement
                error = null
                if (destination.isNotBlank()) {
                    try { replacement.load(browserDestination(destination)) }
                    catch (failure: IllegalArgumentException) { error = failure.message }
                }
            }) { Text("Open replacement tab") }
        }
        if (current.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (current.url.isEmpty()) {
            Text("Tabs and history exist only during this browser session. Cookies, DOM/form storage and downloads are disabled; web data is cleared on exit.")
            Text("Websites still see your IP and may fingerprint you. Android/WebView can retain operational data; this is not a forensic-safe or anonymous browser.", style = MaterialTheme.typography.bodySmall)
        }
        if (!current.stopped) key(current) {
            AndroidView(
                factory = {
                    current.view.also { view -> val parent = view.parent; if (parent is ViewGroup) parent.removeView(view) }
                },
                modifier = Modifier.fillMaxWidth().weight(1f),
            )
        }
    }
}
