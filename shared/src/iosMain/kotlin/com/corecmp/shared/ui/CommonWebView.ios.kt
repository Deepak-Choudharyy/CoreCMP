package com.corecmp.shared.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.platform.LocalInspectionMode
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKWebView

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
@Composable
actual fun CommonWebView(url: String, modifier: Modifier) {
    if (LocalInspectionMode.current) {
        Box(
            modifier = modifier.fillMaxSize().background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Text("WebView Preview: $url")
        }
    } else {
        val nsUrl = remember(url) { NSURL.URLWithString(url) }
        UIKitView(
            factory = {
                WKWebView().apply {
                    nsUrl?.let { loadRequest(NSURLRequest.requestWithURL(it)) }
                }
            },
            update = { webView ->
                // WebView can be updated if the URL changes, but simple load is usually enough.
                // We could also do a load request if url changes.
                nsUrl?.let { webView.loadRequest(NSURLRequest.requestWithURL(it)) }
            },
            modifier = modifier
        )
    }
}
