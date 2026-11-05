package com.pifauna.app.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.pifauna.app.ui.PiFaunaColors

/**
 * Privacy policy, readable with no network and no account.
 *
 * The policy is bundled as an asset rather than fetched, which is why this app
 * needs no INTERNET permission and can show the document in flight mode. The
 * WebView is additionally stopped from loading anything over the network, so a
 * bug in the markup cannot turn into a request.
 */
private const val POLICY_ASSET = "file:///android_asset/legal/privacy.html"
private const val POLICY_ONLINE = "https://layaswarna.berhampore.in/privacy/"

@Composable
fun AboutPrivacyScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onBack) {
                Text("Back", color = PiFaunaColors.Accent, fontSize = 15.sp)
            }
            Text(
                text = "OFFLINE COPY",
                color = PiFaunaColors.Muted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            factory = { context ->
                WebView(context).apply {
                    setBackgroundColor(AndroidColor.parseColor("#0B0716"))
                    settings.javaScriptEnabled = false
                    settings.domStorageEnabled = false
                    settings.allowFileAccess = true
                    settings.allowContentAccess = false
                    // Belt and braces: even if the asset referenced a remote URL,
                    // the WebView would fail it instead of issuing a request.
                    settings.blockNetworkLoads = true
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView,
                            request: WebResourceRequest
                        ): Boolean {
                            val url = request.url ?: return true
                            val scheme = url.scheme.orEmpty()
                            // file:// stays inside the WebView; anything else is
                            // handed to whichever app the user already has.
                            if (scheme == "file") return false
                            if (scheme == "http" || scheme == "https" || scheme == "mailto") {
                                return try {
                                    view.context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(url.toString()))
                                    )
                                    true
                                } catch (missing: ActivityNotFoundException) {
                                    true
                                }
                            }
                            return true
                        }
                    }
                    loadUrl(POLICY_ASSET)
                }
            }
        )

        TextButton(
            onClick = {
                // The offline copy above is the primary document; this opens the
                // published one in whatever browser the user already has. No
                // permission is needed to start that intent.
                runCatching {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(POLICY_ONLINE)))
                }
            },
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(
                text = "Open the published copy",
                color = PiFaunaColors.Muted,
                fontSize = 12.sp
            )
        }
    }
}
