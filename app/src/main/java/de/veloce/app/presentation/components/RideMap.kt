package de.veloce.app.presentation.components

import android.annotation.SuppressLint
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import de.veloce.app.domain.model.MapPoint
import java.util.Locale
import android.view.View

enum class MapMode(val js: String) { LIVE("live"), DETAIL("detail") }

private const val TAG = "VeloceMap"

/**
 * Zeigt die Mapbox-Karte aus assets/map.html in einer WebView.
 * - route: bisherige Strecke (LIVE: wächst Punkt für Punkt, DETAIL: komplette Strecke)
 * - speedKmh: aktuelle Geschwindigkeit (steuert, ob die Karte mit der Fahrtrichtung dreht)
 * Die Komponente kennt weder BLE noch das Backend.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RideMap(
    route: List<MapPoint>,
    speedKmh: Double,
    mode: MapMode,
    mapboxToken: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var ready by remember { mutableStateOf(false) }
    var sentCount by remember { mutableStateOf(0) }
    val webView = remember {
        // Debug: Token kommt an? (sollte mit "pk." beginnen)
        Log.d(TAG, "token length=${mapboxToken.length}, startsWithPk=${mapboxToken.startsWith("pk.")}")

        // Debug: ermöglicht chrome://inspect für die WebView
        WebView.setWebContentsDebuggingEnabled(true)

        WebView(context).apply {
            setBackgroundColor(android.graphics.Color.parseColor("#E7E6E1"))
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    super.onPageFinished(view, url)
                    Log.d(TAG, "Seite geladen: $url")
                }

                // Diagnose: jede Netz-Anfrage der Karte loggen (Mapbox + CDN + Fallback)
                override fun shouldInterceptRequest(
                    view: WebView,
                    request: android.webkit.WebResourceRequest,
                ): android.webkit.WebResourceResponse? {
                    try {
                        val u = request.url.toString()
                        if (u.contains("mapbox.com") || u.contains("unpkg.com") ||
                            u.contains("openstreetmap.org") || u.contains("leaflet")
                        ) {
                            Log.d(TAG, "REQ ${request.method} $u")
                        }
                    } catch (_: Exception) {}
                    return super.shouldInterceptRequest(view, request)
                }

                override fun onReceivedError(
                    view: WebView,
                    request: android.webkit.WebResourceRequest,
                    error: android.webkit.WebResourceError,
                ) {
                    Log.d(TAG, "WebView-Fehler: ${error.description} (${request.url})")
                }

                override fun onReceivedHttpError(
                    view: WebView,
                    request: android.webkit.WebResourceRequest,
                    errorResponse: android.webkit.WebResourceResponse,
                ) {
                    Log.d(TAG, "HTTP-Fehler ${errorResponse.statusCode} bei ${request.url}")
                }
            }

            // Debug: JS-Konsole (console.log/error) nach Logcat weiterleiten
            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(m: ConsoleMessage): Boolean {
                    Log.d(TAG, "${m.message()} (${m.sourceId()}:${m.lineNumber()})")
                    return true
                }
            }

            addJavascriptInterface(object {
                @JavascriptInterface
                fun onMapReady() { post { ready = true } }
            }, "Android")
            val html = context.assets.open("map.html").bufferedReader().use { it.readText() }
                .replace("__MAPBOX_TOKEN__", mapboxToken)
            loadDataWithBaseURL("https://appassets.androidplatform.net/", html, "text/html", "utf-8", null)

            setLayerType(View.LAYER_TYPE_HARDWARE, null)
        }
    }

    DisposableEffect(Unit) { onDispose { webView.destroy() } }

    LaunchedEffect(mode, ready) {
        if (ready) webView.evaluateJavascript("veloce.setMode('${mode.js}')", null)
    }

    LaunchedEffect(route.size, ready) {
        if (!ready) return@LaunchedEffect
        when {
            route.size == sentCount + 1 -> {
                val p = route.last()
                webView.evaluateJavascript(
                    String.format(Locale.ROOT, "veloce.addPoint(%.6f,%.6f,%.1f)", p.lng, p.lat, speedKmh), null
                )
            }
            route.size != sentCount -> {
                val json = route.joinToString(",", "[", "]") {
                    String.format(Locale.ROOT, "[%.6f,%.6f]", it.lng, it.lat)
                }
                webView.evaluateJavascript("veloce.setRoute($json)", null)
            }
        }
        sentCount = route.size
    }

    AndroidView(factory = { webView }, modifier = modifier)
}
