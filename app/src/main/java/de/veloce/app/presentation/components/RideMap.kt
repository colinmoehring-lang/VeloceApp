package de.veloce.app.presentation.components

import android.annotation.SuppressLint
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import de.veloce.app.BuildConfig
import de.veloce.app.domain.model.MapPoint
import de.veloce.app.domain.model.TelemetryPoint
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun RideMap(
    route: List<MapPoint>,
    currentPoint: TelemetryPoint?,
    speedKmh: Double,
    modifier: Modifier = Modifier,
) {
    val accessToken = BuildConfig.MAPBOX_ACCESS_TOKEN.trim()
    if (accessToken.isEmpty()) {
        Surface(modifier = modifier, color = MaterialTheme.colorScheme.surfaceVariant) {
            Text(
                text = "Die Karte ist nicht konfiguriert. Setze MAPBOX_ACCESS_TOKEN in local.properties oder als Gradle-Property.",
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        return
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapReady = remember(context, lifecycleOwner) { mutableStateOf(false) }
    val destroyed = remember(context, lifecycleOwner) { mutableStateOf(false) }
    val mapError = remember(context, lifecycleOwner) { mutableStateOf<String?>(null) }
    val latestRoute = rememberUpdatedState(route)
    val latestPoint = rememberUpdatedState(currentPoint)
    val latestSpeed = rememberUpdatedState(speedKmh)
    val tokenArgument = JSONObject.quote(accessToken)

    fun reportMapError(message: String) {
        val safeMessage = message.replace(accessToken, "[redacted]").take(240)
        if (mapError.value == null) {
            mapError.value = safeMessage
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    val webView = remember(context, lifecycleOwner) {
        WebView(context).apply {
            layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            )
            setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
            setBackgroundColor(0xFF0B0D12.toInt())
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                    if (consoleMessage.messageLevel() == ConsoleMessage.MessageLevel.ERROR) {
                        reportMapError("Map script error: ${consoleMessage.message()}")
                    }
                    return true
                }
            }
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    if (!destroyed.value) {
                        view.evaluateJavascript(
                            """
                                (function() {
                                  if (typeof window.initializeMapbox !== 'function') {
                                    window.RideMapBridge.onMapError('Mapbox script did not initialize.');
                                    return;
                                  }
                                  try {
                                    window.initializeMapbox($tokenArgument);
                                  } catch (error) {
                                    window.RideMapBridge.onMapError(
                                      'Map initialization failed: ' + (error.message || 'unknown error')
                                    );
                                  }
                                })();
                            """.trimIndent(),
                            null,
                        )
                    }
                }

                override fun onReceivedError(
                    view: WebView,
                    request: android.webkit.WebResourceRequest,
                    error: android.webkit.WebResourceError,
                ) {
                    if (request.isForMainFrame && !destroyed.value) {
                        reportMapError("Map page failed to load: ${error.description}")
                    }
                }
            }
            addJavascriptInterface(
                object {
                    @JavascriptInterface
                    fun onMapReady() {
                        post {
                            if (!destroyed.value) mapReady.value = true
                        }
                    }

                    @JavascriptInterface
                    fun onMapError(message: String) {
                        post {
                            if (!destroyed.value && mapError.value == null) {
                                mapError.value = message
                                    .replace(accessToken, "[redacted]")
                                    .take(240)
                            }
                        }
                    }
                },
                "RideMapBridge",
            )
            loadUrl("file:///android_asset/ride_map.html")
        }
    }

    val renderLatest: () -> Unit = {
        if (mapReady.value && !destroyed.value) {
            webView.evaluateJavascript(
                "window.updateRideMap(${createUpdatePayload(latestRoute.value, latestPoint.value, latestSpeed.value)})",
                null,
            )
        }
    }

    DisposableEffect(webView, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> webView.onResume()
                Lifecycle.Event.ON_PAUSE -> webView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (!destroyed.value) {
                destroyed.value = true
                mapReady.value = false
                webView.removeJavascriptInterface("RideMapBridge")
                webView.stopLoading()
                webView.onPause()
                webView.destroy()
            }
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { webView },
            modifier = Modifier.fillMaxSize(),
            update = {
                renderLatest()
            },
        )

        mapError.value?.let { message ->
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(8.dp),
                color = MaterialTheme.colorScheme.errorContainer,
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

private fun createUpdatePayload(
    route: List<MapPoint>,
    point: TelemetryPoint?,
    speedKmh: Double,
): String {
    val routeCoordinates = JSONArray()
    route.filter { it.lat.isValidLatitude() && it.lng.isValidLongitude() && !it.isNoFix() }
        .forEach { mapPoint ->
            routeCoordinates.put(
                JSONObject()
                    .put("lat", mapPoint.lat)
                    .put("lng", mapPoint.lng),
            )
        }

    val current = if (point?.hasValidCoordinates == true) {
        JSONObject()
            .put("lat", point.latitude)
            .put("lng", point.longitude)
    } else {
        JSONObject.NULL
    }
    return JSONObject()
        .put("route", routeCoordinates)
        .put("currentPoint", current)
        .put("speedKmh", speedKmh.takeIf { it.isFinite() } ?: JSONObject.NULL)
        .toString()
}

private fun MapPoint.isNoFix(): Boolean = lat == 0.0 && lng == 0.0

private fun Double.isValidLatitude(): Boolean = isFinite() && this in -90.0..90.0

private fun Double.isValidLongitude(): Boolean = isFinite() && this in -180.0..180.0
