package de.veloce.app.presentation.components

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
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
import java.util.Locale

data class MapPoint(val lat: Double, val lng: Double)

enum class MapMode(val js: String) { LIVE("live"), DETAIL("detail") }

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
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = WebViewClient()
            addJavascriptInterface(object {
                @JavascriptInterface
                fun onMapReady() { post { ready = true } }
            }, "Android")
            val html = context.assets.open("map.html").bufferedReader().use { it.readText() }
                .replace("__MAPBOX_TOKEN__", mapboxToken)
            loadDataWithBaseURL("https://appassets.androidplatform.net/", html, "text/html", "utf-8", null)
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
