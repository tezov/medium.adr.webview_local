package com.tezov.medium.adr.webview_local

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewClientCompat

object WebView {

    fun isWebViewAvailable() =
        kotlin.runCatching { CookieManager.getInstance() }.getOrNull() != null

    @Composable
    operator fun invoke(
        modifier: Modifier = Modifier,
        rawHtmlResourceId: Int,
        placeholders: Map<String, String>? = null,
        onUnavailable: @Composable (() -> Unit),
        listener: ((authority: String?) -> Boolean)? = null
    ) {
        if (isWebViewAvailable()) {
            Box(
                modifier = modifier
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = {
                        WebViewRawResource(it, placeholders, rawHtmlResourceId)
                    },
                    update = { it.listener = listener }
                )
            }

        } else {
            onUnavailable()
        }
    }
}

@SuppressLint("ViewConstructor")
class WebViewRawResource internal constructor(
    context: Context,
    placeholders: Map<String, String>? = null,
    rawHtmlResourceId: Int
) :
    WebView(context) {

    companion object {
        val SCHEME_LOCAL = "local"
        val SCHEME_HTTP = "http"
        val SCHEME_HTTPS = "https"
    }

    private val domain = context.packageName
    internal var listener: ((authority: String?) -> Boolean)? = null

    init {
        webChromeClient = WebChromeClient()
        webViewClient = object : WebViewClientCompat() {
            val assetLoader = WebViewAssetLoader.Builder().apply {
                setDomain(domain)
                addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
                addPathHandler("/res/", WebViewAssetLoader.ResourcesPathHandler(context))
            }.build()

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ) = request?.let {
                assetLoader.shouldInterceptRequest(it.url)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                //local handler
                request.url.scheme?.takeIf { scheme ->
                    scheme == SCHEME_LOCAL
                }?.let {
                    listener?.takeIf { it.invoke(request.url.authority) }?.let {
                        return true
                    }
                    return false
                }
                //remote external browser
                request.url.scheme?.takeIf { scheme ->
                    scheme == SCHEME_HTTP || scheme == SCHEME_HTTPS
                }?.let {
                    view.context.startActivity(Intent(Intent.ACTION_VIEW, request.url))
                    return true
                }
                return false
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                val parent = parent as? View
                invalidate()
                parent?.apply {
                    invalidate()
                    requestLayout()
                } ?: run {
                    requestLayout()
                }
            }
        }
        setBackgroundColor(0x00000000)
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        val baseUrl = "$SCHEME_HTTPS://$domain/res/"
        val name = context.resources.getResourceEntryName(rawHtmlResourceId)
        placeholders?.let {
            val content =
                resources.openRawResource(rawHtmlResourceId).bufferedReader().use { it.readText() }
            val buffer = StringBuilder(content)
            placeholders.forEach { placeholders ->
                val key = "\${${placeholders.key}}"
                var index = 0
                while (buffer.indexOf(key, index).also { index = it } > 0) {
                    buffer.replace(index, index + key.length, placeholders.value)
                    index += placeholders.value.length
                }
            }
            loadDataWithBaseURL(baseUrl, buffer.toString(), "text/html", "UTF-8", null)
        } ?: kotlin.run {
            loadUrl("${baseUrl}/raw/$name.html")
        }
    }

}
