package com.corecmp.shared.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun CommonWebView(url: String, modifier: Modifier = Modifier)
