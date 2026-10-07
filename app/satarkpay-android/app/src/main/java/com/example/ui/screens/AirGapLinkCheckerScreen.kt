package com.example.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.engine.DomainAnalysisResult
import com.example.engine.DomainTrustLevel
import com.example.engine.RuleEngine
import com.example.ui.theme.*

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AirGapLinkCheckerScreen(
    initialUrl: String = "",
    onBack: () -> Unit,
    onReportTo1930: (String) -> Unit = {},
    onAskSanchalak: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var urlInput by remember { mutableStateOf(if (initialUrl.isNotEmpty()) initialUrl else "https://cutt.ly/sec-upi-pay") }
    var activeUrlToLoad by remember { mutableStateOf<String?>(null) }
    var domainResult by remember { mutableStateOf<DomainAnalysisResult?>(null) }
    var isStrictSandbox by remember { mutableStateOf(true) } // JS disabled by default
    var isLoadingPage by remember { mutableStateOf(false) }
    var blockedRedirectCount by remember { mutableStateOf(0) }
    val scrollState = rememberScrollState()

    // Analyze domain initially
    LaunchedEffect(urlInput) {
        domainResult = RuleEngine.analyzeDomain(urlInput)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SatarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("airgap_back_button")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = SatarkInk)
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Air-Gap Link Sandbox",
                        style = MaterialTheme.typography.titleMedium,
                        color = SatarkInk,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = SatarkOkAlpha
                    ) {
                        Text(
                            text = "ZERO-RISK",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = SatarkOk,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "Isolated browser environment — cookies, storage & malicious scripts blocked",
                    style = MaterialTheme.typography.bodySmall,
                    color = SatarkDim,
                    fontSize = 11.sp
                )
            }
        }

        // Input Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SatarkPanelCard),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SatarkBorder)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = {
                        urlInput = it
                        domainResult = RuleEngine.analyzeDomain(it)
                    },
                    label = { Text("Paste suspicious link (SMS / WhatsApp)", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        if (urlInput.isNotEmpty()) {
                            IconButton(onClick = { urlInput = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = SatarkDim)
                            }
                        }
                    }
                )

                // Quick Samples
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val samples = listOf(
                        "cutt.ly/sec-upi-pay" to "Phishing Shortlink",
                        "sbi-kyc-verify.top" to "Lookalike Phishing",
                        "npci.org.in" to "Safe NPCI Portal",
                        "t.me/task_vip_daily" to "Telegram Task Funnel"
                    )
                    samples.forEach { (sampleUrl, label) ->
                        AssistChip(
                            onClick = {
                                urlInput = sampleUrl
                                domainResult = RuleEngine.analyzeDomain(sampleUrl)
                                activeUrlToLoad = if (sampleUrl.startsWith("http")) sampleUrl else "https://$sampleUrl"
                            },
                            label = { Text(label, fontSize = 10.5.sp) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val clean = if (urlInput.startsWith("http")) urlInput else "https://$urlInput"
                            activeUrlToLoad = clean
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SatarkAccent),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open In Sandbox", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = { onAskSanchalak("Is this link safe to open: $urlInput?") },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, tint = SatarkAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("AI Check", color = SatarkAccent, fontSize = 12.sp)
                    }
                }
            }
        }

        // Domain Security Intelligence Summary
        domainResult?.let { res ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (res.trustLevel) {
                        DomainTrustLevel.L1_VERIFIED_GOV_BANK -> SatarkOkAlpha
                        DomainTrustLevel.L4_LOOKALIKE_FAKE -> SatarkDangerAlpha
                        else -> SatarkWarnAlpha
                    }
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        when (res.trustLevel) {
                            DomainTrustLevel.L1_VERIFIED_GOV_BANK -> SatarkOk
                            DomainTrustLevel.L4_LOOKALIKE_FAKE -> SatarkDanger
                            else -> SatarkWarn
                        }
                    )
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DOMAIN TRUST LEVEL: ${res.trustLevel.name}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when (res.trustLevel) {
                                DomainTrustLevel.L1_VERIFIED_GOV_BANK -> SatarkOk
                                DomainTrustLevel.L4_LOOKALIKE_FAKE -> SatarkDanger
                                else -> SatarkWarn
                            }
                        )
                        if (blockedRedirectCount > 0) {
                            Text("🛡 $blockedRedirectCount Redirects Intercepted", fontSize = 10.sp, color = SatarkAccent)
                        }
                    }
                    Text(
                        text = res.warningMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = SatarkInk,
                        fontSize = 11.5.sp
                    )
                }
            }
        }

        // Air-Gap Isolation Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Security, contentDescription = null, tint = SatarkOk, modifier = Modifier.size(16.dp))
                Text("Strict Sandbox (No Script Exec)", style = MaterialTheme.typography.bodySmall, color = SatarkInk)
            }
            Switch(
                checked = isStrictSandbox,
                onCheckedChange = { isStrictSandbox = it }
            )
        }

        // Sandboxed WebView Viewport
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SatarkBorder)),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (activeUrlToLoad != null) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.apply {
                                    javaScriptEnabled = !isStrictSandbox
                                    allowFileAccess = false
                                    allowContentAccess = false
                                    databaseEnabled = false
                                    domStorageEnabled = false
                                    setGeolocationEnabled(false)
                                    userAgentString = "SatarkPay-AirGap-Sandbox/2.1 (Linux; Android; SecurityCrawler)"
                                }
                                webViewClient = object : WebViewClient() {
                                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                        isLoadingPage = true
                                    }
                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        isLoadingPage = false
                                    }
                                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                        val targetUrl = request?.url?.toString() ?: ""
                                        // Intercept external intent schemes or apk downloads
                                        if (targetUrl.startsWith("intent:") ||
                                            targetUrl.startsWith("upi:") ||
                                            targetUrl.startsWith("whatsapp:") ||
                                            targetUrl.endsWith(".apk")
                                        ) {
                                            blockedRedirectCount++
                                            return true // Block external launch to protect user device!
                                        }
                                        return false
                                    }
                                }
                                loadUrl(activeUrlToLoad!!)
                            }
                        },
                        update = { webView ->
                            webView.settings.javaScriptEnabled = !isStrictSandbox
                            if (activeUrlToLoad != null && webView.url != activeUrlToLoad) {
                                webView.loadUrl(activeUrlToLoad!!)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    if (isLoadingPage) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter),
                            color = SatarkAccent
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.VpnLock, contentDescription = null, tint = SatarkDim, modifier = Modifier.size(48.dp))
                        Text(
                            text = "Air-Gap Container Ready",
                            style = MaterialTheme.typography.titleSmall,
                            color = SatarkInk,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Paste any link above and tap 'Open In Sandbox' to inspect safely.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SatarkDim,
                            fontSize = 11.5.sp
                        )
                    }
                }
            }
        }
    }
}
