package app.sadhan.ui.tools

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.CookieManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.sadhan.core.youtubeId
import app.sadhan.ui.BetaChip
import app.sadhan.ui.ErrorText
import app.sadhan.ui.Input
import java.net.URLEncoder

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubeScreen() {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    var input by remember { mutableStateOf("") }
    var target by remember { mutableStateOf<String?>(null) }
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
            CookieManager.getInstance().setAcceptCookie(false)
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, false)
            setDownloadListener { _, _, _, _, _ -> error = "Downloads are not supported in this beta player" }
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    val host = request.url.host.orEmpty()
                    val allowed = request.url.scheme == "https" &&
                        (host == "youtube.com" || host.endsWith(".youtube.com") || host == "youtube-nocookie.com" || host.endsWith(".youtube-nocookie.com") || host == "youtu.be")
                    if (!allowed) error = "External links and sign-in are not supported in this beta player"
                    return !allowed
                }
                override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) { loading = true }
                override fun onPageFinished(view: WebView, url: String?) { loading = false }
                override fun onReceivedError(view: WebView, request: WebResourceRequest, failure: WebResourceError) {
                    if (request.isForMainFrame) { error = "YouTube could not load: ${failure.description}"; loading = false }
                }
            }
        }
    }
    DisposableEffect(webView, owner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> webView.onPause()
                Lifecycle.Event.ON_RESUME -> webView.onResume()
                else -> Unit
            }
        }
        owner.lifecycle.addObserver(observer)
        onDispose {
            owner.lifecycle.removeObserver(observer)
            webView.stopLoading()
            webView.loadUrl("about:blank")
            webView.clearHistory()
            webView.clearCache(true)
            webView.removeAllViews()
            webView.destroy()
        }
    }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BetaChip()
        Text("Beta — playback can be unstable. Uses YouTube's official embed.", color = MaterialTheme.colorScheme.primary)
        Text("YouTube may show ads or restrict playback. SADHAN adds no ads or tracking.", style = MaterialTheme.typography.bodySmall)
        Input("Video link / ID or search", input, { input = it })
        androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                val id = youtubeId(input)
                if (id == null) error = "Enter a valid YouTube link or 11-character video ID"
                else { error = null; target = "https://www.youtube-nocookie.com/embed/$id" }
            }) { Text("Play link") }
            OutlinedButton(enabled = input.isNotBlank(), onClick = {
                error = null
                target = "https://m.youtube.com/results?search_query=${URLEncoder.encode(input.trim(), "UTF-8")}"
            }) { Text("Search") }
        }
        ErrorText(error)
        if (loading) Text("Loading YouTube...")
        if (target != null) AndroidView(factory = { webView }, modifier = Modifier.fillMaxWidth().weight(1f), update = { view ->
            if (view.tag != target) {
                view.tag = target
                view.loadUrl(target!!, mapOf("Referer" to "https://github.com/Unigalactix/SADHAN"))
            }
        })
    }
}
