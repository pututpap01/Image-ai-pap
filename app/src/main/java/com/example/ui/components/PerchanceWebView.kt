package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

class PerchanceWebBridge(
    private val onImageGeneratedCallback: (String) -> Unit,
    private val onStatusUpdateCallback: (String) -> Unit
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun onImageGenerated(url: String) {
        Log.d("PerchanceBridge", "Image generated: $url")
        mainHandler.post {
            onImageGeneratedCallback(url)
        }
    }

    @JavascriptInterface
    fun onStatusUpdate(status: String) {
        Log.d("PerchanceBridge", "Status update: $status")
        mainHandler.post {
            onStatusUpdateCallback(status)
        }
    }

    @JavascriptInterface
    fun postMessageFromIframe(jsonMessage: String) {
        Log.d("PerchanceBridge", "PostMessage received: $jsonMessage")
        mainHandler.post {
            onStatusUpdateCallback("Message from frame: $jsonMessage")
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PerchanceWebView(
    webViewUrl: String = "https://perchance.org/ai-text-to-image-generator",
    onWebViewCreated: (WebView) -> Unit,
    onImageGenerated: (String) -> Unit,
    onStatusChange: (String) -> Unit,
    isExpanded: Boolean = false,
    modifier: Modifier = Modifier
) {
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    val bridge = remember {
        PerchanceWebBridge(
            onImageGeneratedCallback = onImageGenerated,
            onStatusUpdateCallback = onStatusChange
        )
    }

    Column(modifier = modifier) {
        // WebView Status & Controls Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Perchance AI Iframe Engine",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                IconButton(
                    onClick = {
                        webViewInstance?.reload()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Muat Ulang Webview",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Main WebView Frame Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isExpanded) 420.dp else 240.dp)
                .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                )
                .background(Color.Black)
        ) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            allowFileAccess = true
                            setSupportZoom(true)
                            builtInZoomControls = true
                            displayZoomControls = false
                            userAgentString =
                                "Mozilla/5.0 (Linux; Android 13; Pixel 7 Pro) AppleWebKit/537.36 (KHTML, Gecko) Chrome/116.0.0.0 Mobile Safari/537.36"
                        }

                        addJavascriptInterface(bridge, "AndroidBridge")

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                isLoading = true
                                hasError = false
                                onStatusChange("Memuat halaman Perchance AI...")
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                                onStatusChange("Selesai memuat Iframe Perchance")

                                // Inject CSS to customize and clean up Perchance Page layout
                                val customCss = """
                                    header, footer, .ad-container, .top-bar, #top-bar, .nav-bar, 
                                    .social-links, .description-text, .comments-container {
                                        display: none !important;
                                    }
                                    body {
                                        background-color: #0F172A !important;
                                        color: #F8FAFC !important;
                                        font-family: sans-serif !important;
                                        margin: 0 !important;
                                        padding: 8px !important;
                                    }
                                    iframe {
                                        border-radius: 12px !important;
                                        width: 100% !important;
                                    }
                                """.trimIndent().replace("\n", "")

                                val injectCssJs = """
                                    (function() {
                                        var style = document.createElement('style');
                                        style.type = 'text/css';
                                        style.innerHTML = '$customCss';
                                        document.head.appendChild(style);

                                        // Listen for postMessage from inner iframe or window
                                        window.addEventListener('message', function(event) {
                                            if (window.AndroidBridge && event.data) {
                                                window.AndroidBridge.postMessageFromIframe(JSON.stringify(event.data));
                                            }
                                        });

                                        // Set up image observer to detect newly generated images in DOM
                                        var observer = new MutationObserver(function(mutations) {
                                            mutations.forEach(function(mutation) {
                                                mutation.addedNodes.forEach(function(node) {
                                                    if (node.tagName === 'IMG' && node.src && node.src.indexOf('data:image') === -1) {
                                                        if (window.AndroidBridge) {
                                                            window.AndroidBridge.onImageGenerated(node.src);
                                                        }
                                                    } else if (node.querySelectorAll) {
                                                        var imgs = node.querySelectorAll('img');
                                                        imgs.forEach(function(img) {
                                                            if (img.src && img.src.length > 20) {
                                                                if (window.AndroidBridge) {
                                                                    window.AndroidBridge.onImageGenerated(img.src);
                                                                }
                                                            }
                                                        });
                                                    }
                                                });
                                            });
                                        });
                                        observer.observe(document.body, { childList: true, subtree: true });
                                    })();
                                """.trimIndent()

                                view?.evaluateJavascript(injectCssJs, null)
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                super.onReceivedError(view, request, error)
                                if (request?.isForMainFrame == true) {
                                    isLoading = false
                                    hasError = true
                                    errorMessage = error?.description?.toString() ?: "Gagal memuat koneksi web"
                                    onStatusChange("Error: $errorMessage")
                                }
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                super.onProgressChanged(view, newProgress)
                                if (newProgress == 100) {
                                    isLoading = false
                                }
                            }
                        }

                        loadUrl(webViewUrl)
                        webViewInstance = this
                        onWebViewCreated(this)
                    }
                },
                update = { view ->
                    webViewInstance = view
                },
                modifier = Modifier.fillMaxSize()
            )

            if (hasError) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Koneksi Iframe Perchance Terkendala",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Helper function to trigger generation inside Perchance WebView via JS injection.
 */
