package app.sadhan.media

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.webkit.*
import androidx.compose.runtime.*

@SuppressLint("SetJavaScriptEnabled")
class PrivateBrowserTab(context: Context) {
    val id: String = java.util.UUID.randomUUID().toString()
    var title by mutableStateOf("New private tab")
        private set
    var url by mutableStateOf("")
        private set
    var loading by mutableStateOf(false)
        private set
    var back by mutableStateOf(false)
        private set
    var forward by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var stopped by mutableStateOf(false)
        private set
    val view = WebView(context).apply {
        importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
        settings.apply {
            javaScriptEnabled = true
            cacheMode = WebSettings.LOAD_NO_CACHE
            domStorageEnabled = false
            databaseEnabled = false
            saveFormData = false
            allowFileAccess = false
            allowContentAccess = false
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            mediaPlaybackRequiresUserGesture = true
            setGeolocationEnabled(false)
            safeBrowsingEnabled = true
            setSupportMultipleWindows(false)
        }
        CookieManager.getInstance().setAcceptCookie(false)
        CookieManager.getInstance().setAcceptThirdPartyCookies(this, false)
        clearFormData()
        setDownloadListener { _, _, _, _, _ -> error = "Downloads are disabled in private tabs" }
        webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView, value: String?) { if (!stopped) this@PrivateBrowserTab.title = value?.take(100) ?: "Private tab" }
            override fun onPermissionRequest(request: PermissionRequest) {
                request.deny()
                error = "Website camera and microphone requests are blocked"
            }
            override fun onGeolocationPermissionsShowPrompt(origin: String?, callback: GeolocationPermissions.Callback) {
                callback.invoke(origin, false, false)
                error = "Website location requests are blocked"
            }
            override fun onJsAlert(view: WebView, url: String?, message: String?, result: JsResult): Boolean {
                result.cancel(); error = "Website alert dialogs are blocked in private tabs"; return true
            }
            override fun onJsConfirm(view: WebView, url: String?, message: String?, result: JsResult): Boolean {
                result.cancel(); error = "Website confirmation dialogs are blocked in private tabs"; return true
            }
        }
        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                if (request.isForMainFrame && request.url.scheme != "https") {
                    error = "Only HTTPS navigation is allowed; app links, downloads and unsafe schemes are blocked"
                    return true
                }
                return false
            }
            override fun onPageStarted(view: WebView, value: String, favicon: Bitmap?) { if (!stopped) { this@PrivateBrowserTab.url = value; loading = true; error = null } }
            override fun onPageFinished(view: WebView, value: String) {
                if (!stopped) { this@PrivateBrowserTab.url = value; loading = false; back = view.canGoBack(); forward = view.canGoForward() }
            }
            override fun onReceivedError(view: WebView, request: WebResourceRequest, failure: WebResourceError) {
                if (request.isForMainFrame) { error = "Page could not load: ${failure.description}"; loading = false }
            }
            override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, response: WebResourceResponse) {
                if (request.isForMainFrame) error = "Website returned HTTP ${response.statusCode}"
            }
            override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, failure: android.net.http.SslError) {
                handler.cancel(); error = "Certificate validation failed. This connection was blocked."; loading = false
            }
            override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
                error = "This tab's renderer stopped. Close the tab and open a new private tab."
                close()
                return true
            }
        }
    }

    fun load(address: String) {
        check(!stopped) { "This renderer has stopped. Open a replacement tab." }
        error = null
        view.loadUrl(address)
    }

    fun pause() {
        if (!stopped) {
            view.evaluateJavascript("document.querySelectorAll('video,audio').forEach(function(media){media.pause();})", null)
            view.onPause()
        }
    }

    fun resume() { if (!stopped) view.onResume() }
    fun goBack() { check(!stopped) { "This renderer has stopped. Open a replacement tab." }; view.goBack() }
    fun goForward() { check(!stopped) { "This renderer has stopped. Open a replacement tab." }; view.goForward() }
    fun reload() { check(!stopped) { "This renderer has stopped. Open a replacement tab." }; view.reload() }
    fun close() {
        if (!stopped) {
            stopped = true
            loading = false
            back = false
            forward = false
            view.stopLoading()
            view.clearHistory()
            view.clearFormData()
            view.clearCache(true)
            val parent = view.parent
            if (parent is android.view.ViewGroup) parent.removeView(view)
            view.destroy()
        }
    }
}
