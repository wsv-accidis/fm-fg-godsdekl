package se.accidis.fmfg.app.ui.references

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import timber.log.Timber

@Composable
fun ReferenceDetailScreen(
    page: ReferencePage,
    onBack: () -> Unit,
) {
    val surfaceColor = MaterialTheme.colorScheme.surface.toArgb()
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        webViewRef?.onResume()
    }
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        webViewRef?.onPause()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(page.titleResId),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        AndroidView(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(surfaceColor)
                    settings.apply {
                        builtInZoomControls = true
                        displayZoomControls = false
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        javaScriptEnabled = false
                        allowFileAccess = false
                        allowContentAccess = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            isAlgorithmicDarkeningAllowed = false
                        }
                    }
                    webViewClient = ReferenceWebViewClient(context)
                    tag = page.assetUrl
                    loadUrl(page.assetUrl)
                    webViewRef = this
                }
            },
            update = { webView ->
                webView.setBackgroundColor(surfaceColor)
                if (webView.tag != page.assetUrl) {
                    webView.tag = page.assetUrl
                    webView.loadUrl(page.assetUrl)
                }
            },
            onRelease = { webView ->
                webView.stopLoading()
                webView.destroy()
                if (webViewRef === webView) {
                    webViewRef = null
                }
            }
        )
    }
}

private class ReferenceWebViewClient(
    private val context: Context,
) : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val uri = request.url
        val url = uri.toString()
        if (url.startsWith(ASSET_URL_PREFIX)) {
            return false
        }

        openExternalUri(context, uri)
        return true
    }

    companion object {
        private const val ASSET_URL_PREFIX = "file:///android_asset/"
    }
}

private fun openExternalUri(context: Context, uri: Uri) {
    val action = when (uri.scheme?.lowercase()) {
        "http", "https" -> Intent.ACTION_VIEW
        "mailto" -> Intent.ACTION_SENDTO
        else -> return
    }
    try {
        context.startActivity(Intent(action, uri))
    } catch (ex: ActivityNotFoundException) {
        Timber.e(ex, "No activity found to handle URI: %s", uri)
    }
}