fun triggerPerchanceGenerationInWebView(
    webView: WebView?,
    prompt: String,
    styleSuffix: String,
    negativePrompt: String,
    aspectRatio: String
) {
    if (webView == null) return

    val combinedPrompt = (prompt + " " + styleSuffix).trim().replace("'", "\\'").replace("\n", " ")
    val escapedNeg = negativePrompt.trim().replace("'", "\\'").replace("\n", " ")

    val jsScript = """
        (function() {
            // Find input elements inside current document or iframe
            function findAndFillInput(doc) {
                var textareas = doc.querySelectorAll('textarea, input[type="text"]');
                for (var i = 0; i < textareas.length; i++) {
                    var el = textareas[i];
                    if (el.placeholder && el.placeholder.toLowerCase().indexOf('negative') !== -1) {
                        el.value = '$escapedNeg';
                        el.dispatchEvent(new Event('input', { bubbles: true }));
                    } else if (el.id !== 'search' && !el.readOnly) {
                        el.value = '$combinedPrompt';
                        el.dispatchEvent(new Event('input', { bubbles: true }));
                    }
                }

                // Find generate button
                var buttons = doc.querySelectorAll('button, input[type="submit"], .generate-btn, #generateButton');
                for (var j = 0; j < buttons.length; j++) {
                    var btn = buttons[j];
                    var txt = (btn.innerText || btn.value || '').toLowerCase();
                    if (txt.indexOf('generate') !== -1 || txt.indexOf('buat') !== -1 || txt.indexOf('draw') !== -1 || btn.id === 'generateButton') {
                        btn.click();
                        if (window.AndroidBridge) {
                            window.AndroidBridge.onStatusUpdate('Perintah Generate dikirim ke Perchance Frame');
                        }
                        return true;
                    }
                }
                return false;
            }

            var success = findAndFillInput(document);
            if (!success) {
                // Try looking into iframes
                var iframes = document.querySelectorAll('iframe');
                for (var k = 0; k < iframes.length; k++) {
                    try {
                        var iframeDoc = iframes[k].contentDocument || iframes[k].contentWindow.document;
                        if (iframeDoc) {
                            findAndFillInput(iframeDoc);
                        }
                    } catch(e) {
                        console.log('Cross-origin iframe access blocked', e);
                    }
                }
            }
        })();
    """.trimIndent()

    webView.evaluateJavascript(jsScript, null)
}
