package app.sadhan.ui.tools

import android.annotation.SuppressLint
import android.webkit.*
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.sadhan.core.youtubeId
import app.sadhan.ui.*
import java.net.URLEncoder

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun YouTubeScreen() {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    var query by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("") }
    var dialog by remember { mutableStateOf<String?>(null) }
    var target by remember { mutableStateOf<String?>(null) }
    var results by remember { mutableStateOf<String?>(null) }
    var selected by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val webView = remember {
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            settings.domStorageEnabled = false
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            settings.mediaPlaybackRequiresUserGesture = true
            importantForAutofill = android.view.View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
            CookieManager.getInstance().setAcceptCookie(false)
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, false)
            setDownloadListener { _, _, _, _, _ -> error = "Downloads are not supported by the official embed" }
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    if (!request.isForMainFrame) return false
                    val host = request.url.host.orEmpty()
                    val allowed = request.url.scheme == "https" &&
                        (host == "youtube.com" || host.endsWith(".youtube.com") || host == "youtube-nocookie.com" || host.endsWith(".youtube-nocookie.com") || host == "youtu.be")
                    if (!allowed) { error = "External links and sign-in are unavailable in this player"; return true }
                    val id = youtubeId(request.url.toString())
                    if (id != null && !request.url.path.orEmpty().startsWith("/embed/")) {
                        selected = id
                        error = null
                        target = "https://www.youtube-nocookie.com/embed/$id"
                        return true
                    }
                    return false
                }
                override fun onPageStarted(view: WebView, url: String?, favicon: android.graphics.Bitmap?) { loading = true }
                override fun onPageFinished(view: WebView, url: String?) { loading = false }
                override fun onReceivedError(view: WebView, request: WebResourceRequest, failure: WebResourceError) {
                    if (request.isForMainFrame) { error = "YouTube could not load: ${failure.description}"; loading = false }
                }
                override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, response: WebResourceResponse) {
                    if (request.isForMainFrame) error = "YouTube returned HTTP ${response.statusCode}. Availability depends on YouTube and your region."
                }
            }
        }
    }
    DisposableEffect(webView, owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                webView.evaluateJavascript("document.querySelectorAll('video,audio').forEach(function(media){media.pause();})", null)
                webView.onPause()
            }
            if (event == Lifecycle.Event.ON_RESUME) webView.onResume()
        }
        owner.lifecycle.addObserver(observer)
        onDispose {
            owner.lifecycle.removeObserver(observer)
            webView.stopLoading()
            webView.clearHistory()
            webView.clearCache(true)
            webView.destroy()
        }
    }
    BackHandler(selected != null && results != null) { selected = null; target = results }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BetaChip()
            OutlinedButton(onClick = { error = null; dialog = "search" }) { Text("Search YouTube") }
            OutlinedButton(onClick = { error = null; dialog = "link" }) { Text("Paste link") }
        }
        Text("Beta — playback can be unstable. Uses YouTube's official embed.", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
        selected?.let { id ->
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = .7f)) {
                Row(Modifier.fillMaxWidth().padding(12.dp)) {
                    Text("Selected video: $id", Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                    if (results != null) TextButton(onClick = { selected = null; target = results }) { Text("Back to results") }
                }
            }
        }
        ErrorText(error)
        if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (target == null) {
            Text("Search, choose a result, and watch using the official player. No API key, unofficial scraping or video extraction.")
            Text("YouTube may show ads, require cookies for some content, or restrict embeds. SADHAN adds no ads or tracking.", style = MaterialTheme.typography.bodySmall)
        } else AndroidView(factory = { webView }, modifier = Modifier.fillMaxWidth().weight(1f), update = { view ->
            if (view.tag != target) {
                view.tag = target
                target?.let { view.loadUrl(it, mapOf("Referer" to "https://github.com/Unigalactix/SADHAN")) }
            }
        })
    }
    if (dialog != null) AlertDialog(
        onDismissRequest = { dialog = null },
        title = { Text(if (dialog == "search") "Search YouTube" else "Open video link") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (dialog == "search") Input("Search query", query, { query = it })
                else Input("YouTube link or video ID", link, { link = it })
                ErrorText(error)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (dialog == "search") {
                    if (query.isBlank()) error = "Enter a search query"
                    else {
                        results = "https://m.youtube.com/results?search_query=${URLEncoder.encode(query.trim(), "UTF-8")}"
                        selected = null; target = results; error = null; dialog = null
                    }
                } else {
                    val id = youtubeId(link)
                    if (id == null) error = "Enter a valid YouTube link or 11-character video ID"
                    else { selected = id; target = "https://www.youtube-nocookie.com/embed/$id"; error = null; dialog = null }
                }
            }) { Text(if (dialog == "search") "Search" else "Select video") }
        },
        dismissButton = { TextButton(onClick = { dialog = null }) { Text("Cancel") } },
    )
}
