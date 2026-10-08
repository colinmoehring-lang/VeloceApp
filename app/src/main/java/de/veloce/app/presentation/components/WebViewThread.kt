package de.veloce.app.presentation.components

import android.os.Handler
import android.os.Looper

// RideMap's JavaScript bridge can be called from a WebView thread.
internal fun Any.post(action: () -> Unit) {
    Handler(Looper.getMainLooper()).post(action)
}
